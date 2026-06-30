#!/usr/bin/env python3
"""
智壤卫士本地开发环境测试脚本
测试 localhost:5571 后端API和 localhost:5173 前端页面
"""

import os
import sys
import json
import time
import datetime
from pathlib import Path

try:
    from playwright.sync_api import sync_playwright, TimeoutError as PlaywrightTimeout
except ImportError:
    print("请安装 playwright: pip install playwright && playwright install chromium")
    sys.exit(1)

try:
    import pandas as pd
except ImportError:
    print("请安装 pandas: pip install pandas openpyxl")
    sys.exit(1)


class LocalTester:
    """本地开发环境测试器"""

    BACKEND_URL = "http://localhost:5571"
    FRONTEND_URL = "http://localhost:5173"

    # 后端API端点测试列表
    API_ENDPOINTS = [
        "/api/v1/menu/tree",
        "/api/v1/dashboard/stats",
        "/api/v1/mission/list",
        "/api/v1/device/list",
        "/api/v1/approval/list",
        "/api/v1/announcement/list",
        "/api/v1/dataanalyzer/bo",
        "/api/v1/dataanalyzer/stats/summary",
        "/api/v1/export/tasks",
        "/api/v1/designer/drafts",
        "/api/v1/user/list",
        "/api/v1/role/list",
    ]

    # 前端页面路由
    PAGE_ROUTES = [
        "/",
        "/portal",
        "/app/dashboard",
        "/app/mission-list",
        "/app/device",
        "/app/approval-list",
        "/app/workflow-design",
        "/app/data-import",
        "/app/data-export",
    ]

    def __init__(self, output_dir: Path):
        self.output_dir = output_dir
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.screenshots_dir = output_dir / "screenshots"
        self.screenshots_dir.mkdir(exist_ok=True)

        self.results = []
        self.api_results = []

    def test_backend_api(self):
        """测试后端API"""
        print(f"\n测试后端API: {self.BACKEND_URL}")

        import requests

        for endpoint in self.API_ENDPOINTS:
            url = f"{self.BACKEND_URL}{endpoint}"
            result = {
                'endpoint': endpoint,
                'url': url,
                'status': 'UNKNOWN',
                'response_code': None,
                'has_data': False,
                'data_count': 0,
                'error': None,
                'solution': None,
                'test_time': datetime.datetime.now().isoformat(),
            }

            try:
                response = requests.get(url, timeout=10)
                result['response_code'] = response.status_code

                if response.status_code == 200:
                    data = response.json()
                    result['status'] = 'PASS'

                    # 检查是否有数据
                    if isinstance(data, dict):
                        # 检查常见的数据字段
                        if 'data' in data:
                            inner_data = data['data']
                            if isinstance(inner_data, list):
                                result['data_count'] = len(inner_data)
                                result['has_data'] = len(inner_data) > 0
                            elif isinstance(inner_data, dict):
                                result['has_data'] = True
                        elif 'list' in data:
                            result['data_count'] = len(data['list'])
                            result['has_data'] = len(data['list']) > 0
                        elif 'records' in data:
                            result['data_count'] = len(data['records'])
                            result['has_data'] = len(data['records']) > 0
                        else:
                            result['has_data'] = True  # 有响应就算有数据

                    if not result['has_data']:
                        result['status'] = 'NO_DATA'
                        result['error'] = 'API返回空数据'
                        result['solution'] = '数据库无数据，需添加Mock数据或运行定时任务生成数据'

                elif response.status_code == 401:
                    result['status'] = 'AUTH_REQUIRED'
                    result['error'] = '需要认证'
                    result['solution'] = '添加认证token或使用已登录session'

                elif response.status_code == 404:
                    result['status'] = 'NOT_FOUND'
                    result['error'] = 'API不存在'
                    result['solution'] = '检查Controller是否正确注册路由'

                elif response.status_code == 500:
                    result['status'] = 'SERVER_ERROR'
                    result['error'] = '服务器内部错误'
                    result['solution'] = '检查后端日志，修复服务端异常'

                else:
                    result['status'] = 'FAIL'
                    result['error'] = f'异常状态码: {response.status_code}'

            except requests.exceptions.ConnectionError:
                result['status'] = 'CONNECTION_ERROR'
                result['error'] = '无法连接到后端服务'
                result['solution'] = '启动后端服务: cd code/java/zrws-approval && mvn spring-boot:run'

            except requests.exceptions.Timeout:
                result['status'] = 'TIMEOUT'
                result['error'] = '请求超时'
                result['solution'] = '检查后端服务是否正常运行'

            except Exception as e:
                result['status'] = 'ERROR'
                result['error'] = str(e)

            self.api_results.append(result)
            status_icon = '✓' if result['status'] == 'PASS' else '✗'
            data_icon = '有数据' if result['has_data'] else '无数据'
            print(f"  {status_icon} {endpoint}: {result['status']} ({data_icon})")

    def test_frontend_pages(self):
        """测试前端页面"""
        print(f"\n测试前端页面: {self.FRONTEND_URL}")

        with sync_playwright() as p:
            browser = p.chromium.launch(headless=True)
            page = browser.new_page(viewport={'width': 1920, 'height': 1080})

            for route in self.PAGE_ROUTES:
                url = f"{self.FRONTEND_URL}{route}"
                result = {
                    'page_name': route.replace("/app/", "").replace("/", "home") or "home",
                    'route': route,
                    'url': url,
                    'status': 'UNKNOWN',
                    'data_status': 'UNKNOWN',
                    'issues': [],
                    'solution': None,
                    'screenshot': None,
                    'test_time': datetime.datetime.now().isoformat(),
                }

                try:
                    page.goto(url, timeout=15000, wait_until='networkidle')
                    
                    # 截图
                    screenshot_path = self.screenshots_dir / f"{result['page_name']}.png"
                    page.screenshot(path=str(screenshot_path), full_page=True)
                    result['screenshot'] = str(screenshot_path)

                    # 检查数据状态
                    has_data = self._check_page_data(page)
                    result['data_status'] = 'HAS_DATA' if has_data else 'NO_DATA'

                    if not has_data:
                        result['issues'].append('页面无数据')
                        result['solution'] = '数据库无数据，需添加Mock数据或运行定时任务'

                    result['status'] = 'PASS' if not result['issues'] else 'FAIL'

                except PlaywrightTimeout:
                    result['status'] = 'TIMEOUT'
                    result['issues'].append('页面加载超时')
                    result['solution'] = '检查前端服务是否启动，检查API响应时间'

                except Exception as e:
                    result['status'] = 'ERROR'
                    result['issues'].append(str(e))

                self.results.append(result)
                status_icon = '✓' if result['status'] == 'PASS' else '✗'
                print(f"  {status_icon} {route}: {result['status']}")

            browser.close()

    def _check_page_data(self, page) -> bool:
        """检查页面是否有数据"""
        # 检查空状态提示
        empty_indicators = ['暂无数据', '无数据', 'No data', 'Empty']
        for indicator in empty_indicators:
            if page.locator(f'text="{indicator}"').count() > 0:
                return False

        # 检查表格行数
        table_rows = page.locator('table tbody tr, .el-table__row').count()
        if table_rows > 0:
            return True

        # 检查卡片数
        cards = page.locator('.stat-card, .card, .item-card').count()
        if cards > 3:
            return True

        return False

    def generate_excel_report(self):
        """生成Excel报告"""
        timestamp = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
        excel_path = self.output_dir / f"local_test_report_{timestamp}.xlsx"

        with pd.ExcelWriter(excel_path, engine='openpyxl') as writer:
            # API测试结果
            api_df = pd.DataFrame([
                {
                    'API端点': r['endpoint'],
                    'URL': r['url'],
                    '状态': r['status'],
                    '响应码': r['response_code'],
                    '是否有数据': '有' if r['has_data'] else '无',
                    '数据条数': r['data_count'],
                    '错误': r['error'] or '无',
                    '解决方案': r['solution'] or '无需修复',
                    '测试时间': r['test_time'],
                }
                for r in self.api_results
            ])
            api_df.to_excel(writer, sheet_name='API测试', index=False)

            # 页面测试结果
            page_df = pd.DataFrame([
                {
                    '页面名称': r['page_name'],
                    '路由': r['route'],
                    '状态': r['status'],
                    '数据状态': r['data_status'],
                    '问题': '\n'.join(r['issues']) if r['issues'] else '无',
                    '解决方案': r['solution'] or '无需修复',
                    '截图': r['screenshot'] or '无',
                    '测试时间': r['test_time'],
                }
                for r in self.results
            ])
            page_df.to_excel(writer, sheet_name='页面测试', index=False)

            # 问题汇总
            all_issues = []
            for r in self.api_results:
                if r['status'] != 'PASS':
                    all_issues.append({
                        '类型': 'API',
                        '名称': r['endpoint'],
                        '状态': r['status'],
                        '问题描述': r['error'] or '无数据',
                        '解决方案': r['solution'] or '需检查',
                    })
            for r in self.results:
                if r['status'] != 'PASS':
                    all_issues.append({
                        '类型': '页面',
                        '名称': r['page_name'],
                        '状态': r['status'],
                        '问题描述': '\n'.join(r['issues']),
                        '解决方案': r['solution'] or '需检查',
                    })

            if all_issues:
                issues_df = pd.DataFrame(all_issues)
                issues_df.to_excel(writer, sheet_name='问题汇总', index=False)

        print(f"\nExcel报告已生成: {excel_path}")
        return excel_path


def main():
    """主函数"""
    output_dir = Path(__file__).parent / "reports"

    tester = LocalTester(output_dir)

    print("=" * 60)
    print("智壤卫士本地开发环境测试")
    print("=" * 60)

    # 测试后端API
    tester.test_backend_api()

    # 测试前端页面
    tester.test_frontend_pages()

    # 生成报告
    tester.generate_excel_report()

    print("\n测试完成！")


if __name__ == "__main__":
    main()