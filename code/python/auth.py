"""
权限控制模块
支持用户+管理员级别的权限验证
"""
import os
import json
import logging
import hashlib
import sqlite3
from datetime import datetime, timedelta
from typing import Optional, Dict, List
from dataclasses import dataclass, asdict
from functools import wraps

try:
    import jwt
    HAS_JWT = True
except ImportError:
    HAS_JWT = False

try:
    from passlib.hash import pbkdf2_sha256
    HAS_PASSLIB = True
except ImportError:
    HAS_PASSLIB = False

logger = logging.getLogger(__name__)


# 用户角色
class UserRole:
    ADMIN = 'admin'
    USER = 'user'
    GUEST = 'guest'


# 权限级别
class PermissionLevel:
    ADMIN = 100
    USER = 50
    GUEST = 10


@dataclass
class User:
    """用户信息"""
    id: str
    username: str
    password_hash: str
    role: str
    email: Optional[str] = None
    created_at: str = None
    last_login: str = None
    is_active: bool = True
    metadata: Dict = None

    def __post_init__(self):
        if self.created_at is None:
            self.created_at = datetime.now().isoformat()
        if self.metadata is None:
            self.metadata = {}

    def to_dict(self) -> Dict:
        return asdict(self)

    def check_password(self, password: str) -> bool:
        """验证密码"""
        if HAS_PASSLIB:
            return pbkdf2_sha256.verify(password, self.password_hash)
        else:
            return self.password_hash == hashlib.sha256(password.encode()).hexdigest()


