"""
蓝牙设备发现模块
支持BLE设备扫描、连接和数据传输
"""
import asyncio
import logging
from datetime import datetime
from typing import List, Dict, Optional, Callable
from dataclasses import dataclass, asdict

try:
    from bleak import BleakClient, BleakScanner
    from bleak.backends.device import BLEDevice
except ImportError:
    BleakClient = None
    BleakScanner = None
    BLEDevice = None

logger = logging.getLogger(__name__)


@dataclass
class BluetoothDevice:
    """蓝牙设备信息"""
    address: str
    name: str
    rssi: int
    device_type: str = "Unknown"
    manufacturer_data: Dict = None
    service_uuids: List[str] = None
    discovered_at: str = None

    def __post_init__(self):
        if self.manufacturer_data is None:
            self.manufacturer_data = {}
        if self.service_uuids is None:
            self.service_uuids = []
        if self.discovered_at is None:
            self.discovered_at = datetime.now().isoformat()

    def to_dict(self) -> Dict:
        return asdict(self)


class BluetoothDiscovery:
    """蓝牙设备发现类"""

    def __init__(self):
        self.scanner: Optional[BleakScanner] = None
        self.discovered_devices: Dict[str, BluetoothDevice] = {}
        self._scanning = False
        self._detection_callback: Optional[Callable] = None

    async def start_scan(self, timeout: float = 10.0, callback: Callable = None) -> List[BluetoothDevice]:
        """
        开始扫描蓝牙设备

        Args:
            timeout: 扫描超时时间（秒）
            callback: 设备发现回调函数

        Returns:
            发现的设备列表
        """
        if BleakScanner is None:
            logger.error("bleak库未安装，请运行: pip install bleak")
            return []

        self._scanning = True
        self._detection_callback = callback
        self.discovered_devices.clear()

        logger.info(f"开始扫描蓝牙设备，超时: {timeout}秒")

        try:
            self.scanner = BleakScanner(detection_callback=self._on_device_detected)

            async with self.scanner:
                await asyncio.wait_for(
                    self._scan_loop(),
                    timeout=timeout
                )
        except asyncio.TimeoutError:
            logger.info("扫描超时，停止扫描")
        except Exception as e:
            logger.error(f"扫描蓝牙设备时发生错误: {e}")
        finally:
            self._scanning = False

        return list(self.discovered_devices.values())

    async def _scan_loop(self):
        """扫描循环"""
        while self._scanning:
            await asyncio.sleep(0.1)

    def _on_device_detected(self, device: BLEDevice, advertisement_data):
        """设备检测回调"""
        try:
            # 解析厂商数据
            manufacturer_data = {}
            if advertisement_data.manufacturer_data:
                for key, value in advertisement_data.manufacturer_data.items():
                    manufacturer_data[str(key)] = value.hex() if isinstance(value, bytes) else str(value)

            # 创建设备对象
            bt_device = BluetoothDevice(
                address=device.address,
                name=device.name or "Unknown",
                rssi=advertisement_data.rssi or device.rssi,
                device_type=self._get_device_type(advertisement_data),
                manufacturer_data=manufacturer_data,
                service_uuids=[str(uuid) for uuid in (advertisement_data.service_uuids or [])]
            )

            # 更新已发现设备
            self.discovered_devices[device.address] = bt_device

            # 调用回调
            if self._detection_callback:
                self._detection_callback(bt_device)

            logger.debug(f"发现设备: {bt_device.name} ({bt_device.address}) RSSI: {bt_device.rssi}")

        except Exception as e:
            logger.error(f"处理设备信息时发生错误: {e}")

    def _get_device_type(self, advertisement_data) -> str:
        """根据广播数据判断设备类型"""
        if not advertisement_data:
            return "Unknown"

        service_uuids = advertisement_data.service_uuids or []

        # 常见服务UUID
        uuid_types = {
            "00001800-0000-1000-8000-00805f9b34fb": "Generic Access",
            "00001801-0000-1000-8000-00805f9b34fb": "Generic Attribute",
            "0000180a-0000-1000-8000-00805f9b34fb": "Device Information",
            "0000180f-0000-1000-8000-00805f9b34fb": "Battery Service",
            "00001812-0000-1000-8000-00805f9b34fb": "Human Interface Device",
            "0000180d-0000-1000-8000-00805f9b34fb": "Heart Rate",
            "00001810-0000-1000-8000-00805f9b34fb": "Blood Pressure",
            "0000f000-1212-efde-1523-785feabcd123": "Texas Instruments",
            "0000fe9f-0000-1000-8000-00805f9b34fb": "Google LLC",
            "0000fd6f-0000-1000-8000-00805f9b34fb": "Apple Inc.",
        }

        for uuid in service_uuids:
            uuid_lower = str(uuid).lower()
            if uuid_lower in uuid_types:
                return uuid_types[uuid_lower]

        # 根据厂商数据判断
        if advertisement_data.manufacturer_data:
            # Apple
            if 76 in advertisement_data.manufacturer_data:
                return "Apple Device"
            # Microsoft
            if 6 in advertisement_data.manufacturer_data:
                return "Microsoft Device"

        return "BLE Device"

    def stop_scan(self):
        """停止扫描"""
        self._scanning = False

    async def connect_device(self, address: str) -> Optional[BleakClient]:
        """
        连接到指定设备

        Args:
            address: 设备地址

        Returns:
            BleakClient实例或None
        """
        if BleakClient is None:
            logger.error("bleak库未安装")
            return None

        try:
            client = BleakClient(address)
            await client.connect()
            logger.info(f"已连接到设备: {address}")
            return client
        except Exception as e:
            logger.error(f"连接设备失败: {e}")
            return None

    async def disconnect_device(self, client: BleakClient):
        """断开设备连接"""
        try:
            await client.disconnect()
            logger.info("设备已断开连接")
        except Exception as e:
            logger.error(f"断开连接失败: {e}")

    async def read_characteristic(self, client: BleakClient, uuid: str) -> Optional[bytes]:
        """读取特征值"""
        try:
            data = await client.read_gatt_char(uuid)
            logger.debug(f"读取特征值 {uuid}: {data.hex()}")
            return data
        except Exception as e:
            logger.error(f"读取特征值失败: {e}")
            return None

    async def write_characteristic(self, client: BleakClient, uuid: str, data: bytes) -> bool:
        """写入特征值"""
        try:
            await client.write_gatt_char(uuid, data)
            logger.debug(f"写入特征值 {uuid}: {data.hex()}")
            return True
        except Exception as e:
            logger.error(f"写入特征值失败: {e}")
            return False

    async def get_services(self, client: BleakClient) -> List[Dict]:
        """获取设备服务列表"""
        services = []
        try:
            for service in client.services:
                service_info = {
                    "uuid": str(service.uuid),
                    "description": service.description,
                    "characteristics": []
                }
                for char in service.characteristics:
                    char_info = {
                        "uuid": str(char.uuid),
                        "description": char.description,
                        "properties": char.properties
                    }
                    service_info["characteristics"].append(char_info)
                services.append(service_info)
        except Exception as e:
            logger.error(f"获取服务列表失败: {e}")
        return services

    def get_discovered_devices(self) -> List[BluetoothDevice]:
        """获取已发现的设备列表"""
        return list(self.discovered_devices.values())

    def get_device_by_address(self, address: str) -> Optional[BluetoothDevice]:
        """根据地址获取设备"""
        return self.discovered_devices.get(address)


# 同步接口封装
def scan_bluetooth_devices(timeout: float = 10.0, callback: Callable = None) -> List[Dict]:
    """
    同步扫描蓝牙设备接口

    Args:
        timeout: 扫描超时时间
        callback: 设备发现回调

    Returns:
        设备字典列表
    """
    discovery = BluetoothDiscovery()

    async def _scan():
        devices = await discovery.start_scan(timeout, callback)
        return [d.to_dict() for d in devices]

    try:
        loop = asyncio.get_event_loop()
    except RuntimeError:
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)

    return loop.run_until_complete(_scan())


if __name__ == "__main__":
    # 测试代码
    logging.basicConfig(level=logging.INFO)

    def on_device_found(device: BluetoothDevice):
        print(f"发现设备: {device.name} - {device.address} (RSSI: {device.rssi})")

    devices = scan_bluetooth_devices(timeout=5.0, callback=on_device_found)
    print(f"\n共发现 {len(devices)} 个设备:")
    for d in devices:
        print(f"  - {d['name']}: {d['address']} ({d['device_type']})")