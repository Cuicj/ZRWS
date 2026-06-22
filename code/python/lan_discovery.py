"""
局域网IP设备发现模块
支持ARP扫描、端口扫描、设备识别
"""
import asyncio
import socket
import logging
import platform
import subprocess
import ipaddress
from datetime import datetime
from typing import List, Dict, Optional, Tuple
from dataclasses import dataclass, asdict
from concurrent.futures import ThreadPoolExecutor
import struct

try:
    import nmap
    HAS_NMAP = True
except ImportError:
    HAS_NMAP = False

try:
    from scapy.all import ARP, Ether, srp, conf
    HAS_SCAPY = True
except ImportError:
    HAS_SCAPY = False

logger = logging.getLogger(__name__)


@dataclass
class NetworkDevice:
    """网络设备信息"""
    ip_address: str
    mac_address: str
    hostname: str = "Unknown"
    vendor: str = "Unknown"
    open_ports: List[int] = None
    os_type: str = "Unknown"
    device_type: str = "Unknown"
    status: str = "unknown"
    discovered_at: str = None
    last_seen: str = None

    def __post_init__(self):
        if self.open_ports is None:
            self.open_ports = []
        if self.discovered_at is None:
            self.discovered_at = datetime.now().isoformat()
        if self.last_seen is None:
            self.last_seen = datetime.now().isoformat()

    def to_dict(self) -> Dict:
        return asdict(self)


