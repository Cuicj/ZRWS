"""
数据传输模块
支持蓝牙和局域网设备间的数据传输
"""
import asyncio
import socket
import logging
import json
import struct
import os
from typing import Optional, Dict, Any, Callable, Union
from dataclasses import dataclass, asdict
from datetime import datetime
from pathlib import Path
import hashlib

try:
    from bleak import BleakClient
except ImportError:
    BleakClient = None

try:
    import aiohttp
    HAS_AIOHTTP = True
except ImportError:
    HAS_AIOHTTP = False

try:
    import requests
    HAS_REQUESTS = True
except ImportError:
    HAS_REQUESTS = False

logger = logging.getLogger(__name__)


@dataclass
class TransferResult:
    """传输结果"""
    success: bool
    data_size: int
    duration: float
    error: Optional[str] = None
    timestamp: str = None

    def __post_init__(self):
        if self.timestamp is None:
            self.timestamp = datetime.now().isoformat()

    def to_dict(self) -> Dict:
        return asdict(self)


@dataclass
class FileMetadata:
    """文件元数据"""
    filename: str
    size: int
    checksum: str
    file_type: str
    created_at: str = None

    def __post_init__(self):
        if self.created_at is None:
            self.created_at = datetime.now().isoformat()

    def to_dict(self) -> Dict:
        return asdict(self)

    def to_json(self) -> str:
        return json.dumps(self.to_dict())

    @classmethod
    def from_json(cls, json_str: str) -> 'FileMetadata':
        data = json.loads(json_str)
        return cls(**data)


