"""
图片文件存储模块
支持图片保存、管理、缩略图生成
"""
import os
import logging
import hashlib
import json
from datetime import datetime
from typing import List, Dict, Optional, Tuple
from dataclasses import dataclass, asdict
from pathlib import Path
import sqlite3

try:
    from PIL import Image
    HAS_PIL = True
except ImportError:
    HAS_PIL = False

logger = logging.getLogger(__name__)


@dataclass
class ImageInfo:
    """图片信息"""
    id: str
    filename: str
    original_name: str
    file_path: str
    thumbnail_path: Optional[str]
    file_size: int
    width: int
    height: int
    format: str
    checksum: str
    uploader: str
    upload_time: str
    device_source: Optional[str] = None
    metadata: Dict = None
    is_deleted: bool = False

    def __post_init__(self):
        if self.metadata is None:
            self.metadata = {}

    def to_dict(self) -> Dict:
        return asdict(self)

    def to_json(self) -> str:
        return json.dumps(self.to_dict())


class ImageStorage:
    """图片存储管理"""

    # 支持的图片格式
    SUPPORTED_FORMATS = {'.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.tiff'}

    # 缩略图尺寸
    THUMBNAIL_SIZE = (200, 200)

    def __init__(self, storage_dir: str = None, db_path: str = None):
        """
        初始化图片存储

        Args:
            storage_dir: 存储目录
            db_path: 数据库路径
        """
        self.storage_dir = storage_dir or os.path.join(os.getcwd(), 'image_storage')
        self.thumbnail_dir = os.path.join(self.storage_dir, 'thumbnails')
        self.db_path = db_path or os.path.join(self.storage_dir, 'images.db')

        # 创建目录
        os.makedirs(self.storage_dir, exist_ok=True)
        os.makedirs(self.thumbnail_dir, exist_ok=True)

        # 初始化数据库
        self._init_database()

    def _init_database(self):
        """初始化数据库"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        # 创建图片表
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS images (
                id TEXT PRIMARY KEY,
                filename TEXT NOT NULL,
                original_name TEXT NOT NULL,
                file_path TEXT NOT NULL,
                thumbnail_path TEXT,
                file_size INTEGER NOT NULL,
                width INTEGER,
                height INTEGER,
                format TEXT,
                checksum TEXT NOT NULL,
                uploader TEXT NOT NULL,
                upload_time TEXT NOT NULL,
                device_source TEXT,
                metadata TEXT,
                is_deleted INTEGER DEFAULT 0
            )
        ''')

        # 创建索引
        cursor.execute('''
            CREATE INDEX IF NOT EXISTS idx_uploader ON images(uploader)
        ''')
        cursor.execute('''
            CREATE INDEX IF NOT EXISTS idx_upload_time ON images(upload_time)
        ''')
        cursor.execute('''
            CREATE INDEX IF NOT EXISTS idx_checksum ON images(checksum)
        ''')

        conn.commit()
        conn.close()

    def save_image(self, image_data: bytes, original_name: str, uploader: str,
                   device_source: str = None, metadata: Dict = None) -> ImageInfo:
        """
        保存图片

        Args:
            image_data: 图片二进制数据
            original_name: 原始文件名
            uploader: 上传者
            device_source: 设备来源
            metadata: 元数据

        Returns:
            图片信息
        """
        if not HAS_PIL:
            raise ImportError("Pillow库未安装，请运行: pip install Pillow")

        # 生成唯一ID和文件名
        image_id = self._generate_id()
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        ext = os.path.splitext(original_name)[1].lower()

        if ext not in self.SUPPORTED_FORMATS:
            raise ValueError(f"不支持的图片格式: {ext}")

        filename = f"{timestamp}_{image_id}{ext}"
        file_path = os.path.join(self.storage_dir, filename)

        # 计算校验和
        checksum = hashlib.md5(image_data).hexdigest()

        # 检查是否已存在
        existing = self._find_by_checksum(checksum)
        if existing:
            logger.info(f"图片已存在: {existing.id}")
            return existing

        # 保存原图
        with open(file_path, 'wb') as f:
            f.write(image_data)

        # 获取图片信息
        try:
            img = Image.open(file_path)
            width, height = img.size
            img_format = img.format or ext[1:].upper()
        except Exception as e:
            logger.warning(f"无法读取图片信息: {e}")
            width, height = 0, 0
            img_format = ext[1:].upper()

        # 生成缩略图
        thumbnail_path = None
        try:
            thumbnail_path = self._create_thumbnail(file_path, image_id, ext)
        except Exception as e:
            logger.warning(f"创建缩略图失败: {e}")

        # 创建图片信息
        image_info = ImageInfo(
            id=image_id,
            filename=filename,
            original_name=original_name,
            file_path=file_path,
            thumbnail_path=thumbnail_path,
            file_size=len(image_data),
            width=width,
            height=height,
            format=img_format,
            checksum=checksum,
            uploader=uploader,
            upload_time=datetime.now().isoformat(),
            device_source=device_source,
            metadata=metadata or {}
        )

        # 保存到数据库
        self._save_to_db(image_info)

        logger.info(f"图片已保存: {image_id} - {original_name}")
        return image_info

    def save_image_from_file(self, file_path: str, uploader: str,
                             device_source: str = None, metadata: Dict = None) -> ImageInfo:
        """
        从文件保存图片

        Args:
            file_path: 文件路径
            uploader: 上传者
            device_source: 设备来源
            metadata: 元数据

        Returns:
            图片信息
        """
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"文件不存在: {file_path}")

        with open(file_path, 'rb') as f:
            image_data = f.read()

        original_name = os.path.basename(file_path)
        return self.save_image(image_data, original_name, uploader, device_source, metadata)

    def _create_thumbnail(self, file_path: str, image_id: str, ext: str) -> str:
        """创建缩略图"""
        if not HAS_PIL:
            return None

        try:
            img = Image.open(file_path)

            # 保持宽高比缩放
            img.thumbnail(self.THUMBNAIL_SIZE, Image.Resampling.LANCZOS)

            # 保存缩略图
            thumbnail_filename = f"thumb_{image_id}{ext}"
            thumbnail_path = os.path.join(self.thumbnail_dir, thumbnail_filename)

            # 转换为RGB（如果需要）
            if img.mode in ('RGBA', 'LA', 'P'):
                background = Image.new('RGB', img.size, (255, 255, 255))
                if img.mode == 'P':
                    img = img.convert('RGBA')
                background.paste(img, mask=img.split()[-1] if img.mode in ('RGBA', 'LA') else None)
                img = background

            img.save(thumbnail_path, quality=85)
            return thumbnail_path

        except Exception as e:
            logger.error(f"创建缩略图失败: {e}")
            return None

    def _generate_id(self) -> str:
        """生成唯一ID"""
        import uuid
        return uuid.uuid4().hex[:12]

    def _find_by_checksum(self, checksum: str) -> Optional[ImageInfo]:
        """根据校验和查找图片"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        cursor.execute('''
            SELECT id, filename, original_name, file_path, thumbnail_path,
                   file_size, width, height, format, checksum, uploader,
                   upload_time, device_source, metadata, is_deleted
            FROM images
            WHERE checksum = ? AND is_deleted = 0
        ''', (checksum,))

        row = cursor.fetchone()
        conn.close()

        if row:
            return self._row_to_image_info(row)
        return None

    def _save_to_db(self, image_info: ImageInfo):
        """保存到数据库"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        cursor.execute('''
            INSERT OR REPLACE INTO images
            (id, filename, original_name, file_path, thumbnail_path,
             file_size, width, height, format, checksum, uploader,
             upload_time, device_source, metadata, is_deleted)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            image_info.id,
            image_info.filename,
            image_info.original_name,
            image_info.file_path,
            image_info.thumbnail_path,
            image_info.file_size,
            image_info.width,
            image_info.height,
            image_info.format,
            image_info.checksum,
            image_info.uploader,
            image_info.upload_time,
            image_info.device_source,
            json.dumps(image_info.metadata),
            1 if image_info.is_deleted else 0
        ))

        conn.commit()
        conn.close()

    def _row_to_image_info(self, row: tuple) -> ImageInfo:
        """数据库行转ImageInfo"""
        return ImageInfo(
            id=row[0],
            filename=row[1],
            original_name=row[2],
            file_path=row[3],
            thumbnail_path=row[4],
            file_size=row[5],
            width=row[6],
            height=row[7],
            format=row[8],
            checksum=row[9],
            uploader=row[10],
            upload_time=row[11],
            device_source=row[12],
            metadata=json.loads(row[13]) if row[13] else {},
            is_deleted=bool(row[14])
        )

    def get_image(self, image_id: str) -> Optional[ImageInfo]:
        """获取图片信息"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        cursor.execute('''
            SELECT id, filename, original_name, file_path, thumbnail_path,
                   file_size, width, height, format, checksum, uploader,
                   upload_time, device_source, metadata, is_deleted
            FROM images
            WHERE id = ? AND is_deleted = 0
        ''', (image_id,))

        row = cursor.fetchone()
        conn.close()

        if row:
            return self._row_to_image_info(row)
        return None

    def get_image_data(self, image_id: str) -> Optional[bytes]:
        """获取图片数据"""
        image_info = self.get_image(image_id)
        if not image_info or not os.path.exists(image_info.file_path):
            return None

        with open(image_info.file_path, 'rb') as f:
            return f.read()

    def get_thumbnail_data(self, image_id: str) -> Optional[bytes]:
        """获取缩略图数据"""
        image_info = self.get_image(image_id)
        if not image_info or not image_info.thumbnail_path:
            return None

        if not os.path.exists(image_info.thumbnail_path):
            return None

        with open(image_info.thumbnail_path, 'rb') as f:
            return f.read()

    def list_images(self, uploader: str = None, limit: int = 100, offset: int = 0) -> List[ImageInfo]:
        """
        列出图片

        Args:
            uploader: 上传者过滤
            limit: 限制数量
            offset: 偏移量

        Returns:
            图片列表
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        if uploader:
            cursor.execute('''
                SELECT id, filename, original_name, file_path, thumbnail_path,
                       file_size, width, height, format, checksum, uploader,
                       upload_time, device_source, metadata, is_deleted
                FROM images
                WHERE uploader = ? AND is_deleted = 0
                ORDER BY upload_time DESC
                LIMIT ? OFFSET ?
            ''', (uploader, limit, offset))
        else:
            cursor.execute('''
                SELECT id, filename, original_name, file_path, thumbnail_path,
                       file_size, width, height, format, checksum, uploader,
                       upload_time, device_source, metadata, is_deleted
                FROM images
                WHERE is_deleted = 0
                ORDER BY upload_time DESC
                LIMIT ? OFFSET ?
            ''', (limit, offset))

        rows = cursor.fetchall()
        conn.close()

        return [self._row_to_image_info(row) for row in rows]

    def delete_image(self, image_id: str, hard_delete: bool = False) -> bool:
        """
        删除图片

        Args:
            image_id: 图片ID
            hard_delete: 是否硬删除

        Returns:
            是否成功
        """
        image_info = self.get_image(image_id)
        if not image_info:
            return False

        if hard_delete:
            # 删除文件
            if os.path.exists(image_info.file_path):
                os.remove(image_info.file_path)
            if image_info.thumbnail_path and os.path.exists(image_info.thumbnail_path):
                os.remove(image_info.thumbnail_path)

            # 从数据库删除
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            cursor.execute('DELETE FROM images WHERE id = ?', (image_id,))
            conn.commit()
            conn.close()
        else:
            # 软删除
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            cursor.execute('UPDATE images SET is_deleted = 1 WHERE id = ?', (image_id,))
            conn.commit()
            conn.close()

        logger.info(f"图片已删除: {image_id}")
        return True

    def search_images(self, keyword: str, uploader: str = None) -> List[ImageInfo]:
        """
        搜索图片

        Args:
            keyword: 搜索关键词
            uploader: 上传者过滤

        Returns:
            图片列表
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        search_pattern = f"%{keyword}%"

        if uploader:
            cursor.execute('''
                SELECT id, filename, original_name, file_path, thumbnail_path,
                       file_size, width, height, format, checksum, uploader,
                       upload_time, device_source, metadata, is_deleted
                FROM images
                WHERE (original_name LIKE ? OR device_source LIKE ?)
                      AND uploader = ? AND is_deleted = 0
                ORDER BY upload_time DESC
            ''', (search_pattern, search_pattern, uploader))
        else:
            cursor.execute('''
                SELECT id, filename, original_name, file_path, thumbnail_path,
                       file_size, width, height, format, checksum, uploader,
                       upload_time, device_source, metadata, is_deleted
                FROM images
                WHERE (original_name LIKE ? OR device_source LIKE ?)
                      AND is_deleted = 0
                ORDER BY upload_time DESC
            ''', (search_pattern, search_pattern))

        rows = cursor.fetchall()
        conn.close()

        return [self._row_to_image_info(row) for row in rows]

    def get_statistics(self, uploader: str = None) -> Dict:
        """
        获取统计信息

        Args:
            uploader: 上传者过滤

        Returns:
            统计信息
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        if uploader:
            cursor.execute('''
                SELECT COUNT(*), SUM(file_size)
                FROM images
                WHERE uploader = ? AND is_deleted = 0
            ''', (uploader,))
        else:
            cursor.execute('''
                SELECT COUNT(*), SUM(file_size)
                FROM images
                WHERE is_deleted = 0
            ''')

        row = cursor.fetchone()
        conn.close()

        return {
            'total_count': row[0] or 0,
            'total_size': row[1] or 0
        }

    def cleanup_deleted(self, days: int = 30) -> int:
        """
        清理已删除的图片

        Args:
            days: 删除多少天前的图片

        Returns:
            清理数量
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        # 查找要删除的图片
        cursor.execute('''
            SELECT id, file_path, thumbnail_path
            FROM images
            WHERE is_deleted = 1
                  AND datetime(upload_time) < datetime('now', ?)
        ''', (f'-{days} days',))

        rows = cursor.fetchall()
        count = 0

        for row in rows:
            image_id, file_path, thumbnail_path = row

            # 删除文件
            try:
                if os.path.exists(file_path):
                    os.remove(file_path)
                if thumbnail_path and os.path.exists(thumbnail_path):
                    os.remove(thumbnail_path)

                # 从数据库删除
                cursor.execute('DELETE FROM images WHERE id = ?', (image_id,))
                count += 1

            except Exception as e:
                logger.error(f"清理图片失败 {image_id}: {e}")

        conn.commit()
        conn.close()

        logger.info(f"已清理 {count} 个图片")
        return count


if __name__ == "__main__":
    # 测试代码
    logging.basicConfig(level=logging.INFO)

    # 创建图片存储实例
    storage = ImageStorage()

    # 测试保存图片
    print("图片存储模块已初始化")
    print(f"存储目录: {storage.storage_dir}")
    print(f"数据库路径: {storage.db_path}")