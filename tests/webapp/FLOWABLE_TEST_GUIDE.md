# 流程设计和AI功能测试指南

本文档说明如何运行和验证流程设计器功能。

---

## 1. 修复内容汇总

### 问题1: 前端API路径错误
**问题描述：** 前端调用 `/v1/designer/drafts` 等路径，但后端是 `/api/v1/designer/drafts`

**修复方案：** 修改 `src/api/flowable.js`，在所有API路径前添加 `/api` 前缀

### 问题2: 草稿数据未持久化
**问题描述：** FlowableDesignerService 使用内存 HashMap 存储草稿，服务重启后丢失

**修复方案：**
- 新增实体类 `BpmnDraft.java`
- 新增Mapper接口 `BpmnDraftMapper.java`
- 新增数据库表 `zrws_bpmn_draft`
- 修改 `FlowableDesignerService` 使用数据库持久化草稿

---

## 2. 新增文件列表

| 文件 | 说明 |
|------|------|
| `domain/entity/BpmnDraft.java` | 流程草稿实体类 |
| `mapper/BpmnDraftMapper.java` | 流程草稿Mapper接口 |
| `resources/sql/init_bpmn_draft.sql` | 数据库表初始化SQL |
| `test/FlowableDesignerTest.java` | 流程设计器单元测试 |
| `test/FlowableAITest.java` | AI流程生成单元测试 |

---

## 3. 运行测试

### 3.1 后端测试（Java）

```bash
cd code/java/zrws-approval

# 运行流程设计器测试
mvn test -Dtest=FlowableDesignerTest

# 运行AI功能测试
mvn test -Dtest=FlowableAITest

# 运行所有测试
mvn test
```

### 3.2 数据库初始化

```bash
# 执行数据库脚本
mysql -u root -p your_database < src/main/resources/sql/init_bpmn_draft.sql
```

或通过数据库管理工具执行 `init_bpmn_draft.sql` 中的SQL语句。

---

## 4. 测试用例说明

### 4.1 FlowableDesignerTest

| 测试方法 | 说明 | 预期结果 |
|---------|------|----------|
| `testSaveAndLoadDraft` | 测试草稿保存和加载 | 草稿正确保存并可加载 |
| `testDraftVersionIncrement` | 测试版本递增 | 更新后版本号+1 |
| `testListDrafts` | 测试草稿列表查询 | 返回所有草稿 |
| `testSubmitForReview` | 测试提交审核 | 状态变为PENDING_REVIEW |
| `testReview` | 测试审核通过/驳回 | 状态变为APPROVED/REJECTED |
| `testPublish` | 测试流程发布 | 状态变为PUBLISHED |
| `testXmlJsonConversion` | 测试XML与JSON互转 | 互转后内容一致 |
| `testValidateValidProcess` | 测试有效流程验证 | 验证通过 |
| `testValidateMissingStartEvent` | 测试无效流程验证 | 返回错误提示 |
| `testGetTemplates` | 测试获取模板 | 返回4个标准模板 |
| `testCreateFromTemplate` | 测试从模板创建 | 流程创建成功 |

### 4.2 FlowableAITest

| 测试方法 | 说明 | 预期结果 |
|---------|------|----------|
| `testAiServiceAvailable` | 测试AI服务是否注入 | AI服务可用 |
| `testProcessValidation` | 测试流程验证 | 流程定义有效 |
| `testTemplateRecommendation` | 测试模板推荐 | 推荐合适的模板 |
| `testFormFieldGeneration` | 测试表单字段生成 | 生成正确的表单字段 |
| `testConversationHistory` | 测试多轮对话 | 对话正常 |

---

## 5. API接口说明

### 5.1 草稿管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/designer/drafts` | GET | 获取草稿列表 |
| `/api/v1/designer/drafts/{processKey}` | GET | 获取单个草稿 |
| `/api/v1/designer/drafts` | POST | 保存草稿 |
| `/api/v1/designer/drafts/{processKey}/submit` | POST | 提交审核 |
| `/api/v1/designer/drafts/{processKey}/review` | POST | 审核草稿 |
| `/api/v1/designer/drafts/{processKey}/publish` | POST | 发布草稿 |
| `/api/v1/designer/drafts/{processKey}/deploy` | POST | 部署已发布流程 |

### 5.2 流程定义

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/designer/save` | POST | 保存并部署流程 |
| `/api/v1/designer/validate` | POST | 验证流程定义 |
| `/api/v1/designer/xml-to-json` | POST | XML转JSON |
| `/api/v1/designer/json-to-xml` | POST | JSON转XML |
| `/api/v1/designer/templates` | GET | 获取流程模板 |
| `/api/v1/designer/template/{templateKey}/create` | POST | 从模板创建流程 |

---

## 6. 流程生命周期

```
DRAFT → PENDING_REVIEW → APPROVED → PUBLISHED → DEPLOYED
                ↓
            REJECTED
```

| 状态 | 说明 |
|------|------|
| DRAFT | 草稿（可编辑） |
| PENDING_REVIEW | 待审核 |
| APPROVED | 已通过 |
| REJECTED | 已驳回（可重新编辑） |
| PUBLISHED | 已发布 |
| DEPLOYED | 已部署（运行中） |

---

## 7. 数据库表结构

### zrws_bpmn_draft

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| process_key | VARCHAR(100) | 流程唯一标识 |
| process_name | VARCHAR(200) | 流程名称 |
| description | TEXT | 流程描述 |
| xml | LONGTEXT | BPMN XML |
| json_def | LONGTEXT | JSON定义 |
| version | INT | 版本号 |
| status | VARCHAR(20) | 状态 |
| submit_time | DATETIME | 提交时间 |
| review_time | DATETIME | 审核时间 |
| review_comment | TEXT | 审核意见 |
| publish_time | DATETIME | 发布时间 |
| deployment_id | VARCHAR(100) | 部署ID |
| process_definition_id | VARCHAR(100) | 流程定义ID |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |
| creator_id | BIGINT | 创建人ID |
| creator_name | VARCHAR(100) | 创建人名称 |
| is_deleted | TINYINT | 删除标记 |

---

## 8. 验证修复效果

### 8.1 启动后端服务

```bash
cd code/java/zrws-approval
mvn spring-boot:run
```

### 8.2 启动前端服务

```bash
cd code/html
npm run dev
```

### 8.3 访问流程设计器

1. 打开浏览器访问 http://localhost:5173
2. 登录后进入"流程设计"页面
3. 创建新流程或编辑现有草稿
4. 保存草稿
5. 刷新页面，确认草稿数据仍然存在

### 8.4 运行测试验证

```bash
# 后端测试
cd code/java/zrws-approval
mvn test -Dtest=FlowableDesignerTest
```

---

## 9. 常见问题

### Q: 保存草稿失败？
A: 检查后端服务是否启动，检查数据库连接是否正常。

### Q: 草稿刷新后消失？
A: 确认数据库表 `zrws_bpmn_draft` 已创建，确认 `FlowableDesignerService` 使用的是数据库存储而非内存。

### Q: AI功能不可用？
A: 需要配置 OpenAI API Key，或设置 `spring.ai.openai.api-key` 环境变量。