class BluetoothTransfer:
    """蓝牙数据传输"""

    # 标准BLE特征UUID
    TX_CHARACTERISTIC_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
    RX_CHARACTERISTIC_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"

    # 数据包大小限制
    MAX_PACKET_SIZE = 20  # BLE默认MTU减去头部

    def __init__(self):
        self.client: Optional[BleakClient] = None
        self.connected = False

    async def connect(self, address: str) -> bool:
        """
        连接蓝牙设备

        Args:
            address: 设备地址

        Returns:
            是否连接成功
        """
        if BleakClient is None:
            logger.error("bleak库未安装")
            return False

        try:
            self.client = BleakClient(address)
            await self.client.connect()
            self.connected = True
            logger.info(f"已连接到蓝牙设备: {address}")
            return True
        except Exception as e:
            logger.error(f"连接蓝牙设备失败: {e}")
            self.connected = False
            return False

    async def disconnect(self):
        """断开连接"""
        if self.client and self.connected:
            try:
                await self.client.disconnect()
                logger.info("蓝牙设备已断开")
            except Exception as e:
                logger.error(f"断开连接失败: {e}")
            finally:
                self.connected = False
                self.client = None

    async def send_data(self, data: bytes, characteristic_uuid: str = None) -> TransferResult:
        """
        发送数据

        Args:
            data: 要发送的数据
            characteristic_uuid: 特征UUID

        Returns:
            传输结果
        """
        if not self.connected or not self.client:
            return TransferResult(success=False, data_size=0, duration=0, error="未连接设备")

        uuid = characteristic_uuid or self.TX_CHARACTERISTIC_UUID
        start_time = datetime.now()

        try:
            # 分包发送
            total_packets = (len(data) + self.MAX_PACKET_SIZE - 1) // self.MAX_PACKET_SIZE

            for i in range(total_packets):
                start = i * self.MAX_PACKET_SIZE
                end = min(start + self.MAX_PACKET_SIZE, len(data))
                packet = data[start:end]
                await self.client.write_gatt_char(uuid, packet)
                await asyncio.sleep(0.01)  # 避免发送过快

            duration = (datetime.now() - start_time).total_seconds()
            return TransferResult(
                success=True,
                data_size=len(data),
                duration=duration
            )

        except Exception as e:
            logger.error(f"发送数据失败: {e}")
            duration = (datetime.now() - start_time).total_seconds()
            return TransferResult(
                success=False,
                data_size=0,
                duration=duration,
                error=str(e)
            )

    async def receive_data(self, characteristic_uuid: str = None, timeout: float = 30.0) -> bytes:
        """
        接收数据

        Args:
            characteristic_uuid: 特征UUID
            timeout: 超时时间

        Returns:
            接收到的数据
        """
        if not self.connected or not self.client:
            raise ConnectionError("未连接设备")

        uuid = characteristic_uuid or self.RX_CHARACTERISTIC_UUID
        received_data = bytearray()
        received_event = asyncio.Event()

        def notification_handler(sender, data):
            received_data.extend(data)
            # 检查是否接收完成（根据协议判断）
            if len(data) < self.MAX_PACKET_SIZE:
                received_event.set()

        try:
            await self.client.start_notify(uuid, notification_handler)

            # 等待接收完成或超时
            await asyncio.wait_for(received_event.wait(), timeout=timeout)

            await self.client.stop_notify(uuid)
            return bytes(received_data)

        except asyncio.TimeoutError:
            logger.warning("接收数据超时")
            return bytes(received_data)
        except Exception as e:
            logger.error(f"接收数据失败: {e}")
            raise

    async def send_file(self, file_path: str, characteristic_uuid: str = None) -> TransferResult:
        """
        发送文件

        Args:
            file_path: 文件路径
            characteristic_uuid: 特征UUID

        Returns:
            传输结果
        """
        if not os.path.exists(file_path):
            return TransferResult(success=False, data_size=0, duration=0, error="文件不存在")

        try:
            # 读取文件
            with open(file_path, 'rb') as f:
                file_data = f.read()

            # 计算校验和
            checksum = hashlib.md5(file_data).hexdigest()

            # 创建元数据
            metadata = FileMetadata(
                filename=os.path.basename(file_path),
                size=len(file_data),
                checksum=checksum,
                file_type=os.path.splitext(file_path)[1]
            )

            # 发送元数据
            meta_result = await self.send_data(metadata.to_json().encode(), characteristic_uuid)
            if not meta_result.success:
                return meta_result

            # 发送文件数据
            data_result = await self.send_data(file_data, characteristic_uuid)
            return data_result

        except Exception as e:
            logger.error(f"发送文件失败: {e}")
            return TransferResult(success=False, data_size=0, duration=0, error=str(e))


