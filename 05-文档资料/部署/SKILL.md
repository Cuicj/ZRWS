---
name: frontend-deploy
description: Vue 3 + Vite 前端项目构建并部署到远程服务器（支持依赖版本修改、构建、SFTP 上传、Git 提交）
triggers:
  - "部署前端"
  - "构建并上传前端"
  - "重新部署 Vue 项目"
  - "修改依赖后重新部署"
  - "frontend deploy"
  - "build and deploy frontend"
---

# frontend-deploy

Vue 3 + Vite 前端项目的完整构建与部署流程。

## 适用场景

- 修改依赖版本后需要重新构建部署
- 前端代码修改后需要构建并上传到服务器
- `package.json` 配置错误需要修正后重新构建
- 需要将 `dist/` 构建产物上传到远程服务器

---

## 服务器信息（智壤卫士项目）

| 项目 | 值 |
|------|-----|
| 服务器 | `8.163.137.149` |
| SSH 端口 | `22` |
| 用户名 | `root` |
| 密码 | `Test_admin` |
| 前端部署目录 | `/var/www/zrws/` |
| Nginx 端口 | `80` |
| 本地前端路径 | `E:\AIdeom\智壤卫士\code\html` |

> **其他项目使用时，需询问用户服务器信息和本地路径。**

---

## 完整流程

### 第 1 步：修改依赖版本（可选）

```bash
# 编辑 package.json，修改对应依赖的版本号
# 例如：将 element-plus 从 ^2.9.0 改为 ^2.3.1
```

**关键检查项：**
- `package.json` 必须是 Vue 项目配置（包含 `vue`, `vite`, `@vitejs/plugin-vue`）
- 不能是 Node.js 项目的配置
- 必须有 `build` 脚本：`"build": "vite build"`

### 第 2 步：安装依赖

```bash
cd "/e/AIdeom/智壤卫士/code/html"
npm install
# 或指定版本：
npm install element-plus@2.3.1
```

### 第 3 步：构建前端

```bash
cd "/e/AIdeom/智壤卫士/code/html"
npm run build
# 生成 dist/ 目录
```

**常见构建错误及修复：**

| 错误信息 | 原因 | 修复 |
|----------|------|------|
| `Cannot find module 'tailwindcss'` | `postcss.config.js` 引用了 tailwindcss | 清理 `postcss.config.js`，只保留 `export default { plugins: [] }` |
| `Unable to parse HTML` | `index.html` 是静态 HTML（非 Vue 入口） | 重写 `index.html` 为 Vue 入口（只有 `<div id="app">`） |
| `vite: build-html` 报错 | HTML 中有未闭合的标签 | 检查 `index.html` 中的 Vue 模板语法是否正确 |

### 第 4 步：上传到服务器

**⚠️ 关键注意事项：SFTP 上传必须用 POSIX 路径格式（`/`）**

```python
# 错误示例（Windows 反斜杠）：
remote_file = f"{remote_dir}\\{rel_path}"  # ❌ 文件会放错位置

# 正确示例（POSIX 正斜杠）：
remote_file = f"{remote_dir}/{rel_path.as_posix()}"  # ✅
```

**上传步骤（Python + paramiko）：**

```python
import paramiko
from pathlib import Path

host = '8.163.137.149'
port = 22
username = 'root'
password = 'Test_admin'
local_dist = r'E:\AIdeom\智壤卫士\code\html\dist'
remote_dir = '/var/www/zrws'

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, port, username, password, timeout=10)
sftp = client.open_sftp()

# 1. 备份旧文件
import datetime
timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
client.exec_command(f'mv {remote_dir} {remote_dir}_backup_{timestamp}')

# 2. 创建新目录
client.exec_command(f'mkdir -p {remote_dir}')

# 3. 上传 dist/ 下所有文件
local_path = Path(local_dist)
for file_path in local_path.rglob('*'):
    if file_path.is_file():
        # 关键：用 as_posix() 转换路径格式
        rel_posix = file_path.relative_to(local_path).as_posix()
        remote_file = f"{remote_dir}/{rel_posix}"
        remote_parent = str(Path(remote_file).parent)
        client.exec_command(f'mkdir -p "{remote_parent}"')
        sftp.put(str(file_path), remote_file)

# 4. 设置权限
client.exec_command(f'chown -R www-data:www-data {remote_dir}')
client.exec_command(f'chmod -R 755 {remote_dir}')

# 5. 验证
stdin, stdout, stderr = client.exec_command(
    f'curl -s -o /dev/null -w "%{{http_code}}" http://localhost/',
    timeout=5
)
status = stdout.read().decode('utf-8').strip()
print(f"HTTP {status}")  # 应该是 200

sftp.close()
client.close()
```

