"""
Web应用主模块
提供REST API接口
"""
import os
import logging
import asyncio
from datetime import datetime
from flask import Flask, request, jsonify, send_file, send_from_directory
from flask_cors import CORS
from functools import wraps
import json

# 导入自定义模块
from bluetooth_discovery import BluetoothDiscovery, BluetoothDevice
from lan_discovery import LANDiscovery, NetworkDevice
from data_transfer import DataTransferManager, TCPTransfer, HTTPTransfer
from image_storage import ImageStorage
from auth import AuthManager, UserRole, require_permission

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 创建Flask应用
app = Flask(__name__)
CORS(app)

# 配置
app.config['SECRET_KEY'] = os.environ.get('SECRET_KEY', 'dev-secret-key')
app.config['MAX_CONTENT_LENGTH'] = 50 * 1024 * 1024  # 最大50MB

# 初始化模块
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, 'data')
IMAGE_DIR = os.path.join(DATA_DIR, 'images')

os.makedirs(DATA_DIR, exist_ok=True)
os.makedirs(IMAGE_DIR, exist_ok=True)

auth_manager = AuthManager(db_path=os.path.join(DATA_DIR, 'auth.db'))
image_storage = ImageStorage(storage_dir=IMAGE_DIR, db_path=os.path.join(DATA_DIR, 'images.db'))
bluetooth_discovery = BluetoothDiscovery()
lan_discovery = LANDiscovery()
data_transfer = DataTransferManager()


# ==================== 权限装饰器 ====================

def token_required(f):
    """Token验证装饰器"""
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization')

        if not token:
            return jsonify({'error': '缺少Token'}), 401

        # 移除Bearer前缀
        if token.startswith('Bearer '):
            token = token[7:]

        user_info = auth_manager.verify_token(token)
        if not user_info:
            return jsonify({'error': 'Token无效或已过期'}), 401

        # 添加用户信息到请求
        request.current_user = user_info
        return f(*args, **kwargs)

    return decorated


def admin_required(f):
    """管理员权限装饰器"""
    @wraps(f)
    @token_required
    def decorated(*args, **kwargs):
        if request.current_user.get('role') != UserRole.ADMIN:
            return jsonify({'error': '需要管理员权限'}), 403
        return f(*args, **kwargs)

    return decorated


def user_or_admin_required(f):
    """用户或管理员权限装饰器"""
    @wraps(f)
    @token_required
    def decorated(*args, **kwargs):
        role = request.current_user.get('role')
        if role not in [UserRole.USER, UserRole.ADMIN]:
            return jsonify({'error': '需要用户或管理员权限'}), 403
        return f(*args, **kwargs)

    return decorated


# ==================== 认证API ====================

@app.route('/api/auth/login', methods=['POST'])
def login():
    """用户登录"""
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({'error': '缺少用户名或密码'}), 400

    result = auth_manager.authenticate(username, password)
    if not result:
        return jsonify({'error': '用户名或密码错误'}), 401

    return jsonify({
        'success': True,
        'token': result['token'],
        'user': result['user'],
        'expires_at': result['expires_at']
    })


@app.route('/api/auth/logout', methods=['POST'])
@token_required
def logout():
    """用户注销"""
    token = request.headers.get('Authorization')
    if token.startswith('Bearer '):
        token = token[7:]

    success = auth_manager.logout(token)
    return jsonify({'success': success})


@app.route('/api/auth/register', methods=['POST'])
@admin_required
def register():
    """注册新用户（仅管理员）"""
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')
    role = data.get('role', UserRole.USER)
    email = data.get('email')

    if not username or not password:
        return jsonify({'error': '缺少用户名或密码'}), 400

    try:
        user = auth_manager.create_user(username, password, role, email)
        return jsonify({
            'success': True,
            'user': user.to_dict()
        })
    except ValueError as e:
        return jsonify({'error': str(e)}), 400


@app.route('/api/auth/users', methods=['GET'])
@admin_required
def list_users():
    """列出用户（仅管理员）"""
    role = request.args.get('role')
    users = auth_manager.list_users(role)
    return jsonify({
        'success': True,
        'users': [u.to_dict() for u in users]
    })