class TCPTransfer:
    """TCP数据传输"""

    DEFAULT_PORT = 9527
    BUFFER_SIZE = 4096

    def __init__(self, host: str = '0.0.0.0', port: int = None):
        self.host = host
        self.port = port or self.DEFAULT_PORT
        self.server_socket: Optional[socket.socket] = None
        self.client_socket: Optional[socket.socket] = None
        self.is_server = False

    def start_server(self, callback: Callable[[bytes, tuple], bytes] = None):
        """
        启动TCP服务器

        Args:
            callback: 数据接收回调函数
        """
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.server_socket.bind((self.host, self.port))
        self.server_socket.listen(5)
        self.is_server = True

        logger.info(f"TCP服务器启动: {self.host}:{self.port}")

        while self.is_server:
            try:
                client_socket, address = self.server_socket.accept()
                logger.info(f"客户端连接: {address}")

                # 处理客户端连接
                self._handle_client(client_socket, address, callback)

            except Exception as e:
                if self.is_server:
                    logger.error(f"服务器错误: {e}")

    def _handle_client(self, client_socket: socket.socket, address: tuple,
                       callback: Callable[[bytes, tuple], bytes] = None):
        """处理客户端连接"""
        try:
            while True:
                # 接收数据长度
                length_data = client_socket.recv(4)
                if not length_data:
                    break

                data_length = struct.unpack('!I', length_data)[0]

                # 接收数据
                received_data = bytearray()
                while len(received_data) < data_length:
                    chunk = client_socket.recv(min(self.BUFFER_SIZE, data_length - len(received_data)))
                    if not chunk:
                        break
                    received_data.extend(chunk)

                if callback:
                    # 调用回调处理数据
                    response = callback(bytes(received_data), address)
                    if response:
                        self._send_response(client_socket, response)
                else:
                    # 默认响应
                    self._send_response(client_socket, b'ACK')

        except Exception as e:
            logger.error(f"处理客户端错误: {e}")
        finally:
            client_socket.close()
            logger.info(f"客户端断开: {address}")

    def _send_response(self, socket_conn: socket.socket, data: bytes):
        """发送响应"""
        length = struct.pack('!I', len(data))
        socket_conn.sendall(length + data)

    def connect(self, host: str, port: int = None) -> bool:
        """
        连接到服务器

        Args:
            host: 服务器地址
            port: 服务器端口

        Returns:
            是否连接成功
        """
        try:
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket.connect((host, port or self.port))
            logger.info(f"已连接到服务器: {host}:{port or self.port}")
            return True
        except Exception as e:
            logger.error(f"连接服务器失败: {e}")
            return False

    def disconnect(self):
        """断开连接"""
        if self.client_socket:
            try:
                self.client_socket.close()
            except Exception:
                pass
            finally:
                self.client_socket = None

        if self.server_socket:
            self.is_server = False
            try:
                self.server_socket.close()
            except Exception:
                pass
            finally:
                self.server_socket = None

    def send_data(self, data: bytes) -> TransferResult:
        """
        发送数据

        Args:
            data: 要发送的数据

        Returns:
            传输结果
        """
        if not self.client_socket:
            return TransferResult(success=False, data_size=0, duration=0, error="未连接服务器")

        start_time = datetime.now()

        try:
            # 发送数据长度
            length = struct.pack('!I', len(data))
            self.client_socket.sendall(length)

            # 发送数据
            self.client_socket.sendall(data)

            # 接收响应
            response_length = self.client_socket.recv(4)
            if response_length:
                resp_len = struct.unpack('!I', response_length)[0]
                response = self.client_socket.recv(resp_len)
                logger.debug(f"收到响应: {response}")

            duration = (datetime.now() - start_time).total_seconds()
            return TransferResult(
                success=True,
                data_size=len(data),
                duration=duration
            )

        except Exception as e:
            logger.error(f"发送数据失败: {e}")
            duration = (datetime.now() - start_time).total_seconds()
            return TransferResult(
                success=False,
                data_size=0,
                duration=duration,
                error=str(e)
            )

    def send_file(self, file_path: str) -> TransferResult:
        """
        发送文件

        Args:
            file_path: 文件路径

        Returns:
            传输结果
        """
        if not os.path.exists(file_path):
            return TransferResult(success=False, data_size=0, duration=0, error="文件不存在")

        try:
            with open(file_path, 'rb') as f:
                file_data = f.read()

            # 计算校验和
            checksum = hashlib.md5(file_data).hexdigest()

            # 创建元数据
            metadata = FileMetadata(
                filename=os.path.basename(file_path),
                size=len(file_data),
                checksum=checksum,
                file_type=os.path.splitext(file_path)[1]
            )

            # 发送元数据
            meta_json = metadata.to_json()
            meta_data = b'META:' + meta_json.encode()
            meta_result = self.send_data(meta_data)

            if not meta_result.success:
                return meta_result

            # 发送文件数据
            file_result = self.send_file_data(file_data)
            return file_result

        except Exception as e:
            logger.error(f"发送文件失败: {e}")
            return TransferResult(success=False, data_size=0, duration=0, error=str(e))

    def send_file_data(self, data: bytes) -> TransferResult:
        """发送文件数据"""
        file_data = b'FILE:' + data
        return self.send_data(file_data)


