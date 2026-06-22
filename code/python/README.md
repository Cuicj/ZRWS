# 智壤卫士 - 设备发现与图片管理系统

## 功能概述

本系统提供以下功能：

1. **蓝牙设备发现** - 扫描BLE蓝牙设备，获取设备信息
2. **局域网设备发现** - 扫描局域网IP设备，识别设备类型
3. **数据传输** - 支持蓝牙和TCP协议的数据传输
4. **图片存储** - 图片上传、管理、缩略图生成
5. **权限控制** - 用户+管理员级别的权限验证

## 安装依赖

```bash
pip install -r requirements.txt
```

## 使用方法

### 1. 启动Web服务器

```bash
python app.py
```

服务器将在 `http://localhost:5000` 启动

### 2. 默认账户

- 管理员: `admin` / `admin123`
- 首次登录后请修改密码

### 3. API接口

#### 认证接口

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户注销
- `GET /api/auth/verify` - 验证Token
- `POST /api/auth/register` - 注册新用户（管理员）
- `GET /api/auth/users` - 列出用户（管理员）

#### 蓝牙设备接口

- `POST /api/bluetooth/scan` - 扫描蓝牙设备
- `GET /api/bluetooth/devices` - 获取已发现设备
- `POST /api/bluetooth/connect/<address>` - 连接设备

#### 局域网设备接口

- `POST /api/lan/scan` - 扫描局域网
- `GET /api/lan/devices` - 获取已发现设备
- `GET /api/lan/ports/<ip>` - 扫描端口

#### 图片接口

- `POST /api/images/upload` - 上传图片
- `GET /api/images` - 列出图片
- `GET /api/images/<id>` - 获取图片详情
- `GET /api/images/<id>/data` - 获取图片文件
- `GET /api/images/<id>/thumbnail` - 获取缩略图
- `DELETE /api/images/<id>` - 删除图片
- `GET /api/images/search` - 搜索图片

#### 数据传输接口

- `POST /api/transfer/send` - 发送数据
- `POST /api/transfer/file` - 发送文件

#### 操作日志接口

- `GET /api/logs` - 获取操作日志（管理员）

## 权限说明

- **管理员(admin)**: 可以查看所有数据、管理用户、查看日志
- **用户(user)**: 可以扫描设备、上传图片、查看自己的图片
- **访客(guest)**: 无权限

## 模块说明

### bluetooth_discovery.py
蓝牙设备发现模块，使用bleak库实现BLE设备扫描

### lan_discovery.py
局域网设备发现模块，支持ARP扫描、端口扫描、设备识别

### data_transfer.py
数据传输模块，支持蓝牙和TCP协议的数据传输

### image_storage.py
图片存储模块，支持图片保存、缩略图生成、SQLite数据库管理

### auth.py
权限控制模块，支持JWT认证、用户管理、操作日志

### app.py
Web应用主模块，提供REST API接口

## 注意事项

1. 蓝牙扫描需要系统支持BLE
2. 局域网扫描可能需要管理员权限
3. 生产环境请修改JWT_SECRET和默认密码
4. 图片存储目录默认在 `data/images`

## 开发环境

- Python 3.8+
- Flask 3.0+
- SQLite 3