class LANDiscovery:
    """局域网设备发现类"""

    # 常见端口及其服务
    COMMON_PORTS = {
        21: "FTP",
        22: "SSH",
        23: "Telnet",
        25: "SMTP",
        53: "DNS",
        80: "HTTP",
        110: "POP3",
        143: "IMAP",
        443: "HTTPS",
        445: "SMB",
        993: "IMAPS",
        995: "POP3S",
        1433: "MSSQL",
        1521: "Oracle",
        3306: "MySQL",
        3389: "RDP",
        5432: "PostgreSQL",
        5900: "VNC",
        6379: "Redis",
        8080: "HTTP-Alt",
        8443: "HTTPS-Alt",
        27017: "MongoDB"
    }

    # MAC地址前缀对应的厂商
    VENDOR_PREFIXES = {
        "00:0C:29": "VMware",
        "00:50:56": "VMware",
        "00:1A:11": "Intel",
        "00:1B:21": "Intel",
        "00:1E:8C": "Intel",
        "00:25:00": "Intel",
        "00:26:B9": "Apple",
        "00:03:93": "Apple",
        "00:05:02": "Apple",
        "00:0A:27": "Apple",
        "00:0D:93": "Apple",
        "00:11:24": "Apple",
        "00:14:51": "Apple",
        "00:16:CB": "Apple",
        "00:17:F2": "Apple",
        "00:19:E3": "Apple",
        "00:1B:63": "Apple",
        "00:1C:B3": "Apple",
        "00:1D:4F": "Apple",
        "00:1E:52": "Apple",
        "00:1E:C2": "Apple",
        "00:21:E9": "Apple",
        "00:22:41": "Apple",
        "00:23:12": "Apple",
        "00:23:32": "Apple",
        "00:23:6C": "Apple",
        "00:23:DF": "Apple",
        "00:24:36": "Apple",
        "00:25:00": "Apple",
        "00:25:4B": "Apple",
        "00:25:BC": "Apple",
        "00:26:08": "Apple",
        "00:26:4A": "Apple",
        "00:26:B0": "Apple",
        "00:26:BB": "Apple",
        "00:30:65": "Apple",
        "00:50:E4": "Apple",
        "00:A0:40": "Apple",
        "04:0C:CE": "Apple",
        "04:1E:64": "Apple",
        "08:00:07": "Apple",
        "08:66:98": "Apple",
        "08:70:45": "Apple",
        "10:41:7F": "Apple",
        "10:9A:DD": "Apple",
        "10:DD:B1": "Apple",
        "14:10:9F": "Apple",
        "14:20:5E": "Apple",
        "14:5A:05": "Apple",
        "14:8F:21": "Apple",
        "14:99:E2": "Apple",
        "18:34:51": "Apple",
        "18:E7:F4": "Apple",
        "1C:1A:C0": "Apple",
        "1C:AB:A7": "Apple",
        "20:3C:AE": "Apple",
        "20:7C:8F": "Apple",
        "20:AB:37": "Apple",
        "24:A0:74": "Apple",
        "24:A4:3C": "Apple",
        "24:CF:DB": "Apple",
        "28:0B:5C": "Apple",
        "28:37:37": "Apple",
        "28:5A:EB": "Apple",
        "28:6A:B8": "Apple",
        "28:CF:DA": "Apple",
        "28:E1:2C": "Apple",
        "28:E7:CF": "Apple",
        "2C:33:11": "Apple",
        "2C:56:DC": "Apple",
        "2C:9B:F4": "Apple",
        "30:10:E4": "Apple",
        "30:35:AD": "Apple",
        "30:90:AB": "Apple",
        "34:12:F9": "Apple",
        "34:36:7B": "Apple",
        "34:A3:95": "Apple",
        "34:C0:59": "Apple",
        "34:E2:FD": "Apple",
        "38:0F:4A": "Apple",
        "38:48:4C": "Apple",
        "38:C9:86": "Apple",
        "3C:07:54": "Apple",
        "3C:15:B2": "Apple",
        "3C:2E:F9": "Apple",
        "3C:8B:FE": "Apple",
        "3C:A5:34": "Apple",
        "3C:A8:2A": "Apple",
        "3C:AB:8E": "Apple",
        "40:3C:FC": "Apple",
        "40:4D:7F": "Apple",
        "40:6C:8F": "Apple",
        "40:9F:38": "Apple",
        "40:A6:D9": "Apple",
        "40:B3:CD": "Apple",
        "40:D3:2D": "Apple",
        "44:00:10": "Apple",
        "44:2A:60": "Apple",
        "44:4B:DB": "Apple",
        "44:59:E3": "Apple",
        "44:D8:84": "Apple",
        "44:FB:42": "Apple",
        "48:43:7C": "Apple",
        "48:A1:95": "Apple",
        "48:B6:20": "Apple",
        "48:D7:05": "Apple",
        "4C:57:CA": "Apple",
        "4C:7C:5F": "Apple",
        "4C:8D:79": "Apple",
        "50:32:37": "Apple",
        "50:7A:55": "Apple",
        "50:BC:96": "Apple",
        "50:ED:3C": "Apple",
        "54:26:96": "Apple",
        "54:4E:90": "Apple",
        "54:72:4F": "Apple",
        "54:80:1D": "Apple",
        "54:9F:13": "Apple",
        "58:1F:AA": "Apple",
        "58:40:4E": "Apple",
        "58:55:CA": "Apple",
        "58:B0:35": "Apple",
        "5C:95:AE": "Apple",
        "5C:F8:21": "Apple",
        "60:03:08": "Apple",
        "60:33:4B": "Apple",
        "60:92:4A": "Apple",
        "60:C5:47": "Apple",
        "60:F8:1D": "Apple",
        "60:FA:CD": "Apple",
        "60:FB:42": "Apple",
        "64:20:0C": "Apple",
        "64:4B:F0": "Apple",
        "64:76:86": "Apple",
        "64:9A:BE": "Apple",
        "64:A3:CB": "Apple",
        "64:B0:A6": "Apple",
        "64:B9:E8": "Apple",
        "64:E6:82": "Apple",
        "68:04:49": "Apple",
        "68:08:D6": "Apple",
        "68:27:37": "Apple",
        "68:5B:35": "Apple",
        "68:64:4B": "Apple",
        "68:9C:70": "Apple",
        "68:A8:6D": "Apple",
        "68:DB:F5": "Apple",
        "6C:19:C0": "Apple",
        "6C:3B:E5": "Apple",
        "6C:40:08": "Apple",
        "6C:4D:73": "Apple",
        "6C:57:34": "Apple",
        "6C:70:9F": "Apple",
        "6C:94:66": "Apple",
        "6C:AB:31": "Apple",
        "70:11:24": "Apple",
        "70:3E:AC": "Apple",
        "70:48:0F": "Apple",
        "70:56:81": "Apple",
        "70:73:CB": "Apple",
        "70:85:C7": "Apple",
        "70:CD:60": "Apple",
        "70:DE:E2": "Apple",
        "74:81:14": "Apple",
        "74:9A:AF": "Apple",
        "74:E1:B6": "Apple",
        "74:E2:F5": "Apple",
        "78:24:AF": "Apple",
        "78:31:C1": "Apple",
        "78:4F:43": "Apple",
        "78:7B:8A": "Apple",
        "78:A3:E4": "Apple",
        "78:CA:39": "Apple",
        "7C:01:91": "Apple",
        "7C:11:BE": "Apple",
        "7C:6D:62": "Apple",
        "7C:D1:C3": "Apple",
        "7C:F2:6E": "Apple",
        "80:00:0B": "Apple",
        "80:3E:BD": "Apple",
        "80:58:F8": "Apple",
        "80:92:9F": "Apple",
        "80:BE:05": "Apple",
        "80:E8:2C": "Apple",
        "84:38:35": "Apple",
        "84:7C:00": "Apple",
        "84:89:AD": "Apple",
        "84:FC:FE": "Apple",
        "88:1D:FC": "Apple",
        "88:53:95": "Apple",
        "88:63:DF": "Apple",
        "88:66:5A": "Apple",
        "88:AE:1D": "Apple",
        "88:C6:26": "Apple",
        "88:E7:12": "Apple",
        "8C:00:6D": "Apple",
        "8C:29:37": "Apple",
        "8C:2D:AA": "Apple",
        "8C:58:77": "Apple",
        "8C:85:90": "Apple",
        "8C:8E:37": "Apple",
        "8C:BE:BE": "Apple",
        "90:72:40": "Apple",
        "90:84:0D": "Apple",
        "90:8D:6C": "Apple",
        "90:B2:1F": "Apple",
        "90:B9:31": "Apple",
        "90:DD:5D": "Apple",
        "94:94:26": "Apple",
        "94:B0:7F": "Apple",
        "94:B8:63": "Apple",
        "94:BF:2D": "Apple",
        "94:E9:6A": "Apple",
        "98:01:A7": "Apple",
        "98:0C:82": "Apple",
        "98:18:88": "Apple",
        "98:5A:EB": "Apple",
        "98:7B:19": "Apple",
        "98:9E:63": "Apple",
        "98:B8:E3": "Apple",
        "98:CA:33": "Apple",
        "98:D6:BB": "Apple",
        "98:E0:D9": "Apple",
        "9C:04:EB": "Apple",
        "9C:20:7B": "Apple",
        "9C:35:EB": "Apple",
        "9C:84:87": "Apple",
        "9C:99:A0": "Apple",
        "9C:B6:D0": "Apple",
        "9C:D0:04": "Apple",
        "9C:F3:87": "Apple",
        "A0:18:28": "Apple",
        "A0:99:9B": "Apple",
        "A4:31:35": "Apple",
        "A4:50:46": "Apple",
        "A4:67:06": "Apple",
        "A4:83:E7": "Apple",
        "A4:B1:97": "Apple",
        "A4:C3:F0": "Apple",
        "A4:D1:8C": "Apple",
        "A4:D9:31": "Apple",
        "A4:DE:33": "Apple",
        "A8:20:66": "Apple",
        "A8:5B:78": "Apple",
        "A8:66:7F": "Apple",
        "A8:88:08": "Apple",
        "A8:96:8A": "Apple",
        "A8:BB:CF": "Apple",
        "A8:BE:27": "Apple",
        "A8:E3:EE": "Apple",
        "AC:1F:74": "Apple",
        "AC:29:3A": "Apple",
        "AC:2B:6E": "Apple",
        "AC:3B:77": "Apple",
        "AC:61:54": "Apple",
        "AC:87:A3": "Apple",
        "AC:BC:32": "Apple",
        "AC:CF:5C": "Apple",
        "B0:19:21": "Apple",
        "B0:34:95": "Apple",
        "B0:65:BD": "Apple",
        "B0:70:2D": "Apple",
        "B0:9F:BA": "Apple",
        "B0:CA:68": "Apple",
        "B0:DF:3A": "Apple",
        "B4:0E:DE": "Apple",
        "B4:18:D1": "Apple",
        "B4:1E:8B": "Apple",
        "B4:2D:87": "Apple",
        "B4:3A:28": "Apple",
        "B4:4B:D6": "Apple",
        "B4:6D:83": "Apple",
        "B4:8C:9D": "Apple",
        "B4:9F:BF": "Apple",
        "B4:C4:FC": "Apple",
        "B4:E1:C4": "Apple",
        "B4:F0:AB": "Apple",
        "B4:F6:1C": "Apple",
        "B8:09:8A": "Apple",
        "B8:17:C2": "Apple",
        "B8:1D:7B": "Apple",
        "B8:41:A4": "Apple",
        "B8:44:D9": "Apple",
        "B8:53:AC": "Apple",
        "B8:78:2E": "Apple",
        "B8:8D:12": "Apple",
        "B8:9A:1A": "Apple",
        "B8:C1:11": "Apple",
        "B8:C7:5D": "Apple",
        "B8:E8:56": "Apple",
        "B8:F6:B1": "Apple",
        "B8:F9:34": "Apple",
        "B8:FF:34": "Apple",
        "BC:3B:AF": "Apple",
        "BC:52:B7": "Apple",
        "BC:67:1C": "Apple",
        "BC:6C:21": "Apple",
        "BC:79:AD": "Apple",
        "BC:85:56": "Apple",
        "BC:9F:EF": "Apple",
        "BC:B0:73": "Apple",
        "BC:C3:41": "Apple",
        "BC:D1:77": "Apple",
        "BC:EC:2D": "Apple",
        "C0:1A:DA": "Apple",
        "C0:63:AE": "Apple",
        "C0:97:27": "Apple",
        "C0:B6:F9": "Apple",
        "C0:C1:C0": "Apple",
        "C0:CE:CD": "Apple",
        "C4:2C:03": "Apple",
        "C4:2F:B0": "Apple",
        "C4:4E:24": "Apple",
        "C4:8E:8F": "Apple",
        "C4:98:80": "Apple",
        "C4:B1:6C": "Apple",
        "C8:1E:E7": "Apple",
        "C8:33:4B": "Apple",
        "C8:69:CD": "Apple",
        "C8:85:50": "Apple",
        "C8:8C:90": "Apple",
        "C8:9B:AD": "Apple",
        "C8:BC:C8": "Apple",
        "C8:CD:72": "Apple",
        "C8:D0:83": "Apple",
        "C8:D7:19": "Apple",
        "C8:E0:EB": "Apple",
        "C8:E2:DF": "Apple",
        "C8:F2:30": "Apple",
        "CC:25:EF": "Apple",
        "CC:29:F5": "Apple",
        "CC:2D:83": "Apple",
        "CC:61:E5": "Apple",
        "CC:78:5F": "Apple",
        "CC:8E:82": "Apple",
        "CC:9F:7A": "Apple",
        "CC:B5:D1": "Apple",
        "CC:C7:60": "Apple",
        "D0:03:4B": "Apple",
        "D0:23:DB": "Apple",
        "D0:25:98": "Apple",
        "D0:33:11": "Apple",
        "D0:4F:7E": "Apple",
        "D0:52:A8": "Apple",
        "D0:5A:09": "Apple",
        "D0:5F:64": "Apple",
        "D0:67:E5": "Apple",
        "D0:81:7A": "Apple",
        "D0:87:E2": "Apple",
        "D0:A6:37": "Apple",
        "D0:A8:6D": "Apple",
        "D0:AE:EC": "Apple",
        "D0:C5:F3": "Apple",
        "D0:D2:9A": "Apple",
        "D0:D9:DC": "Apple",
        "D0:E7:82": "Apple",
        "D0:EE:07": "Apple",
        "D4:61:9D": "Apple",
        "D4:90:9C": "Apple",
        "D4:A3:3D": "Apple",
        "D4:D8:25": "Apple",
        "D8:00:4D": "Apple",
        "D8:1C:79": "Apple",
        "D8:30:62": "Apple",
        "D8:37:3B": "Apple",
        "D8:3B:BF": "Apple",
        "D8:5C:52": "Apple",
        "D8:5D:4C": "Apple",
        "D8:5D:E2": "Apple",
        "D8:61:72": "Apple",
        "D8:62:90": "Apple",
        "D8:6B:F7": "Apple",
        "D8:7B:95": "Apple",
        "D8:8C:79": "Apple",
        "D8:96:85": "Apple",
        "D8:9E:3F": "Apple",
        "D8:A2:5E": "Apple",
        "D8:A9:8B": "Apple",
        "D8:BB:2C": "Apple",
        "D8:C0:A6": "Apple",
        "D8:C4:E9": "Apple",
        "D8:D1:CB": "Apple",
        "D8:E0:E1": "Apple",
        "DC:0C:5C": "Apple",
        "DC:2B:2A": "Apple",
        "DC:37:43": "Apple",
        "DC:3C:F6": "Apple",
        "DC:44:6B": "Apple",
        "DC:4F:22": "Apple",
        "DC:54:7D": "Apple",
        "DC:56:0B": "Apple",
        "DC:5E:4B": "Apple",
        "DC:6D:CA": "Apple",
        "DC:70:B2": "Apple",
        "DC:72:9B": "Apple",
        "DC:86:D8": "Apple",
        "DC:8B:28": "Apple",
        "DC:9E:91": "Apple",
        "DC:A2:66": "Apple",
        "DC:A6:32": "Apple",
        "DC:A9:04": "Apple",
        "DC:D3:A2": "Apple",
        "DC:E8:56": "Apple",
        "DC:F3:90": "Apple",
        "E0:03:6A": "Apple",
        "E0:33:8E": "Apple",
        "E0:4A:4D": "Apple",
        "E0:5F:45": "Apple",
        "E0:88:5D": "Apple",
        "E0:9D:FA": "Apple",
        "E0:AC:CB": "Apple",
        "E0:B5:2D": "Apple",
        "E0:B9:BA": "Apple",
        "E0:C7:67": "Apple",
        "E0:CB:4E": "Apple",
        "E0:CC:7A": "Apple",
        "E0:D5:38": "Apple",
        "E0:F5:C6": "Apple",
        "E0:F8:47": "Apple",
        "E0:FA:98": "Apple",
        "E4:25:E7": "Apple",
        "E4:32:CB": "Apple",
        "E4:4B:3B": "Apple",
        "E4:8B:7F": "Apple",
        "E4:98:D6": "Apple",
        "E4:9A:79": "Apple",
        "E4:A9:72": "Apple",
        "E4:C6:0D": "Apple",
        "E4:C8:E5": "Apple",
        "E4:CE:8F": "Apple",
        "E4:D1:37": "Apple",
        "E4:D9:09": "Apple",
        "E8:04:0B": "Apple",
        "E8:06:88": "Apple",
        "E8:08:8B": "Apple",
        "E8:0A:22": "Apple",
        "E8:11:32": "Apple",
        "E8:1D:E8": "Apple",
        "E8:20:E2": "Apple",
        "E8:26:AE": "Apple",
        "E8:2A:EA": "Apple",
        "E8:33:89": "Apple",
        "E8:39:35": "Apple",
        "E8:3E:8C": "Apple",
        "E8:40:40": "Apple",
        "E8:44:9D": "Apple",
        "E8:4E:CE": "Apple",
        "E8:50:8B": "Apple",
        "E8:52:09": "Apple",
        "E8:80:2D": "Apple",
        "E8:85:8B": "Apple",
        "E8:86:73": "Apple",
        "E8:87:8F": "Apple",
        "E8:88:20": "Apple",
        "E8:89:2F": "Apple",
        "E8:8D:28": "Apple",
        "E8:8E:53": "Apple",
        "E8:91:4F": "Apple",
        "E8:92:A4": "Apple",
        "E8:93:09": "Apple",
        "E8:94:EE": "Apple",
        "E8:95:6E": "Apple",
        "E8:96:1F": "Apple",
        "E8:97:EF": "Apple",
        "E8:98:8B": "Apple",
        "E8:99:C4": "Apple",
        "E8:9A:8F": "Apple",
        "E8:9B:BD": "Apple",
        "E8:9C:8A": "Apple",
        "E8:9E:4F": "Apple",
        "E8:A0:0D": "Apple",
        "E8:A5:5F": "Apple",
        "E8:B2:AC": "Apple",
        "E8:B7:48": "Apple",
        "E8:B9:68": "Apple",
        "E8:BA:70": "Apple",
        "E8:BB:50": "Apple",
        "E8:BC:80": "Apple",
        "E8:C0:02": "Apple",
        "E8:C1:1B": "Apple",
        "E8:C2:8B": "Apple",
        "E8:C5:2B": "Apple",
        "E8:C7:4F": "Apple",
        "E8:C8:29": "Apple",
        "E8:CA:88": "Apple",
        "E8:CB:9F": "Apple",
        "E8:CC:18": "Apple",
        "E8:CD:2A": "Apple",
        "E8:CE:53": "Apple",
        "E8:D0:FC": "Apple",
        "E8:D1:1B": "Apple",
        "E8:D2:37": "Apple",
        "E8:D3:85": "Apple",
        "E8:D4:9B": "Apple",
        "E8:D5:37": "Apple",
        "E8:D6:53": "Apple",
        "E8:D8:1B": "Apple",
        "E8:D9:EF": "Apple",
        "E8:DA:0B": "Apple",
        "E8:DB:55": "Apple",
        "E8:DC:80": "Apple",
        "E8:DD:3F": "Apple",
        "E8:DE:27": "Apple",
        "E8:DF:70": "Apple",
        "E8:E0:7B": "Apple",
        "E8:E1:1B": "Apple",
        "E8:E2:50": "Apple",
        "E8:E3:8E": "Apple",
        "E8:E4:CE": "Apple",
        "E8:E5:D1": "Apple",
        "E8:E6:AA": "Apple",
        "E8:E7:84": "Apple",
        "E8:E8:8B": "Apple",
        "E8:E9:F7": "Apple",
        "E8:EA:6A": "Apple",
        "E8:EB:11": "Apple",
        "E8:EC:8A": "Apple",
        "E8:ED:05": "Apple",
        "E8:EE:CC": "Apple",
        "E8:EF:06": "Apple",
        "E8:F0:FC": "Apple",
        "E8:F1:15": "Apple",
        "E8:F2:E2": "Apple",
        "E8:F3:09": "Apple",
        "E8:F4:5C": "Apple",
        "E8:F5:1B": "Apple",
        "E8:F6:0B": "Apple",
        "E8:F7:1B": "Apple",
        "E8:F8:53": "Apple",
        "E8:F9:28": "Apple",
        "E8:FA:8B": "Apple",
        "E8:FB:1A": "Apple",
        "E8:FC:AF": "Apple",
        "E8:FD:6B": "Apple",
        "E8:FE:05": "Apple",
        "EC:35:86": "Apple",
        "EC:61:4F": "Apple",
        "EC:85:2F": "Apple",
        "EC:9B:3B": "Apple",
        "EC:A6:2D": "Apple",
        "EC:B0:07": "Apple",
        "EC:C4:0D": "Apple",
        "EC:C7:13": "Apple",
        "EC:CE:77": "Apple",
        "EC:D0:9F": "Apple",
        "EC:D6:8A": "Apple",
        "EC:D9:15": "Apple",
        "F0:0D:58": "Apple",
        "F0:1C:13": "Apple",
        "F0:24:75": "Apple",
        "F0:35:5B": "Apple",
        "F0:3C:91": "Apple",
        "F0:3E:90": "Apple",
        "F0:4F:7C": "Apple",
        "F0:51:7B": "Apple",
        "F0:59:73": "Apple",
        "F0:5E:CD": "Apple",
        "F0:6A:12": "Apple",
        "F0:6E:0B": "Apple",
        "F0:71:4C": "Apple",
        "F0:74:86": "Apple",
        "F0:76:6C": "Apple",
        "F0:77:63": "Apple",
        "F0:78:21": "Apple",
        "F0:79:59": "Apple",
        "F0:7A:62": "Apple",
        "F0:7B:D9": "Apple",
        "F0:7E:3D": "Apple",
        "F0:81:73": "Apple",
        "F0:84:7F": "Apple",
        "F0:85:61": "Apple",
        "F0:86:00": "Apple",
        "F0:87:4F": "Apple",
        "F0:88:17": "Apple",
        "F0:89:0A": "Apple",
        "F0:8A:36": "Apple",
        "F0:8B:21": "Apple",
        "F0:8C:7D": "Apple",
        "F0:8D:30": "Apple",
        "F0:8E:08": "Apple",
        "F0:8F:78": "Apple",
        "F0:90:6D": "Apple",
        "F0:91:61": "Apple",
        "F0:92:3D": "Apple",
        "F0:93:0A": "Apple",
        "F0:94:2A": "Apple",
        "F0:95:05": "Apple",
        "F0:96:1A": "Apple",
        "F0:97:0B": "Apple",
        "F0:98:56": "Apple",
        "F0:99:30": "Apple",
        "F0:9A:0D": "Apple",
        "F0:9B:70": "Apple",
        "F0:9C:71": "Apple",
        "F0:9D:09": "Apple",
        "F0:9E:A0": "Apple",
        "F0:9F:E7": "Apple",
        "F0:A0:29": "Apple",
        "F0:A1:4D": "Apple",
        "F0:A2:25": "Apple",
        "F0:A3:5F": "Apple",
        "F0:A4:3E": "Apple",
        "F0:A5:58": "Apple",
        "F0:A6:62": "Apple",
        "F0:A7:FD": "Apple",
        "F0:A8:78": "Apple",
        "F0:A9:0A": "Apple",
        "F0:AA:63": "Apple",
        "F0:AB:91": "Apple",
        "F0:AC:9F": "Apple",
        "F0:AD:4E": "Apple",
        "F0:AE:4D": "Apple",
        "F0:AF:70": "Apple",
        "F0:B0:09": "Apple",
        "F0:B1:EC": "Apple",
        "F0:B2:07": "Apple",
        "F0:B3:0D": "Apple",
        "F0:B4:79": "Apple",
        "F0:B5:1A": "Apple",
        "F0:B6:72": "Apple",
        "F0:B7:79": "Apple",
        "F0:B8:64": "Apple",
        "F0:B9:6A": "Apple",
        "F0:BA:0D": "Apple",
        "F0:BB:15": "Apple",
        "F0:BC:02": "Apple",
        "F0:BD:5E": "Apple",
        "F0:BE:41": "Apple",
        "F0:BF:2A": "Apple",
        "F0:C0:7C": "Apple",
        "F0:C1:2D": "Apple",
        "F0:C2:35": "Apple",
        "F0:C3:71": "Apple",
        "F0:C4:7E": "Apple",
        "F0:C5:CB": "Apple",
        "F0:C6:12": "Apple",
        "F0:C7:4B": "Apple",
        "F0:C8:2D": "Apple",
        "F0:C9:2F": "Apple",
        "F0:CA:04": "Apple",
        "F0:CB:03": "Apple",
        "F0:CC:16": "Apple",
        "F0:CD:6D": "Apple",
        "F0:CE:1A": "Apple",
        "F0:CF:A3": "Apple",
        "F0:D0:05": "Apple",
        "F0:D1:8D": "Apple",
        "F0:D2:1C": "Apple",
        "F0:D3:8F": "Apple",
        "F0:D4:15": "Apple",
        "F0:D5:0A": "Apple",
        "F0:D6:0D": "Apple",
        "F0:D7:10": "Apple",
        "F0:D8:35": "Apple",
        "F0:D9:1A": "Apple",
        "F0:DA:97": "Apple",
        "F0:DB:E2": "Apple",
        "F0:DC:29": "Apple",
        "F0:DD:5A": "Apple",
        "F0:DE:B1": "Apple",
        "F0:DF:0A": "Apple",
        "F0:E0:65": "Apple",
        "F0:E1:4C": "Apple",
        "F0:E2:4F": "Apple",
        "F0:E3:3D": "Apple",
        "F0:E4:3A": "Apple",
        "F0:E5:25": "Apple",
        "F0:E6:0B": "Apple",
        "F0:E7:33": "Apple",
        "F0:E8:17": "Apple",
        "F0:E9:60": "Apple",
        "F0:EA:10": "Apple",
        "F0:EB:75": "Apple",
        "F0:EC:72": "Apple",
        "F0:ED:1D": "Apple",
        "F0:EE:10": "Apple",
        "F0:EF:86": "Apple",
        "F0:F0:84": "Apple",
        "F0:F1:56": "Apple",
        "F0:F2:23": "Apple",
        "F0:F3:25": "Apple",
        "F0:F4:7B": "Apple",
        "F0:F5:24": "Apple",
        "F0:F6:19": "Apple",
        "F0:F7:6F": "Apple",
        "F0:F8:15": "Apple",
        "F0:F9:6D": "Apple",
        "F0:FA:74": "Apple",
        "F0:FB:4F": "Apple",
        "F0:FC:13": "Apple",
        "F0:FD:3B": "Apple",
        "F0:FE:6B": "Apple",
        "F0:FF:C1": "Apple",
        "F4:0D:74": "Apple",
        "F4:1A:4D": "Apple",
        "F4:28:53": "Apple",
        "F4:37:B7": "Apple",
        "F4:3C:32": "Apple",
        "F4:4E:88": "Apple",
        "F4:5C:89": "Apple",
        "F4:61:7F": "Apple",
        "F4:79:60": "Apple",
        "F4:7B:5B": "Apple",
        "F4:83:CD": "Apple",
        "F4:87:F7": "Apple",
        "F4:8A:97": "Apple",
        "F4:8B:32": "Apple",
        "F4:9F:85": "Apple",
        "F4:B1:9A": "Apple",
        "F4:B7:E2": "Apple",
        "F4:C7:71": "Apple",
        "F4:CF:A7": "Apple",
        "F4:D1:08": "Apple",
        "F4:D4:88": "Apple",
        "F4:D9:5F": "Apple",
        "F4:DB:E3": "Apple",
        "F4:DC:9F": "Apple",
        "F4:DE:9F": "Apple",
        "F4:E0:8D": "Apple",
        "F4:E5:35": "Apple",
        "F4:E8:8A": "Apple",
        "F4:EA:67": "Apple",
        "F4:F1:5A": "Apple",
        "F4:F2:6D": "Apple",
        "F4:F3:52": "Apple",
        "F4:F4:85": "Apple",
        "F4:F5:24": "Apple",
        "F4:F6:2B": "Apple",
        "F4:F7:76": "Apple",
        "F4:F8:51": "Apple",
        "F4:F9:87": "Apple",
        "F4:FA:16": "Apple",
        "F4:FB:10": "Apple",
        "F4:FC:3F": "Apple",
        "F4:FD:B5": "Apple",
        "F4:FE:8B": "Apple",
        "F4:FF:7E": "Apple",
        "F8:27:93": "Apple",
        "F8:2C:18": "Apple",
        "F8:34:41": "Apple",
        "F8:35:DD": "Apple",
        "F8:4D:89": "Apple",
        "F8:50:97": "Apple",
        "F8:54:88": "Apple",
        "F8:5C:7D": "Apple",
        "F8:5E:3C": "Apple",
        "F8:6B:D9": "Apple",
        "F8:77:B8": "Apple",
        "F8:78:B8": "Apple",
        "F8:79:60": "Apple",
        "F8:7A:79": "Apple",
        "F8:7B:09": "Apple",
        "F8:7C:35": "Apple",
        "F8:7D:76": "Apple",
        "F8:7E:4C": "Apple",
        "F8:7F:41": "Apple",
        "F8:80:27": "Apple",
        "F8:81:1A": "Apple",
        "F8:82:23": "Apple",
        "F8:83:52": "Apple",
        "F8:84:2A": "Apple",
        "F8:85:4F": "Apple",
        "F8:86:84": "Apple",
        "F8:87:3D": "Apple",
        "F8:88:2B": "Apple",
        "F8:89:4A": "Apple",
        "F8:8A:4A": "Apple",
        "F8:8B:2B": "Apple",
        "F8:8C:89": "Apple",
        "F8:8D:25": "Apple",
        "F8:8E:85": "Apple",
        "F8:8F:84": "Apple",
        "F8:90:1E": "Apple",
        "F8:91:79": "Apple",
        "F8:92:48": "Apple",
        "F8:93:9A": "Apple",
        "F8:94:C2": "Apple",
        "F8:95:EA": "Apple",
        "F8:96:8A": "Apple",
        "F8:97:AA": "Apple",
        "F8:98:0E": "Apple",
        "F8:99:8F": "Apple",
        "F8:9A:5D": "Apple",
        "F8:9B:9A": "Apple",
        "F8:9C:02": "Apple",
        "F8:9D:18": "Apple",
        "F8:9E:0D": "Apple",
        "F8:9F:1E": "Apple",
        "F8:A0:97": "Apple",
        "F8:A1:13": "Apple",
        "F8:A2:F6": "Apple",
        "F8:A3:33": "Apple",
        "F8:A4:8A": "Apple",
        "F8:A5:1D": "Apple",
        "F8:A6:CF": "Apple",
        "F8:A7:60": "Apple",
        "F8:A8:6A": "Apple",
        "F8:A9:6F": "Apple",
        "F8:AA:0D": "Apple",
        "F8:AB:4A": "Apple",
        "F8:AC:9F": "Apple",
        "F8:AD:CB": "Apple",
        "F8:AE:8D": "Apple",
        "F8:AF:6F": "Apple",
        "F8:B0:41": "Apple",
        "F8:B1:56": "Apple",
        "F8:B2:63": "Apple",
        "F8:B3:4F": "Apple",
        "F8:B4:28": "Apple",
        "F8:B5:4D": "Apple",
        "F8:B6:8B": "Apple",
        "F8:B7:1B": "Apple",
        "F8:B8:4E": "Apple",
        "F8:B9:90": "Apple",
        "F8:BA:10": "Apple",
        "F8:BB:AC": "Apple",
        "F8:BC:12": "Apple",
        "F8:BD:09": "Apple",
        "F8:BE:0D": "Apple",
        "F8:BF:CC": "Apple",
        "F8:C0:17": "Apple",
        "F8:C1:11": "Apple",
        "F8:C2:1B": "Apple",
        "F8:C3:97": "Apple",
        "F8:C4:88": "Apple",
        "F8:C5:CB": "Apple",
        "F8:C6:57": "Apple",
        "F8:C7:8A": "Apple",
        "F8:C8:83": "Apple",
        "F8:C9:4F": "Apple",
        "F8:CA:0E": "Apple",
        "F8:CB:69": "Apple",
        "F8:CC:0C": "Apple",
        "F8:CD:4D": "Apple",
        "F8:CE:6E": "Apple",
        "F8:CF:C5": "Apple",
        "F8:D0:BD": "Apple",
        "F8:D1:11": "Apple",
        "F8:D2:8A": "Apple",
        "F8:D3:85": "Apple",
        "F8:D4:12": "Apple",
        "F8:D5:BD": "Apple",
        "F8:D6:0D": "Apple",
        "F8:D7:8B": "Apple",
        "F8:D8:47": "Apple",
        "F8:D9:9E": "Apple",
        "F8:DA:0C": "Apple",
        "F8:DB:8F": "Apple",
        "F8:DC:47": "Apple",
        "F8:DD:13": "Apple",
        "F8:DE:89": "Apple",
        "F8:DF:90": "Apple",
        "F8:E0:4C": "Apple",
        "F8:E1:6E": "Apple",
        "F8:E2:D7": "Apple",
        "F8:E3:FB": "Apple",
        "F8:E4:34": "Apple",
        "F8:E5:25": "Apple",
        "F8:E6:0A": "Apple",
        "F8:E7:92": "Apple",
        "F8:E8:4A": "Apple",
        "F8:E9:03": "Apple",
        "F8:EA:0D": "Apple",
        "F8:EB:87": "Apple",
        "F8:EC:5A": "Apple",
        "F8:ED:9F": "Apple",
        "F8:EE:7E": "Apple",
        "F8:EF:90": "Apple",
        "F8:F0:18": "Apple",
        "F8:F1:AC": "Apple",
        "F8:F2:7A": "Apple",
        "F8:F3:5D": "Apple",
        "F8:F4:1D": "Apple",
        "F8:F5:23": "Apple",
        "F8:F6:1A": "Apple",
        "F8:F7:92": "Apple",
        "F8:F8:17": "Apple",
        "F8:F9:8D": "Apple",
        "F8:FA:7C": "Apple",
        "F8:FB:57": "Apple",
        "F8:FC:17": "Apple",
        "F8:FD:9C": "Apple",
        "F8:FE:5E": "Apple",
        "F8:FF:C2": "Apple",
        "FC:0D:B2": "Apple",
        "FC:0E:6B": "Apple",
        "FC:0F:4B": "Apple",
        "FC:10:4D": "Apple",
        "FC:11:4F": "Apple",
        "FC:12:28": "Apple",
        "FC:13:8B": "Apple",
        "FC:14:5B": "Apple",
        "FC:15:4D": "Apple",
        "FC:16:3E": "Apple",
        "FC:17:3D": "Apple",
        "FC:18:96": "Apple",
        "FC:19:0B": "Apple",
        "FC:1A:11": "Apple",
        "FC:1B:4E": "Apple",
        "FC:1C:4B": "Apple",
        "FC:1D:43": "Apple",
        "FC:1E:3F": "Apple",
        "FC:1F:6B": "Apple",
        "FC:20:1D": "Apple",
        "FC:21:4B": "Apple",
        "FC:22:3D": "Apple",
        "FC:23:5D": "Apple",
        "FC:24:4F": "Apple",
        "FC:25:3F": "Apple",
        "FC:26:2D": "Apple",
        "FC:27:69": "Apple",
        "FC:28:98": "Apple",
        "FC:29:2A": "Apple",
        "FC:2A:11": "Apple",
        "FC:2B:5E": "Apple",
        "FC:2C:6E": "Apple",
        "FC:2D:93": "Apple",
        "FC:2E:5B": "Apple",
        "FC:2F:8A": "Apple",
        "FC:30:0D": "Apple",
        "FC:31:3D": "Apple",
        "FC:32:5D": "Apple",
        "FC:33:9A": "Apple",
        "FC:34:0B": "Apple",
        "FC:35:8D": "Apple",
        "FC:36:1D": "Apple",
        "FC:37:4E": "Apple",
        "FC:38:3B": "Apple",
        "FC:39:0D": "Apple",
        "FC:3A:2A": "Apple",
        "FC:3B:3D": "Apple",
        "FC:3C:91": "Apple",
        "FC:3D:9F": "Apple",
        "FC:3E:8D": "Apple",
        "FC:3F:7A": "Apple",
        "FC:40:81": "Apple",
        "FC:41:63": "Apple",
        "FC:42:6F": "Apple",
        "FC:43:0B": "Apple",
        "FC:44:8A": "Apple",
        "FC:45:67": "Apple",
        "FC:46:3F": "Apple",
        "FC:47:1B": "Apple",
        "FC:48:0F": "Apple",
        "FC:49:2B": "Apple",
        "FC:4A:2E": "Apple",
        "FC:4B:0D": "Apple",
        "FC:4C:6F": "Apple",
        "FC:4D:8B": "Apple",
        "FC:4E:3F": "Apple",
        "FC:4F:2A": "Apple",
        "FC:50:1B": "Apple",
        "FC:51:4C": "Apple",
        "FC:52:0A": "Apple",
        "FC:53:9D": "Apple",
        "FC:54:4F": "Apple",
        "FC:55:3B": "Apple",
        "FC:56:2D": "Apple",
        "FC:57:5A": "Apple",
        "FC:58:9A": "Apple",
        "FC:59:3F": "Apple",
        "FC:5A:1E": "Apple",
        "FC:5B:3A": "Apple",
        "FC:5C:1C": "Apple",
        "FC:5D:4B": "Apple",
        "FC:5E:6B": "Apple",
        "FC:5F:2A": "Apple",
        "FC:60:1F": "Apple",
        "FC:61:4D": "Apple",
        "FC:62:3E": "Apple",
        "FC:63:0B": "Apple",
        "FC:64:3D": "Apple",
        "FC:65:9A": "Apple",
        "FC:66:2F": "Apple",
        "FC:67:1A": "Apple",
        "FC:68:0E": "Apple",
        "FC:69:4B": "Apple",
        "FC:6A:2F": "Apple",
        "FC:6B:0D": "Apple",
        "FC:6C:8A": "Apple",
        "FC:6D:3F": "Apple",
        "FC:6E:6B": "Apple",
        "FC:6F:3A": "Apple",
        "FC:70:1C": "Apple",
        "FC:71:0D": "Apple",
        "FC:72:9F": "Apple",
        "FC:73:2B": "Apple",
        "FC:74:1A": "Apple",
        "FC:75:4D": "Apple",
        "FC:76:3C": "Apple",
        "FC:77:0B": "Apple",
        "FC:78:2E": "Apple",
        "FC:79:4A": "Apple",
        "FC:7A:1D": "Apple",
        "FC:7B:6F": "Apple",
        "FC:7C:3D": "Apple",
        "FC:7D:2A": "Apple",
        "FC:7E:5B": "Apple",
        "FC:7F:8A": "Apple",
        "FC:80:1C": "Apple",
        "FC:81:3D": "Apple",
        "FC:82:4B": "Apple",
        "FC:83:2A": "Apple",
        "FC:84:1F": "Apple",
        "FC:85:0E": "Apple",
        "FC:86:3C": "Apple",
        "FC:87:5A": "Apple",
        "FC:88:2D": "Apple",
        "FC:89:1B": "Apple",
        "FC:8A:4F": "Apple",
        "FC:8B:3E": "Apple",
        "FC:8C:0A": "Apple",
        "FC:8D:2C": "Apple",
        "FC:8E:5B": "Apple",
        "FC:8F:1D": "Apple",
        "FC:90:3A": "Apple",
        "FC:91:4E": "Apple",
        "FC:92:0B": "Apple",
        "FC:93:6F": "Apple",
        "FC:94:2D": "Apple",
        "FC:95:1A": "Apple",
        "FC:96:3B": "Apple",
        "FC:97:0C": "Apple",
        "FC:98:4A": "Apple",
        "FC:99:2E": "Apple",
        "FC:9A:1F": "Apple",
        "FC:9B:5D": "Apple",
        "FC:9C:3C": "Apple",
        "FC:9D:0B": "Apple",
        "FC:9E:4F": "Apple",
        "FC:9F:2A": "Apple",
        "FC:A0:1D": "Apple",
        "FC:A1:3E": "Apple",
        "FC:A2:5B": "Apple",
        "FC:A3:0C": "Apple",
        "FC:A4:2F": "Apple",
        "FC:A5:1A": "Apple",
        "FC:A6:4D": "Apple",
        "FC:A7:3B": "Apple",
        "FC:A8:0E": "Apple",
        "FC:A9:2C": "Apple",
        "FC:AA:5F": "Apple",
        "FC:AB:1D": "Apple",
        "FC:AC:4B": "Apple",
        "FC:AD:3A": "Apple",
        "FC:AE:0F": "Apple",
        "FC:AF:2E": "Apple",
        "FC:B0:1C": "Apple",
        "FC:B1:5A": "Apple",
        "FC:B2:3D": "Apple",
        "FC:B3:0B": "Apple",
        "FC:B4:4F": "Apple",
        "FC:B5:2A": "Apple",
        "FC:B6:1E": "Apple",
        "FC:B7:3C": "Apple",
        "FC:B8:5D": "Apple",
        "FC:B9:0A": "Apple",
        "FC:BA:4E": "Apple",
        "FC:BB:1B": "Apple",
        "FC:BC:3F": "Apple",
        "FC:BD:2D": "Apple",
        "FC:BE:0C": "Apple",
        "FC:BF:5B": "Apple",
        "FC:C0:1A": "Apple",
        "FC:C1:4F": "Apple",
        "FC:C2:3E": "Apple",
        "FC:C3:0D": "Apple",
        "FC:C4:2B": "Apple",
        "FC:C5:1C": "Apple",
        "FC:C6:5A": "Apple",
        "FC:C7:3D": "Apple",
        "FC:C8:0B": "Apple",
        "FC:C9:4E": "Apple",
        "FC:CA:2F": "Apple",
        "FC:CB:1D": "Apple",
        "FC:CC:3A": "Apple",
        "FC:CD:5B": "Apple",
        "FC:CE:0E": "Apple",
        "FC:CF:2C": "Apple",
        "FC:D0:1F": "Apple",
        "FC:D1:4A": "Apple",
        "FC:D2:3B": "Apple",
        "FC:D3:0F": "Apple",
        "FC:D4:5E": "Apple",
        "FC:D5:2D": "Apple",
        "FC:D6:1B": "Apple",
        "FC:D7:3C": "Apple",
        "FC:D8:0A": "Apple",
        "FC:D9:4F": "Apple",
        "FC:DA:2E": "Apple",
        "FC:DB:1C": "Apple",
        "FC:DC:3F": "Apple",
        "FC:DD:5A": "Apple",
        "FC:DE:0D": "Apple",
        "FC:DF:2B": "Apple",
        "FC:E0:1E": "Apple",
        "FC:E1:3D": "Apple",
        "FC:E2:4C": "Apple",
        "FC:E3:0B": "Apple",
        "FC:E4:2A": "Apple",
        "FC:E5:1F": "Apple",
        "FC:E6:5B": "Apple",
        "FC:E7:3E": "Apple",
        "FC:E8:0C": "Apple",
        "FC:E9:4D": "Apple",
        "FC:EA:2F": "Apple",
        "FC:EB:1A": "Apple",
        "FC:EC:3B": "Apple",
        "FC:ED:5C": "Apple",
        "FC:EE:0F": "Apple",
        "FC:EF:2D": "Apple",
        "FC:F0:1C": "Apple",
        "FC:F1:4B": "Apple",
        "FC:F2:3A": "Apple",
        "FC:F3:0E": "Apple",
        "FC:F4:5F": "Apple",
        "FC:F5:2C": "Apple",
        "FC:F6:1D": "Apple",
        "FC:F7:3F": "Apple",
        "FC:F8:0B": "Apple",
        "FC:F9:4E": "Apple",
        "FC:FA:2E": "Apple",
        "FC:FB:1B": "Apple",
        "FC:FC:3C": "Apple",
        "FC:FD:5D": "Apple",
        "FC:FE:0A": "Apple",
        "FC:FF:2F": "Apple",
    }

    def __init__(self, network_range: str = None):
        """
        初始化局域网发现

        Args:
            network_range: 网络范围，如 "192.168.1.0/24"
        """
        self.network_range = network_range
        self.discovered_devices: Dict[str, NetworkDevice] = {}

    def get_local_network(self) -> str:
        """获取本地网络范围"""
        try:
            # 获取本机IP
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect(("8.8.8.8", 80))
            local_ip = s.getsockname()[0]
            s.close()

            # 推断网络范围
            ip_parts = local_ip.split('.')
            network = f"{ip_parts[0]}.{ip_parts[1]}.{ip_parts[2]}.0/24"
            return network
        except Exception as e:
            logger.error(f"获取本地网络失败: {e}")
            return "192.168.1.0/24"

    def scan_arp(self, network_range: str = None) -> List[NetworkDevice]:
        """
        ARP扫描发现设备

        Args:
            network_range: 网络范围

        Returns:
            发现的设备列表
        """
        if network_range is None:
            network_range = self.network_range or self.get_local_network()

        devices = []
        logger.info(f"开始ARP扫描: {network_range}")

        if HAS_SCAPY:
            devices = self._scan_arp_scapy(network_range)
        else:
            devices = self._scan_arp_native(network_range)

        # 更新已发现设备
        for device in devices:
            self.discovered_devices[device.ip_address] = device

        return devices

    def _scan_arp_scapy(self, network_range: str) -> List[NetworkDevice]:
        """使用Scapy进行ARP扫描"""
        devices = []
        try:
            # 创建ARP请求
            arp = ARP(pdst=network_range)
            ether = Ether(dst="ff:ff:ff:ff:ff:ff")
            packet = ether / arp

            # 发送并接收响应
            result = srp(packet, timeout=3, verbose=0)[0]

            for sent, received in result:
                mac = received.hwsrc
                ip = received.psrc

                device = NetworkDevice(
                    ip_address=ip,
                    mac_address=mac,
                    vendor=self._get_vendor(mac),
                    status="up"
                )
                devices.append(device)
                logger.debug(f"ARP发现: {ip} - {mac}")

        except Exception as e:
            logger.error(f"Scapy ARP扫描失败: {e}")

        return devices

    def _scan_arp_native(self, network_range: str) -> List[NetworkDevice]:
        """使用系统命令进行ARP扫描"""
        devices = []
        try:
            # 解析网络范围
            network = ipaddress.ip_network(network_range, strict=False)

            # 使用ping触发ARP
            with ThreadPoolExecutor(max_workers=50) as executor:
                ips = [str(ip) for ip in network.hosts()]
                executor.map(self._ping_host, ips[:254])  # 限制扫描数量

            # 读取ARP表
            devices = self._read_arp_table()

        except Exception as e:
            logger.error(f"原生ARP扫描失败: {e}")

        return devices

    def _ping_host(self, ip: str):
        """Ping主机触发ARP"""
        try:
            param = '-n' if platform.system().lower() == 'windows' else '-c'
            subprocess.run(
                ['ping', param, '1', '-w', '100', ip],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL
            )
        except Exception:
            pass

    def _read_arp_table(self) -> List[NetworkDevice]:
        """读取系统ARP表"""
        devices = []
        try:
            if platform.system().lower() == 'windows':
                result = subprocess.run(['arp', '-a'], capture_output=True, text=True)
                lines = result.stdout.split('\n')

                for line in lines:
                    # Windows ARP表格式: "  192.168.1.1           00-aa-bb-cc-dd-ee     动态"
                    parts = line.split()
                    if len(parts) >= 2:
                        ip = parts[0]
                        mac = parts[1].replace('-', ':')
                        if self._is_valid_ip(ip) and self._is_valid_mac(mac):
                            device = NetworkDevice(
                                ip_address=ip,
                                mac_address=mac,
                                vendor=self._get_vendor(mac),
                                status="up"
                            )
                            devices.append(device)
            else:
                # Linux/Mac
                result = subprocess.run(['arp', '-a'], capture_output=True, text=True)
                lines = result.stdout.split('\n')

                for line in lines:
                    # 格式: "host (192.168.1.1) at 00:aa:bb:cc:dd:ee [ether] on eth0"
                    if '(' in line and ')' in line:
                        ip_start = line.find('(') + 1
                        ip_end = line.find(')')
                        ip = line[ip_start:ip_end]

                        if 'at' in line:
                            parts = line.split('at')
                            if len(parts) > 1:
                                mac = parts[1].split()[0]
                                if self._is_valid_mac(mac):
                                    device = NetworkDevice(
                                        ip_address=ip,
                                        mac_address=mac,
                                        vendor=self._get_vendor(mac),
                                        status="up"
                                    )
                                    devices.append(device)

        except Exception as e:
            logger.error(f"读取ARP表失败: {e}")

        return devices

    def scan_ports(self, target: str, ports: List[int] = None) -> List[int]:
        """
        端口扫描

        Args:
            target: 目标IP
            ports: 要扫描的端口列表

        Returns:
            开放的端口列表
        """
        if ports is None:
            ports = list(self.COMMON_PORTS.keys())

        open_ports = []
        logger.info(f"开始端口扫描: {target}")

        if HAS_NMAP:
            open_ports = self._scan_ports_nmap(target, ports)
        else:
            open_ports = self._scan_ports_socket(target, ports)

        return open_ports

    def _scan_ports_nmap(self, target: str, ports: List[int]) -> List[int]:
        """使用nmap进行端口扫描"""
        open_ports = []
        try:
            nm = nmap.PortScanner()
            port_str = ','.join(map(str, ports))
            nm.scan(target, arguments=f'-p {port_str} -T4')

            for host in nm.all_hosts():
                for proto in nm[host].all_protocols():
                    ports = nm[host][proto].keys()
                    for port in ports:
                        if nm[host][proto][port]['state'] == 'open':
                            open_ports.append(port)
                            logger.debug(f"端口开放: {target}:{port}")

        except Exception as e:
            logger.error(f"nmap端口扫描失败: {e}")

        return open_ports

    def _scan_ports_socket(self, target: str, ports: List[int]) -> List[int]:
        """使用socket进行端口扫描"""
        open_ports = []

        def check_port(port):
            try:
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                sock.settimeout(1)
                result = sock.connect_ex((target, port))
                sock.close()
                if result == 0:
                    return port
            except Exception:
                pass
            return None

        with ThreadPoolExecutor(max_workers=20) as executor:
            results = executor.map(check_port, ports)
            open_ports = [p for p in results if p is not None]

        return open_ports

    def get_hostname(self, ip: str) -> str:
        """获取主机名"""
        try:
            hostname = socket.gethostbyaddr(ip)[0]
            return hostname
        except socket.herror:
            return "Unknown"
        except Exception as e:
            logger.debug(f"获取主机名失败 {ip}: {e}")
            return "Unknown"

    def identify_device(self, device: NetworkDevice) -> NetworkDevice:
        """识别设备类型"""
        # 根据开放端口判断设备类型
        if device.open_ports:
            if 80 in device.open_ports or 443 in device.open_ports:
                device.device_type = "Web Server"
            elif 22 in device.open_ports:
                device.device_type = "SSH Server"
            elif 3389 in device.open_ports:
                device.device_type = "Windows RDP"
            elif 445 in device.open_ports:
                device.device_type = "File Server"
            elif 3306 in device.open_ports:
                device.device_type = "MySQL Server"
            elif 5432 in device.open_ports:
                device.device_type = "PostgreSQL Server"
            elif 27017 in device.open_ports:
                device.device_type = "MongoDB Server"
            elif 6379 in device.open_ports:
                device.device_type = "Redis Server"

        # 根据MAC地址厂商判断
        if device.vendor != "Unknown":
            vendor_lower = device.vendor.lower()
            if "apple" in vendor_lower:
                device.device_type = "Apple Device"
            elif "intel" in vendor_lower:
                device.device_type = "Intel Device"
            elif "vmware" in vendor_lower:
                device.device_type = "Virtual Machine"

        return device

    def full_scan(self, network_range: str = None, scan_ports: bool = True) -> List[NetworkDevice]:
        """
        完整扫描

        Args:
            network_range: 网络范围
            scan_ports: 是否扫描端口

        Returns:
            发现的设备列表
        """
        # ARP扫描
        devices = self.scan_arp(network_range)

        # 端口扫描和设备识别
        for device in devices:
            # 获取主机名
            device.hostname = self.get_hostname(device.ip_address)

            # 端口扫描
            if scan_ports:
                device.open_ports = self.scan_ports(device.ip_address)

            # 设备识别
            device = self.identify_device(device)

            # 更新设备信息
            self.discovered_devices[device.ip_address] = device

        logger.info(f"扫描完成，发现 {len(devices)} 个设备")
        return devices

    def _get_vendor(self, mac: str) -> str:
        """根据MAC地址获取厂商"""
        if not mac:
            return "Unknown"

        # 获取MAC前缀
        mac_prefix = mac.upper()[:8]
        if len(mac_prefix) >= 8:
            mac_prefix = mac_prefix.replace(':', '')[:6]
            mac_prefix = f"{mac_prefix[:2]}:{mac_prefix[2:4]}:{mac_prefix[4:6]}"

        return self.VENDOR_PREFIXES.get(mac_prefix, "Unknown")

    def _is_valid_ip(self, ip: str) -> bool:
        """验证IP地址格式"""
        try:
            ipaddress.ip_address(ip)
            return True
        except ValueError:
            return False

    def _is_valid_mac(self, mac: str) -> bool:
        """验证MAC地址格式"""
        import re
        pattern = r'^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$'
        return bool(re.match(pattern, mac))

    def get_device_by_ip(self, ip: str) -> Optional[NetworkDevice]:
        """根据IP获取设备"""
        return self.discovered_devices.get(ip)

    def get_all_devices(self) -> List[NetworkDevice]:
        """获取所有已发现设备"""
        return list(self.discovered_devices.values())


# 同步接口封装
def scan_lan_devices(network_range: str = None, scan_ports: bool = False) -> List[Dict]:
    """
    同步扫描局域网设备

    Args:
        network_range: 网络范围
        scan_ports: 是否扫描端口

    Returns:
        设备字典列表
    """
    discovery = LANDiscovery(network_range)
    devices = discovery.full_scan(network_range, scan_ports)
    return [d.to_dict() for d in devices]


if __name__ == "__main__":
    # 测试代码
    logging.basicConfig(level=logging.INFO)

    print("开始扫描局域网设备...")
    devices = scan_lan_devices(scan_ports=True)

    print(f"\n共发现 {len(devices)} 个设备:")
    for d in devices:
        print(f"  - {d['ip_address']} ({d['mac_address']}) - {d['hostname']} - {d['vendor']} - {d['device_type']}")
        if d['open_ports']:
            print(f"    开放端口: {d['open_ports']}")