class HTTPTransfer:
    """HTTP数据传输"""

    def __init__(self, base_url: str = None, timeout: int = 30):
        self.base_url = base_url
        self.timeout = timeout

    def upload_file(self, file_path: str, url: str = None, headers: Dict = None) -> TransferResult:
        """
        上传文件

        Args:
            file_path: 文件路径
            url: 上传URL
            headers: 请求头

        Returns:
            传输结果
        """
        if not HAS_REQUESTS:
            return TransferResult(success=False, data_size=0, duration=0, error="requests库未安装")

        if not os.path.exists(file_path):
            return TransferResult(success=False, data_size=0, duration=0, error="文件不存在")

        upload_url = url or f"{self.base_url}/upload"
        start_time = datetime.now()

        try:
            with open(file_path, 'rb') as f:
                files = {'file': (os.path.basename(file_path), f)}
                response = requests.post(upload_url, files=files, headers=headers, timeout=self.timeout)

            duration = (datetime.now() - start_time).total_seconds()

            if response.status_code == 200:
                return TransferResult(
                    success=True,
                    data_size=os.path.getsize(file_path),
                    duration=duration
                )
            else:
                return TransferResult(
                    success=False,
                    data_size=0,
                    duration=duration,
                    error=f"HTTP {response.status_code}: {response.text}"
                )

        except Exception as e:
            duration = (datetime.now() - start_time).total_seconds()
            return TransferResult(
                success=False,
                data_size=0,
                duration=duration,
                error=str(e)
            )

    def download_file(self, url: str, save_path: str, headers: Dict = None) -> TransferResult:
        """
        下载文件

        Args:
            url: 下载URL
            save_path: 保存路径
            headers: 请求头

        Returns:
            传输结果
        """
        if not HAS_REQUESTS:
            return TransferResult(success=False, data_size=0, duration=0, error="requests库未安装")

        start_time = datetime.now()

        try:
            response = requests.get(url, headers=headers, timeout=self.timeout, stream=True)

            if response.status_code != 200:
                duration = (datetime.now() - start_time).total_seconds()
                return TransferResult(
                    success=False,
                    data_size=0,
                    duration=duration,
                    error=f"HTTP {response.status_code}"
                )

            # 确保目录存在
            os.makedirs(os.path.dirname(save_path), exist_ok=True)

            # 下载文件
            total_size = 0
            with open(save_path, 'wb') as f:
                for chunk in response.iter_content(chunk_size=8192):
                    if chunk:
                        f.write(chunk)
                        total_size += len(chunk)

            duration = (datetime.now() - start_time).total_seconds()
            return TransferResult(
                success=True,
                data_size=total_size,
                duration=duration
            )

        except Exception as e:
            duration = (datetime.now() - start_time).total_seconds()
            return TransferResult(
                success=False,
                data_size=0,
                duration=duration,
                error=str(e)
            )

    async def upload_file_async(self, file_path: str, url: str = None,
                                 headers: Dict = None) -> TransferResult:
        """异步上传文件"""
        if not HAS_AIOHTTP:
            return TransferResult(success=False, data_size=0, duration=0, error="aiohttp库未安装")

        if not os.path.exists(file_path):
            return TransferResult(success=False, data_size=0, duration=0, error="文件不存在")

        upload_url = url or f"{self.base_url}/upload"
        start_time = datetime.now()

        try:
            async with aiohttp.ClientSession() as session:
                with open(file_path, 'rb') as f:
                    data = aiohttp.FormData()
                    data.add_field('file', f, filename=os.path.basename(file_path))

                    async with session.post(upload_url, data=data, headers=headers,
                                           timeout=aiohttp.ClientTimeout(total=self.timeout)) as response:
                        duration = (datetime.now() - start_time).total_seconds()

                        if response.status == 200:
                            return TransferResult(
                                success=True,
                                data_size=os.path.getsize(file_path),
                                duration=duration
                            )
                        else:
                            text = await response.text()
                            return TransferResult(
                                success=False,
                                data_size=0,
                                duration=duration,
                                error=f"HTTP {response.status}: {text}"
                            )

        except Exception as e:
            duration = (datetime.now() - start_time).total_seconds()
            return TransferResult(
                success=False,
                data_size=0,
                duration=duration,
                error=str(e)
            )

    async def download_file_async(self, url: str, save_path: str,
                                   headers: Dict = None) -> TransferResult:
        """异步下载文件"""
        if not HAS_AIOHTTP:
            return TransferResult(success=False, data_size=0, duration=0, error="aiohttp库未安装")

        start_time = datetime.now()

        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(url, headers=headers,
                                       timeout=aiohttp.ClientTimeout(total=self.timeout)) as response:
                    if response.status != 200:
                        duration = (datetime.now() - start_time).total_seconds()
                        return TransferResult(
                            success=False,
                            data_size=0,
                            duration=duration,
                            error=f"HTTP {response.status}"
                        )

                    # 确保目录存在
                    os.makedirs(os.path.dirname(save_path), exist_ok=True)

                    # 下载文件
                    total_size = 0
                    with open(save_path, 'wb') as f:
                        async for chunk in response.content.iter_chunked(8192):
                            f.write(chunk)
                            total_size += len(chunk)

                    duration = (datetime.now() - start_time).total_seconds()
                    return TransferResult(
                        success=True,
                        data_size=total_size,
                        duration=duration
                    )

        except Exception as e:
            duration = (datetime.now() - start_time).total_seconds()
            return TransferResult(
                success=False,
                data_size=0,
                duration=duration,
                error=str(e)
            )


