#!/usr/bin/env python3
"""
智壤卫士网站自动化测试脚本
测试 https://www.zrws.cloud 所有页面
检测：无数据页面、按钮点击无效、API错误等问题
生成Excel报告并导出
"""

import os
import sys
import json
import time
import datetime
from pathlib import Path

# 添加项目路径
PROJECT_ROOT = Path(__file__).parent.parent.parent
sys.path.insert(0, str(PROJECT_ROOT))

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


class ZrwsTester:
    """智壤卫士网站测试器"""

    BASE_URL = "https://www.zrws.cloud"

    # 测试的页面路由列表
    PAGE_ROUTES = [
        "/",  # 登录页或门户页
        "/portal",
        "/app/dashboard",
        "/app/mission-list",
        "/app/flight-control",
        "/app/gps-track",
        "/app/soil-sample",
        "/app/data-import",
        "/app/data-export",
        "/app/quality-check",
        "/app/reconstruction",
        "/app/land-map",
        "/app/announcement-board",
        "/app/soil-classify",
        "/app/rock-stratum-analysis",
        "/app/disaster-risk",
        "/app/area-calc",
        "/app/cad-viewer",
        "/app/cad-compare",
        "/app/approval-list",
        "/app/workflow-design",
        "/app/device",
        "/app/user-manage",
        "/app/role-manage",
        "/app/sys-config",
        "/app/announcement",
        "/app/report-center",
        "/app/open-api-manage",
    ]

    def __init__(self, output_dir: Path):
        self.output_dir = output_dir
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.screenshots_dir = output_dir / "screenshots"
        self.screenshots_dir.mkdir(exist_ok=True)

        self.results = []
        self.browser = None
        self.page = None
        self.context = None

    def start_browser(self):
        """启动浏览器"""
        self.playwright = sync_playwright().start()
        self.browser = self.playwright.chromium.launch(
            headless=True,
            args=['--no-sandbox', '--disable-setuid-sandbox']
        )
        self.context = self.browser.new_context(
            viewport={'width': 1920, 'height': 1080},
            locale='zh-CN',
        )
        self.page = self.context.new_page()

        # 监听网络请求错误
        self.page.on("response", self._on_response)
        self.page.on("requestfailed", self._on_request_failed)

        # 监听console错误
        self.page.on("console", self._on_console)

        self.api_errors = []
        self.console_errors = []

    def _on_response(self, response):
        """监听响应"""
        if response.status >= 400:
            url = response.url
            status = response.status
            self.api_errors.append({
                'url': url,
                'status': status,
                'time': datetime.datetime.now().isoformat()
            })

    def _on_request_failed(self, request):
        """监听请求失败"""
        self.api_errors.append({
            'url': request.url,
            'status': 'FAILED',
            'error': request.failure,
            'time': datetime.datetime.now().isoformat()
        })

    def _on_console(self, msg):
        """监听console"""
        if msg.type in ['error', 'warning']:
            self.console_errors.append({
                'type': msg.type,
                'text': msg.text,
                'time': datetime.datetime.now().isoformat()
            })

    def close_browser(self):
        """关闭浏览器"""
        if self.browser:
            self.browser.close()
        if self.playwright:
            self.playwright.stop()

    def test_page(self, route: str):
        """测试单个页面"""
        url = f"{self.BASE_URL}{route}"
        page_name = route.replace("/app/", "").replace("/", "home") or "home"

        result = {
            'page_name': page_name,
            'route': route,
            'url': url,
            'test_time': datetime.datetime.now().isoformat(),
            'status': 'UNKNOWN',
            'issues': [],
            'data_status': 'UNKNOWN',
            'buttons_found': 0,
            'buttons_working': 0,
            'buttons_broken': 0,
            'api_errors': [],
            'console_errors': [],
            'screenshot': None,
            'solution': None,
        }

        self.api_errors.clear()
        self.console_errors.clear()

        try:
            print(f"测试页面: {url}")

            # 导航到页面
            self.page.goto(url, timeout=30000, wait_until='networkidle')

            # 截图
            screenshot_path = self.screenshots_dir / f"{page_name}.png"
            self.page.screenshot(path=str(screenshot_path), full_page=True)
            result['screenshot'] = str(screenshot_path)

            # 检查页面是否需要登录
            login_required = self._check_login_required()
            if login_required:
                result['status'] = 'LOGIN_REQUIRED'
                result['issues'].append('页面需要登录，请先登录后再测试')
                result['solution'] = '添加登录流程或使用已登录的session'
                return result

            # 检查数据状态
            data_status = self._check_data_status()
            result['data_status'] = data_status

            if data_status == 'NO_DATA':
                result['issues'].append('页面无数据显示')
                result['solution'] = '检查数据库是否有数据，或添加测试数据生成定时任务'

            # 测试按钮
            buttons_result = self._test_buttons()
            result['buttons_found'] = buttons_result['total']
            result['buttons_working'] = buttons_result['working']
            result['buttons_broken'] = buttons_result['broken']

            if buttons_result['broken'] > 0:
                for broken_btn in buttons_result['broken_details']:
                    result['issues'].append(f"按钮无效: {broken_btn['text']} - {broken_btn['reason']}")

            if buttons_result['broken'] > 0:
                result['solution'] = '检查按钮绑定的API是否正常，检查权限配置，检查前端事件绑定'

            # 记录API错误
            if self.api_errors:
                result['api_errors'] = self.api_errors.copy()
                for err in self.api_errors:
                    result['issues'].append(f"API错误: {err['url']} - 状态码 {err['status']}")

            # 记录Console错误
            if self.console_errors:
                result['console_errors'] = self.console_errors.copy()
                for err in self.console_errors[:5]:  # 只记录前5个
                    result['issues'].append(f"Console {err['type']}: {err['text'][:100]}")

            # 综合状态
            if not result['issues']:
                result['status'] = 'PASS'
            else:
                result['status'] = 'FAIL'

        except PlaywrightTimeout:
            result['status'] = 'TIMEOUT'
            result['issues'].append('页面加载超时')
            result['solution'] = '检查服务器响应时间，优化前端加载性能'

        except Exception as e:
            result['status'] = 'ERROR'
            result['issues'].append(f'测试异常: {str(e)}')
            result['solution'] = '检查网络连接，确认网站可访问'

        return result

    def _check_login_required(self) -> bool:
        """检查是否需要登录"""
        # 检查是否有登录表单
        login_form = self.page.locator('input[type="password"], .login-form, #login').first
        if login_form.is_visible():
            return True

        # 检查URL是否重定向到登录页
        current_url = self.page.url
        if '/login' in current_url or current_url == self.BASE_URL + '/':
            # 检查是否有登录按钮或表单
            has_login = self.page.locator('text=登录, text=Login, button:has-text("登录")').count() > 0
            if has_login:
                return True

        return False

    def _check_data_status(self) -> str:
        """检查页面数据状态"""
        # 检查常见的无数据提示
        no_data_indicators = [
            '暂无数据',
            '无数据',
            'No data',
            'Empty',
            '暂无记录',
            '没有数据',
            'data-empty',
            'empty-state',
            '.empty',
            '.no-data',
        ]

        for indicator in no_data_indicators:
            try:
                if self.page.locator(f'text="{indicator}", .{indicator}, [class*="{indicator}"]').count() > 0:
                    return 'NO_DATA'
            except:
                pass

        # 检查表格是否有数据行
        try:
            table_rows = self.page.locator('table tbody tr, .el-table__row, .data-row').count()
            if table_rows > 0:
                return 'HAS_DATA'
        except:
            pass

        # 检查列表卡片
        try:
            cards = self.page.locator('.card, .stat-card, .mission-card, .item-card').count()
            if cards > 0:
                return 'HAS_DATA'
        except:
            pass

        return 'UNKNOWN'

    def _test_buttons(self) -> dict:
        """测试页面按钮"""
        result = {
            'total': 0,
            'working': 0,
            'broken': 0,
            'broken_details': []
        }

        # 获取所有可点击按钮
        buttons = self.page.locator('button, .btn, .el-button, [role="button"], a.btn').all()
        result['total'] = len(buttons)

        for btn in buttons:
            try:
                # 获取按钮文本
                text = btn.inner_text(timeout=1000) or btn.get_attribute('aria-label') or '未知按钮'

                # 获取按钮状态
                is_disabled = btn.is_disabled()
                is_visible = btn.is_visible()

                if is_disabled:
                    result['broken'] += 1
                    result['broken_details'].append({
                        'text': text,
                        'reason': '按钮被禁用'
                    })
                    continue

                if not is_visible:
                    continue  # 跳过不可见按钮

                # 尝试点击（不跳转页面的按钮）
                current_url = self.page.url

                # 监听点击后的变化
                try:
                    btn.click(timeout=2000)
                    self.page.wait_for_timeout(500)

                    # 检查是否有错误弹窗
                    error_dialog = self.page.locator('.el-message--error, .error-message, .toast-error').count()
                    if error_dialog > 0:
                        result['broken'] += 1
                        result['broken_details'].append({
                            'text': text,
                            'reason': '点击后显示错误提示'
                        })
                        continue

                    # 检查是否有网络错误
                    if self.api_errors:
                        result['broken'] += 1
                        result['broken_details'].append({
                            'text': text,
                            'reason': '点击触发API错误'
                        })
                        continue

                    result['working'] += 1

                except PlaywrightTimeout:
                    result['broken'] += 1
                    result['broken_details'].append({
                        'text': text,
                        'reason': '点击无响应或超时'
                    })

            except Exception as e:
                # 某些按钮可能无法获取文本，跳过
                continue

        return result

    def run_all_tests(self):
        """运行所有测试"""
        print(f"开始测试 {self.BASE_URL}")
        print(f"共 {len(self.PAGE_ROUTES)} 个页面")

        self.start_browser()

        for route in self.PAGE_ROUTES:
            result = self.test_page(route)
            self.results.append(result)
            print(f"  - {result['page_name']}: {result['status']}")

        self.close_browser()

        print(f"\n测试完成，共 {len(self.results)} 个页面")

    def generate_excel_report(self):
        """生成Excel报告"""
        if not self.results:
            print("没有测试结果")
            return

        # 准备数据
        df_data = []
        for r in self.results:
            df_data.append({
                '页面名称': r['page_name'],
                '路由': r['route'],
                'URL': r['url'],
                '测试状态': r['status'],
                '数据状态': r['data_status'],
                '发现问题数': len(r['issues']),
                '问题详情': '\n'.join(r['issues']) if r['issues'] else '无',
                '按钮总数': r['buttons_found'],
                '有效按钮': r['buttons_working'],
                '无效按钮': r['buttons_broken'],
                'API错误数': len(r['api_errors']),
                'Console错误数': len(r['console_errors']),
                '建议解决方案': r['solution'] or '无需修复',
                '截图路径': r['screenshot'] or '无截图',
                '测试时间': r['test_time'],
            })

        df = pd.DataFrame(df_data)

        # 生成Excel文件
        timestamp = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
        excel_path = self.output_dir / f"test_report_{timestamp}.xlsx"

        with pd.ExcelWriter(excel_path, engine='openpyxl') as writer:
            # 主报告
            df.to_excel(writer, sheet_name='测试报告', index=False)

            # 问题汇总
            issues_df = df[df['发现问题数'] > 0][['页面名称', '路由', '问题详情', '建议解决方案']]
            issues_df.to_excel(writer, sheet_name='问题汇总', index=False)

            # 无数据页面
            no_data_df = df[df['数据状态'] == 'NO_DATA'][['页面名称', '路由', '建议解决方案']]
            if len(no_data_df) > 0:
                no_data_df.to_excel(writer, sheet_name='无数据页面', index=False)

            # API错误详情
            api_errors_data = []
            for r in self.results:
                for err in r['api_errors']:
                    api_errors_data.append({
                        '页面': r['page_name'],
                        'URL': err['url'],
                        '状态码': err['status'],
                        '时间': err['time'],
                    })
            if api_errors_data:
                api_errors_df = pd.DataFrame(api_errors_data)
                api_errors_df.to_excel(writer, sheet_name='API错误详情', index=False)

        print(f"Excel报告已生成: {excel_path}")
        return excel_path

    def generate_summary_json(self):
        """生成JSON摘要"""
        timestamp = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
        json_path = self.output_dir / f"test_summary_{timestamp}.json"

        summary = {
            'test_time': datetime.datetime.now().isoformat(),
            'base_url': self.BASE_URL,
            'total_pages': len(self.results),
            'pass_count': sum(1 for r in self.results if r['status'] == 'PASS'),
            'fail_count': sum(1 for r in self.results if r['status'] == 'FAIL'),
            'no_data_count': sum(1 for r in self.results if r['data_status'] == 'NO_DATA'),
            'login_required_count': sum(1 for r in self.results if r['status'] == 'LOGIN_REQUIRED'),
            'results': self.results,
        }

        with open(json_path, 'w', encoding='utf-8') as f:
            json.dump(summary, f, ensure_ascii=False, indent=2)

        print(f"JSON摘要已生成: {json_path}")
        return json_path


def main():
    """主函数"""
    # 输出目录
    output_dir = Path(__file__).parent / "reports"

    tester = ZrwsTester(output_dir)

    try:
        tester.run_all_tests()
        tester.generate_excel_report()
        tester.generate_summary_json()
    except KeyboardInterrupt:
        print("\n测试中断")
        tester.close_browser()
    except Exception as e:
        print(f"测试异常: {e}")
        tester.close_browser()
        raise


if __name__ == "__main__":
    main()