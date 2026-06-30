# 智壤卫士网站测试框架

本目录包含完整的自动化测试脚本和数据生成定时任务。

---

## 目录结构

```
tests/webapp/
├── test_zrws_website.py      # 线上网站测试脚本 (www.zrws.cloud)
├── test_local.py             # 本地开发环境测试脚本
├── requirements.txt          # Python依赖
├── reports/                  # 测试报告输出目录
│   ├── screenshots/          # 页面截图
│   ├── test_report_*.xlsx    # Excel测试报告
│   └── test_summary_*.json   # JSON摘要
└── README.md                 # 本说明文档
```

---

## 安装依赖

```bash
cd tests/webapp
pip install -r requirements.txt
playwright install chromium
```

---

## 运行测试

### 1. 测试线上网站

```bash
python test_zrws_website.py
```

**测试内容：**
- 遍历所有页面路由
- 检测无数据页面
- 测试按钮点击有效性
- 监听API错误和Console错误
- 生成截图和Excel报告

### 2. 测试本地开发环境

```bash
# 先启动后端服务
cd code/java/zrws-approval
mvn spring-boot:run

# 再启动前端服务
cd code/html
npm run dev

# 运行测试
python test_local.py
```

**测试内容：**
- 后端API端点可用性
- API返回数据状态
- 前端页面加载和渲染
- 数据展示状态

---

## 测试报告说明

### Excel报告内容

| Sheet | 内容 |
|-------|------|
| 测试报告 | 所有页面测试结果汇总 |
| 问题汇总 | 仅包含FAIL状态的页面 |
| 无数据页面 | data_status为NO_DATA的页面 |
| API错误详情 | 所有API错误记录 |

### 字段说明

| 字段 | 说明 |
|------|------|
| status | PASS/FAIL/TIMEOUT/LOGIN_REQUIRED/ERROR |
| data_status | HAS_DATA/NO_DATA/UNKNOWN |
| buttons_found | 页面按钮总数 |
| buttons_working | 有效按钮数 |
| buttons_broken | 无效按钮数 |
| issues | 发现的问题列表 |
| solution | 建议的解决方案 |

---

## 常见问题及解决方案

### 1. 页面无数据

**问题原因：**
- 数据库表为空
- API返回空列表
- 前端未正确渲染数据

**解决方案：**
- 确认 MockDataInitializer 已执行
- 检查 Mapper 查询条件
- 启动每日数据生成定时任务

### 2. 按钮点击无效

**问题原因：**
- 前端事件未绑定
- API返回错误
- 权限不足导致操作被拒绝

**解决方案：**
- 检查按钮的@click/@click事件绑定
- 检查API Controller是否正常注册
- 检查权限配置

### 3. API返回404

**问题原因：**
- Controller未注册
- 路由路径错误
- Nginx代理配置错误

**解决方案：**
- 检查Controller注解路径
- 检查Nginx proxy_pass配置
- 检查前端API路径

### 4. 页面需要登录

**问题原因：**
- Session过期
- 需要认证token

**解决方案：**
- 添加登录流程到测试脚本
- 使用已登录的session状态
- 或跳过需要认证的页面测试

---

## 数据生成定时任务

### 后端定时任务配置

定时任务类：`DailyDataGenerator.java`

**执行时间：**
- 每日凌晨2点：生成新数据
- 每小时整点：更新设备状态

**生成数据类型：**
- 飞行任务（FlightMission）
- 土壤采样（SoilSample）
- 质量校验（QualityCheck）
- 公告（Announcement）
- 审批任务（ApprovalTask）
- 灾害风险（DisasterRisk）

### 启用定时任务

确保Spring Boot主类有 `@EnableScheduling` 注解：

```java
@SpringBootApplication
@EnableScheduling
public class ZrwsApprovalApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZrwsApprovalApplication.class, args);
    }
}
```

---

## 修复流程

1. **运行测试** → 生成Excel报告
2. **查看问题汇总** → 确认问题类型
3. **按解决方案修复** → 修改代码或添加数据
4. **重新运行测试** → 验证修复效果
5. **提交代码** → Git commit并创建tag

---

## 版本记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v3.4.2 | 2026-06-30 | 添加测试框架和数据生成定时任务 |