class DataTransferManager:
    """数据传输管理器"""

    def __init__(self):
        self.bluetooth = BluetoothTransfer()
        self.tcp = TCPTransfer()
        self.http = HTTPTransfer()

    async def send_to_device(self, device_address: str, data: bytes,
                             protocol: str = 'tcp', port: int = None) -> TransferResult:
        """
        发送数据到设备

        Args:
            device_address: 设备地址
            data: 数据
            protocol: 协议类型 ('bluetooth', 'tcp')
            port: TCP端口

        Returns:
            传输结果
        """
        if protocol == 'bluetooth':
            if not self.bluetooth.connected:
                await self.bluetooth.connect(device_address)
            return await self.bluetooth.send_data(data)

        elif protocol == 'tcp':
            if not self.tcp.client_socket:
                self.tcp.connect(device_address, port)
            return self.tcp.send_data(data)

        else:
            return TransferResult(success=False, data_size=0, duration=0, error="不支持的协议")

    async def send_file_to_device(self, device_address: str, file_path: str,
                                   protocol: str = 'tcp', port: int = None) -> TransferResult:
        """
        发送文件到设备

        Args:
            device_address: 设备地址
            file_path: 文件路径
            protocol: 协议类型
            port: TCP端口

        Returns:
            传输结果
        """
        if protocol == 'bluetooth':
            if not self.bluetooth.connected:
                await self.bluetooth.connect(device_address)
            return await self.bluetooth.send_file(file_path)

        elif protocol == 'tcp':
            if not self.tcp.client_socket:
                self.tcp.connect(device_address, port)
            return self.tcp.send_file(file_path)

        else:
            return TransferResult(success=False, data_size=0, duration=0, error="不支持的协议")

    def cleanup(self):
        """清理资源"""
        if self.bluetooth.connected:
            asyncio.create_task(self.bluetooth.disconnect())
        self.tcp.disconnect()


if __name__ == "__main__":
    # 测试代码
    logging.basicConfig(level=logging.INFO)

    # 测试TCP传输
    print("测试TCP传输...")
    tcp = TCPTransfer(port=9527)

    # 测试HTTP传输
    print("\n测试HTTP传输...")
    http = HTTPTransfer()
    # 需要实际的服务器URL进行测试