@app.route('/api/auth/verify', methods=['GET'])
@token_required
def verify_token():
    """验证Token"""
    return jsonify({
        'success': True,
        'user': request.current_user
    })


# ==================== 蓝牙设备API ====================

@app.route('/api/bluetooth/scan', methods=['POST'])
@user_or_admin_required
def scan_bluetooth():
    """扫描蓝牙设备"""
    data = request.get_json() or {}
    timeout = data.get('timeout', 10.0)

    try:
        # 异步扫描
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)

        devices = loop.run_until_complete(
            bluetooth_discovery.start_scan(timeout=timeout)
        )

        loop.close()

        # 记录操作
        auth_manager.log_operation(
            request.current_user['user_id'],
            'bluetooth_scan',
            'bluetooth',
            {'timeout': timeout, 'device_count': len(devices)}
        )

        return jsonify({
            'success': True,
            'devices': [d.to_dict() for d in devices],
            'count': len(devices)
        })

    except Exception as e:
        logger.error(f"蓝牙扫描失败: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/bluetooth/devices', methods=['GET'])
@user_or_admin_required
def get_bluetooth_devices():
    """获取已发现的蓝牙设备"""
    devices = bluetooth_discovery.get_discovered_devices()
    return jsonify({
        'success': True,
        'devices': [d.to_dict() for d in devices]
    })


@app.route('/api/bluetooth/connect/<address>', methods=['POST'])
@user_or_admin_required
def connect_bluetooth(address):
    """连接蓝牙设备"""
    try:
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)

        client = loop.run_until_complete(
            bluetooth_discovery.connect_device(address)
        )

        loop.close()

        if client:
            auth_manager.log_operation(
                request.current_user['user_id'],
                'bluetooth_connect',
                address
            )
            return jsonify({'success': True, 'connected': True})
        else:
            return jsonify({'error': '连接失败'}), 500

    except Exception as e:
        return jsonify({'error': str(e)}), 500


# ==================== 局域网设备API ====================

@app.route('/api/lan/scan', methods=['POST'])
@user_or_admin_required
def scan_lan():
    """扫描局域网设备"""
    data = request.get_json() or {}
    network_range = data.get('network_range')
    scan_ports = data.get('scan_ports', False)

    try:
        devices = lan_discovery.full_scan(network_range, scan_ports)

        auth_manager.log_operation(
            request.current_user['user_id'],
            'lan_scan',
            'network',
            {'network_range': network_range, 'scan_ports': scan_ports, 'device_count': len(devices)}
        )

        return jsonify({
            'success': True,
            'devices': [d.to_dict() for d in devices],
            'count': len(devices)
        })

    except Exception as e:
        logger.error(f"局域网扫描失败: {e}")
        return jsonify({'error': str(e)}), 500


@app.route('/api/lan/devices', methods=['GET'])
@user_or_admin_required
def get_lan_devices():
    """获取已发现的局域网设备"""
    devices = lan_discovery.get_all_devices()
    return jsonify({
        'success': True,
        'devices': [d.to_dict() for d in devices]
    })