class AuthManager:
    """权限管理器"""

    JWT_SECRET = os.environ.get('JWT_SECRET', 'your-secret-key-change-in-production')
    JWT_ALGORITHM = 'HS256'
    JWT_EXPIRATION_HOURS = 24

    def __init__(self, db_path: str = None):
        self.db_path = db_path or os.path.join(os.getcwd(), 'auth.db')
        self._init_database()

    def _init_database(self):
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        cursor.execute('''
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL,
                email TEXT,
                created_at TEXT NOT NULL,
                last_login TEXT,
                is_active INTEGER DEFAULT 1,
                metadata TEXT
            )
        ''')

        cursor.execute('''
            CREATE TABLE IF NOT EXISTS sessions (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                token TEXT UNIQUE NOT NULL,
                created_at TEXT NOT NULL,
                expires_at TEXT NOT NULL,
                is_valid INTEGER DEFAULT 1,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        ''')

        cursor.execute('''
            CREATE TABLE IF NOT EXISTS operation_logs (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                action TEXT NOT NULL,
                resource TEXT,
                details TEXT,
                ip_address TEXT,
                created_at TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        ''')

        cursor.execute('CREATE INDEX IF NOT EXISTS idx_username ON users(username)')
        cursor.execute('CREATE INDEX IF NOT EXISTS idx_user_id ON sessions(user_id)')
        cursor.execute('CREATE INDEX IF NOT EXISTS idx_session_token ON sessions(token)')

        conn.commit()
        conn.close()

        self._create_default_admin()

    def _create_default_admin(self):
        if not self.get_user_by_username('admin'):
            self.create_user(
                username='admin',
                password='admin123',
                role=UserRole.ADMIN,
                email='admin@example.com'
            )
            logger.info("已创建默认管理员账户: admin / admin123")

    def _generate_id(self) -> str:
        import uuid
        return uuid.uuid4().hex

    def _hash_password(self, password: str) -> str:
        if HAS_PASSLIB:
            return pbkdf2_sha256.hash(password)
        else:
            return hashlib.sha256(password.encode()).hexdigest()

    def create_user(self, username: str, password: str, role: str = UserRole.USER,
                    email: str = None, metadata: Dict = None) -> User:
        if self.get_user_by_username(username):
            raise ValueError(f"用户名已存在: {username}")

        user = User(
            id=self._generate_id(),
            username=username,
            password_hash=self._hash_password(password),
            role=role,
            email=email,
            metadata=metadata or {}
        )

        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        cursor.execute('''
            INSERT INTO users (id, username, password_hash, role, email, created_at, last_login, is_active, metadata)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            user.id, user.username, user.password_hash, user.role, user.email,
            user.created_at, user.last_login, 1 if user.is_active else 0,
            json.dumps(user.metadata)
        ))

        conn.commit()
        conn.close()

        logger.info(f"用户已创建: {username} ({role})")
        return user

    def get_user_by_id(self, user_id: str) -> Optional[User]:
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        cursor.execute('''
            SELECT id, username, password_hash, role, email, created_at, last_login, is_active, metadata
            FROM users WHERE id = ?
        ''', (user_id,))

        row = cursor.fetchone()
        conn.close()

        return self._row_to_user(row) if row else None

    def get_user_by_username(self, username: str) -> Optional[User]:
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        cursor.execute('''
            SELECT id, username, password_hash, role, email, created_at, last_login, is_active, metadata
            FROM users WHERE username = ?
        ''', (username,))

        row = cursor.fetchone()
        conn.close()

        return self._row_to_user(row) if row else None

    def _row_to_user(self, row: tuple) -> User:
        return User(
            id=row[0], username=row[1], password_hash=row[2], role=row[3],
            email=row[4], created_at=row[5], last_login=row[6],
            is_active=bool(row[7]), metadata=json.loads(row[8]) if row[8] else {}
        )

    def authenticate(self, username: str, password: str) -> Optional[Dict]:
        user = self.get_user_by_username(username)

        if not user or not user.is_active or not user.check_password(password):
            return None

        self._update_last_login(user.id)
        token = self._generate_token(user)
        self.log_operation(user.id, 'login', 'auth', {'method': 'password'})

        return {
            'token': token,
            'user': user.to_dict(),
            'expires_at': (datetime.now() + timedelta(hours=self.JWT_EXPIRATION_HOURS)).isoformat()
        }

    def _update_last_login(self, user_id: str):
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        cursor.execute('UPDATE users SET last_login = ? WHERE id = ?', (datetime.now().isoformat(), user_id))
        conn.commit()
        conn.close()

    def _generate_token(self, user: User) -> str:
        if not HAS_JWT:
            raise ImportError("PyJWT库未安装")

        payload = {
            'user_id': user.id,
            'username': user.username,
            'role': user.role,
            'exp': datetime.utcnow() + timedelta(hours=self.JWT_EXPIRATION_HOURS),
            'iat': datetime.utcnow()
        }

        token = jwt.encode(payload, self.JWT_SECRET, algorithm=self.JWT_ALGORITHM)
        self._save_session(user.id, token)
        return token

    def _save_session(self, user_id: str, token: str):
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        cursor.execute('''
            INSERT INTO sessions (id, user_id, token, created_at, expires_at, is_valid)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (
            self._generate_id(), user_id, token, datetime.now().isoformat(),
            (datetime.now() + timedelta(hours=self.JWT_EXPIRATION_HOURS)).isoformat(), 1
        ))

        conn.commit()
        conn.close()

    def verify_token(self, token: str) -> Optional[Dict]:
        if not HAS_JWT:
            raise ImportError("PyJWT库未安装")

        try:
            payload = jwt.decode(token, self.JWT_SECRET, algorithms=[self.JWT_ALGORITHM])
            if not self._is_session_valid(token):
                return None
            return {
                'user_id': payload['user_id'],
                'username': payload['username'],
                'role': payload['role']
            }
        except jwt.ExpiredSignatureError:
            return None
        except jwt.InvalidTokenError:
            return None

    def _is_session_valid(self, token: str) -> bool:
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        cursor.execute('SELECT is_valid, expires_at FROM sessions WHERE token = ?', (token,))
        row = cursor.fetchone()
        conn.close()

        if not row:
            return False

        return bool(row[0]) and datetime.now() < datetime.fromisoformat(row[1])

    def logout(self, token: str) -> bool:
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        cursor.execute('UPDATE sessions SET is_valid = 0 WHERE token = ?', (token,))
        affected = cursor.rowcount
        conn.commit()
        conn.close()
        return affected > 0

    def check_permission(self, user_role: str, required_role: str) -> bool:
        """检查权限"""
        role_levels = {
            UserRole.ADMIN: PermissionLevel.ADMIN,
            UserRole.USER: PermissionLevel.USER,
            UserRole.GUEST: PermissionLevel.GUEST
        }

        user_level = role_levels.get(user_role, PermissionLevel.GUEST)
        required_level = role_levels.get(required_role, PermissionLevel.ADMIN)

        return user_level >= required_level

    def log_operation(self, user_id: str, action: str, resource: str = None,
                      details: Dict = None, ip_address: str = None):
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        cursor.execute('''
            INSERT INTO operation_logs (id, user_id, action, resource, details, ip_address, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        ''', (
            self._generate_id(), user_id, action, resource,
            json.dumps(details) if details else None, ip_address, datetime.now().isoformat()
        ))

        conn.commit()
        conn.close()

    def get_operation_logs(self, user_id: str = None, limit: int = 100) -> List[Dict]:
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        if user_id:
            cursor.execute('''
                SELECT id, user_id, action, resource, details, ip_address, created_at
                FROM operation_logs WHERE user_id = ? ORDER BY created_at DESC LIMIT ?
            ''', (user_id, limit))
        else:
            cursor.execute('''
                SELECT id, user_id, action, resource, details, ip_address, created_at
                FROM operation_logs ORDER BY created_at DESC LIMIT ?
            ''', (limit,))

        rows = cursor.fetchall()
        conn.close()

        return [{
            'id': row[0], 'user_id': row[1], 'action': row[2],
            'resource': row[3], 'details': json.loads(row[4]) if row[4] else None,
            'ip_address': row[5], 'created_at': row[6]
        } for row in rows]

    def list_users(self, role: str = None) -> List[User]:
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        if role:
            cursor.execute('''
                SELECT id, username, password_hash, role, email, created_at, last_login, is_active, metadata
                FROM users WHERE role = ? ORDER BY created_at DESC
            ''', (role,))
        else:
            cursor.execute('''
                SELECT id, username, password_hash, role, email, created_at, last_login, is_active, metadata
                FROM users ORDER BY created_at DESC
            ''')

        rows = cursor.fetchall()
        conn.close()

        return [self._row_to_user(row) for row in rows]


def require_permission(min_role: str = UserRole.USER):
    """权限装饰器"""
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            user = kwargs.get('user') or kwargs.get('current_user')
            if not user:
                raise PermissionError("未登录")

            auth_manager = kwargs.get('auth_manager')
            if auth_manager and not auth_manager.check_permission(user.get('role'), min_role):
                raise PermissionError(f"权限不足，需要 {min_role} 角色")

            return func(*args, **kwargs)
        return wrapper
    return decorator


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)

    auth = AuthManager()
    print("权限管理模块已初始化")

    # 测试登录
    result = auth.authenticate('admin', 'admin123')
    if result:
        print(f"登录成功: {result['user']['username']}")
        print(f"Token: {result['token'][:50]}...")