### 第 5 步：验证部署

```bash
# 在服务器上执行
curl -s http://localhost/          # 应该返回 Vue 入口 HTML
curl -s -o /dev/null -w "%{http_code}" http://localhost/assets/index-xxx.js  # 应该是 200
```

### 第 6 步（可选）：提交到 Git

```bash
cd "/e/AIdeom/智壤卫士/code/html"
git add package.json package-lock.json
git commit -m "修改依赖版本：element-plus@2.3.1"
git push origin main
```

---

## 常见问题排查

### 问题 1：页面空白（白屏）

**原因：** `dist/` 上传时路径格式错误，导致 `assets/` 目录不存在。

**排查：**
```bash
# 在服务器上检查
ls -la /var/www/zrws/
ls -la /var/www/zrws/assets/   # 应该存在且有文件
```

**修复：** 重新上传，确保使用 `Path.as_posix()` 转换路径。

---

### 问题 2：404 错误

**原因：** 访问了错误的端口。

| URL | 说明 |
|-----|------|
| `http://8.163.137.149:5571/login.html` | ❌ 错误（5571 是后端端口，不提供前端） |
| `http://8.163.137.149/` | ✅ 正确（通过 Nginx 端口 80） |

---

### 问题 3：80 端口从外部无法访问

**原因：** 云服务商安全组未开放 80 端口。

**解决方案：** 去腾讯云/阿里云控制台，开放入站规则 80/tcp。

**验证：**
```bash
# 在服务器本地测试（应该返回 200）
curl -s -o /dev/null -w "%{http_code}" http://localhost/

# 从服务器用公网 IP 测试（如果超时，说明安全组未开放）
curl -s -o /dev/null -w "%{http_code}" http://8.163.137.149:80/
```

---

### 问题 4：`index.html` 是 85KB 的静态 HTML

**原因：** 项目目录中混用了 Node.js 项目和 Vue 项目的文件。

**修复：**
1. 备份原来的 `package.json`：`cp package.json package.json.backup`
2. 替换为 Vue 项目的 `package.json`
3. 重写 `index.html` 为 Vue 入口：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>项目标题</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

---

## 关键文件说明

| 文件 | 作用 | 注意事项 |
|------|------|----------|
| `package.json` | 项目配置和依赖 | 必须是 Vue 项目配置 |
| `vite.config.js` | Vite 构建配置 | 确保有 `plugins: [vue()]` |
| `postcss.config.js` | PostCSS 配置 | 如果不用 Tailwind CSS，需清理 |
| `index.html` | Vue 入口 | 必须在项目根目录，不能是 `public/` |
| `dist/` | 构建产物 | 添加到 `.gitignore` |
| `.gitignore` | Git 忽略文件 | 必须包含 `node_modules/`, `dist/` |

---

## 完整示例（智壤卫士项目）

```bash
# 1. 修改依赖版本
# 编辑 E:\AIdeom\智壤卫士\code\html\package.json
# 将 "element-plus": "^2.9.0" 改为 "^2.3.1"

# 2. 安装依赖
cd "/e/AIdeom/智壤卫士/code/html"
npm install element-plus@2.3.1

# 3. 构建
npm run build

# 4. 上传到服务器（用 Python 脚本）
# 参考上面的 Python 代码

# 5. 验证
# 浏览器访问 http://8.163.137.149/

# 6. 提交到 Git
git add package.json package-lock.json
git commit -m "降级 element-plus 到 2.3.1"
git push origin main
```

---

## 注意事项

1. **SFTP 上传路径必须用 `/`**（不能用 Windows 的 `\`）
2. **`dist/` 必须添加到 `.gitignore`**
3. **`package.json.backup` 是原配置备份**，不要提交到 Git
4. **每次构建后 JS/CSS 文件名会带 hash**，浏览器会自动获取最新版本
5. **如果页面空白，先检查 `assets/` 目录是否存在**