@app.route('/api/lan/ports/<ip>', methods=['GET'])
@user_or_admin_required
def scan_ports(ip):
    """扫描设备端口"""
    ports = request.args.get('ports', '')

    try:
        port_list = [int(p) for p in ports.split(',') if p] if ports else None
        open_ports = lan_discovery.scan_ports(ip, port_list)

        return jsonify({
            'success': True,
            'ip': ip,
            'open_ports': open_ports
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500


# ==================== 数据传输API ====================

@app.route('/api/transfer/send', methods=['POST'])
@user_or_admin_required
def send_data():
    """发送数据"""
    data = request.get_json()
    target = data.get('target')
    protocol = data.get('protocol', 'tcp')
    port = data.get('port')
    message = data.get('message')

    if not target or not message:
        return jsonify({'error': '缺少目标地址或消息'}), 400

    try:
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)

        result = loop.run_until_complete(
            data_transfer.send_to_device(target, message.encode(), protocol, port)
        )

        loop.close()

        auth_manager.log_operation(
            request.current_user['user_id'],
            'data_send',
            target,
            {'protocol': protocol, 'success': result.success}
        )

        return jsonify({
            'success': result.success,
            'data_size': result.data_size,
            'duration': result.duration,
            'error': result.error
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/api/transfer/file', methods=['POST'])
@user_or_admin_required
def send_file():
    """发送文件"""
    if 'file' not in request.files:
        return jsonify({'error': '缺少文件'}), 400

    file = request.files['file']
    target = request.form.get('target')
    protocol = request.form.get('protocol', 'tcp')
    port = request.form.get('port')

    if not target:
        return jsonify({'error': '缺少目标地址'}), 400

    # 临时保存文件
    temp_path = os.path.join(DATA_DIR, 'temp', file.filename)
    os.makedirs(os.path.dirname(temp_path), exist_ok=True)
    file.save(temp_path)

    try:
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)

        result = loop.run_until_complete(
            data_transfer.send_file_to_device(target, temp_path, protocol, port)
        )

        loop.close()

        # 清理临时文件
        os.remove(temp_path)

        return jsonify({
            'success': result.success,
            'data_size': result.data_size,
            'duration': result.duration,
            'error': result.error
        })

    except Exception as e:
        os.remove(temp_path)
        return jsonify({'error': str(e)}), 500


# ==================== 图片存储API ====================

@app.route('/api/images/upload', methods=['POST'])
@user_or_admin_required
def upload_image():
    """上传图片"""
    if 'image' not in request.files:
        return jsonify({'error': '缺少图片文件'}), 400

    image_file = request.files['image']
    device_source = request.form.get('device_source')
    metadata_str = request.form.get('metadata', '{}')

    try:
        metadata = json.loads(metadata_str)
        image_data = image_file.read()

        image_info = image_storage.save_image(
            image_data,
            image_file.filename,
            request.current_user['username'],
            device_source,
            metadata
        )

        auth_manager.log_operation(
            request.current_user['user_id'],
            'image_upload',
            image_info.id,
            {'filename': image_file.filename}
        )

        return jsonify({
            'success': True,
            'image': image_info.to_dict()
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/api/images', methods=['GET'])
@user_or_admin_required
def list_images():
    """列出图片"""
    # 管理员可以查看所有图片，用户只能查看自己的
    if request.current_user['role'] == UserRole.ADMIN:
        uploader = request.args.get('uploader')
    else:
        uploader = request.current_user['username']

    limit = int(request.args.get('limit', 50))
    offset = int(request.args.get('offset', 0))

    images = image_storage.list_images(uploader, limit, offset)
    stats = image_storage.get_statistics(uploader)

    return jsonify({
        'success': True,
        'images': [img.to_dict() for img in images],
        'statistics': stats
    })


@app.route('/api/images/<image_id>', methods=['GET'])
@user_or_admin_required
def get_image(image_id):
    """获取图片详情"""
    image_info = image_storage.get_image(image_id)

    if not image_info:
        return jsonify({'error': '图片不存在'}), 404

    # 权限检查：用户只能查看自己的图片，管理员可以查看所有
    if request.current_user['role'] != UserRole.ADMIN:
        if image_info.uploader != request.current_user['username']:
            return jsonify({'error': '无权访问此图片'}), 403

    return jsonify({
        'success': True,
        'image': image_info.to_dict()
    })


@app.route('/api/images/<image_id>/data', methods=['GET'])
@user_or_admin_required
def get_image_data(image_id):
    """获取图片文件"""
    image_info = image_storage.get_image(image_id)

    if not image_info:
        return jsonify({'error': '图片不存在'}), 404

    # 权限检查
    if request.current_user['role'] != UserRole.ADMIN:
        if image_info.uploader != request.current_user['username']:
            return jsonify({'error': '无权访问此图片'}), 403

    if not os.path.exists(image_info.file_path):
        return jsonify({'error': '文件不存在'}), 404

    return send_file(image_info.file_path, mimetype=f'image/{image_info.format.lower()}')


@app.route('/api/images/<image_id>/thumbnail', methods=['GET'])
@user_or_admin_required
def get_thumbnail(image_id):
    """获取缩略图"""
    image_info = image_storage.get_image(image_id)

    if not image_info:
        return jsonify({'error': '图片不存在'}), 404

    # 权限检查
    if request.current_user['role'] != UserRole.ADMIN:
        if image_info.uploader != request.current_user['username']:
            return jsonify({'error': '无权访问此图片'}), 403

    if not image_info.thumbnail_path or not os.path.exists(image_info.thumbnail_path):
        return jsonify({'error': '缩略图不存在'}), 404

    return send_file(image_info.thumbnail_path, mimetype=f'image/{image_info.format.lower()}')


@app.route('/api/images/<image_id>', methods=['DELETE'])
@user_or_admin_required
def delete_image(image_id):
    """删除图片"""
    image_info = image_storage.get_image(image_id)

    if not image_info:
        return jsonify({'error': '图片不存在'}), 404

    # 权限检查：用户只能删除自己的图片，管理员可以删除所有
    if request.current_user['role'] != UserRole.ADMIN:
        if image_info.uploader != request.current_user['username']:
            return jsonify({'error': '无权删除此图片'}), 403

    hard_delete = request.args.get('hard', 'false').lower() == 'true'
    success = image_storage.delete_image(image_id, hard_delete)

    if success:
        auth_manager.log_operation(
            request.current_user['user_id'],
            'image_delete',
            image_id,
            {'hard_delete': hard_delete}
        )

    return jsonify({'success': success})


@app.route('/api/images/search', methods=['GET'])
@user_or_admin_required
def search_images():
    """搜索图片"""
    keyword = request.args.get('keyword', '')

    if not keyword:
        return jsonify({'error': '缺少搜索关键词'}), 400

    # 权限过滤
    if request.current_user['role'] == UserRole.ADMIN:
        uploader = request.args.get('uploader')
    else:
        uploader = request.current_user['username']

    images = image_storage.search_images(keyword, uploader)

    return jsonify({
        'success': True,
        'images': [img.to_dict() for img in images]
    })


# ==================== 操作日志API ====================

@app.route('/api/logs', methods=['GET'])
@admin_required
def get_logs():
    """获取操作日志（仅管理员）"""
    user_id = request.args.get('user_id')
    limit = int(request.args.get('limit', 100))

    logs = auth_manager.get_operation_logs(user_id, limit)

    return jsonify({
        'success': True,
        'logs': logs
    })


# ==================== 静态文件 ====================

@app.route('/')
def index():
    """主页"""
    return send_from_directory('static', 'index.html')


@app.route('/static/<path:filename>')
def static_files(filename):
    """静态文件"""
    return send_from_directory('static', filename)


# ==================== 错误处理 ====================

@app.errorhandler(404)
def not_found(error):
    return jsonify({'error': '资源不存在'}), 404


@app.errorhandler(500)
def internal_error(error):
    return jsonify({'error': '服务器内部错误'}), 500


@app.errorhandler(403)
def forbidden(error):
    return jsonify({'error': '权限不足'}), 403


@app.errorhandler(401)
def unauthorized(error):
    return jsonify({'error': '未授权'}), 401


# ==================== 启动 ====================

def create_static_dir():
    """创建静态文件目录"""
    static_dir = os.path.join(BASE_DIR, 'static')
    os.makedirs(static_dir, exist_ok=True)

    # 创建简单的HTML页面
    index_html = '''<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>设备发现与图片管理系统</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', sans-serif; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }
        .header { background: #2c3e50; color: white; padding: 20px; margin-bottom: 20px; }
        .header h1 { font-size: 24px; }
        .login-form { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; }
        .form-group input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .btn { padding: 10px 20px; background: #3498db; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .btn:hover { background: #2980b9; }
        .section { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
        .section h2 { margin-bottom: 15px; color: #2c3e50; }
        .device-list { list-style: none; }
        .device-item { padding: 10px; border-bottom: 1px solid #eee; }
        .status { padding: 5px 10px; border-radius: 3px; font-size: 12px; }
        .status.online { background: #27ae60; color: white; }
        .status.offline { background: #e74c3c; color: white; }
    </style>
</head>
<body>
    <div class="header">
        <h1>设备发现与图片管理系统</h1>
    </div>
    <div class="container">
        <div class="login-form">
            <h2>登录</h2>
            <div class="form-group">
                <label>用户名</label>
                <input type="text" id="username" value="admin">
            </div>
            <div class="form-group">
                <label>密码</label>
                <input type="password" id="password" value="admin123">
            </div>
            <button class="btn" onclick="login()">登录</button>
        </div>

        <div class="section" id="bluetooth-section">
            <h2>蓝牙设备</h2>
            <button class="btn" onclick="scanBluetooth()">扫描蓝牙设备</button>
            <ul class="device-list" id="bluetooth-list"></ul>
        </div>

        <div class="section" id="lan-section">
            <h2>局域网设备</h2>
            <button class="btn" onclick="scanLan()">扫描局域网</button>
            <ul class="device-list" id="lan-list"></ul>
        </div>

        <div class="section" id="images-section">
            <h2>图片管理</h2>
            <button class="btn" onclick="listImages()">查看图片</button>
            <div id="images-list"></div>
        </div>
    </div>

    <script>
        let token = '';

        async function login() {
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({username, password})
                });

                const data = await response.json();
                if (data.success) {
                    token = data.token;
                    alert('登录成功！');
                } else {
                    alert('登录失败: ' + data.error);
                }
            } catch (e) {
                alert('请求失败: ' + e.message);
            }
        }

        async function scanBluetooth() {
            if (!token) { alert('请先登录'); return; }

            try {
                const response = await fetch('/api/bluetooth/scan', {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({timeout: 5})
                });

                const data = await response.json();
                const list = document.getElementById('bluetooth-list');
                list.innerHTML = data.devices.map(d =>
                    '<li class="device-item">' + d.name + ' - ' + d.address +
                    ' <span class="status online">RSSI: ' + d.rssi + '</span></li>'
                ).join('');
            } catch (e) {
                alert('扫描失败: ' + e.message);
            }
        }

        async function scanLan() {
            if (!token) { alert('请先登录'); return; }

            try {
                const response = await fetch('/api/lan/scan', {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({})
                });

                const data = await response.json();
                const list = document.getElementById('lan-list');
                list.innerHTML = data.devices.map(d =>
                    '<li class="device-item">' + d.ip_address + ' - ' + d.mac_address +
                    ' (' + d.vendor + ') <span class="status online">' + d.status + '</span></li>'
                ).join('');
            } catch (e) {
                alert('扫描失败: ' + e.message);
            }
        }

        async function listImages() {
            if (!token) { alert('请先登录'); return; }

            try {
                const response = await fetch('/api/images', {
                    headers: {'Authorization': 'Bearer ' + token}
                });

                const data = await response.json();
                const list = document.getElementById('images-list');
                list.innerHTML = data.images.map(img =>
                    '<div style="margin:10px;padding:10px;border:1px solid #eee;">' +
                    '<strong>' + img.original_name + '</strong><br>' +
                    '大小: ' + (img.file_size / 1024).toFixed(2) + ' KB<br>' +
                    '上传者: ' + img.uploader + '<br>' +
                    '<img src="/api/images/' + img.id + '/thumbnail" style="max-width:200px;margin-top:10px;">' +
                    '</div>'
                ).join('');
            } catch (e) {
                alert('获取失败: ' + e.message);
            }
        }
    </script>
</body>
</html>'''

    with open(os.path.join(static_dir, 'index.html'), 'w', encoding='utf-8') as f:
        f.write(index_html)


if __name__ == '__main__':
    create_static_dir()

    port = int(os.environ.get('PORT', 5000))
    debug = os.environ.get('FLASK_DEBUG', 'false').lower() == 'true'

    logger.info(f"启动Web服务器: http://localhost:{port}")
    logger.info("默认管理员账户: admin / admin123")

    app.run(host='0.0.0.0', port=port, debug=debug)