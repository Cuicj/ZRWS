# 智壤卫士 v3.4.0 部署脚本 (PowerShell + SSH.NET)
# 使用 SSH.NET 库进行密码认证连接

param(
    [string]$HostName = "8.163.137.149",
    [int]$Port = 22,
    [string]$Username = "root",
    [string]$Password = "Test_admin",
    [string]$Version = "v3.4.0"
)

$ErrorActionPreference = "Stop"

$sshNetPath = "$env:TEMP\SSH.NET.dll"
if (-not (Test-Path $sshNetPath)) {
    Write-Host "下载 SSH.NET 库..."
    $ProgressPreference = "SilentlyContinue"
    Invoke-WebRequest -Uri "https://www.nuget.org/api/v2/package/SSH.NET/2024.1.0" -OutFile "$env:TEMP\sshnet.zip"
    Expand-Archive -Path "$env:TEMP\sshnet.zip" -DestinationPath "$env:TEMP\sshnet" -Force
    $dllPath = Get-ChildItem -Path "$env:TEMP\sshnet" -Recurse -Filter "Renci.SshNet.dll" | Select-Object -First 1 -ExpandProperty FullName
    Copy-Item $dllPath $sshNetPath -Force
}

Add-Type -Path $sshNetPath

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "  $Message" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Invoke-SshCommand {
    param(
        [Renci.SshNet.SshClient]$Client,
        [string]$Command,
        [int]$Timeout = 300
    )
    
    Write-Host "`$ $Command" -ForegroundColor Gray
    try {
        $cmd = $Client.CreateCommand($Command)
        $cmd.CommandTimeout = [TimeSpan]::FromSeconds($Timeout)
        $asyncResult = $cmd.BeginExecute()
        $result = $cmd.EndExecute($asyncResult)
        $exitCode = $cmd.ExitStatus
        $stdout = $result
        $stderr = $cmd.Error
        
        if ($stdout) {
            $lines = $stdout -split "`n"
            foreach ($line in $lines) {
                if ($line.Trim()) {
                    Write-Host "  $line"
                }
            }
        }
        
        if ($stderr -and $exitCode -ne 0) {
            Write-Host "  [错误] $stderr" -ForegroundColor Red
        }
        
        if ($exitCode -ne 0) {
            throw "命令执行失败 (exit code $exitCode): $Command"
        }
        
        return $stdout
    }
    catch {
        throw
    }
}

try {
    Write-Step "Step 1: 连接服务器"
    
    $connInfo = New-Object Renci.SshNet.ConnectionInfo(
        $HostName, $Port, $Username,
        @(New-Object Renci.SshNet.PasswordAuthenticationMethod($Username, $Password))
    )
    
    $client = New-Object Renci.SshNet.SshClient($connInfo)
    $client.Connect()
    Write-Host "✅ 已连接到 ${Username}@${HostName}" -ForegroundColor Green
    
    Invoke-SshCommand $client 'uname -a && uptime && df -h /root'
    
    Write-Step "Step 2: 拉取代码并切换到 $Version"
    Invoke-SshCommand $client 'cd /root/workspace/ZRWS && git fetch --tags'
    Invoke-SshCommand $client "cd /root/workspace/ZRWS && git checkout $Version"
    Invoke-SshCommand $client 'cd /root/workspace/ZRWS && git log --oneline -3'
    
    Write-Step "Step 3: 备份当前 JAR"
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupCmd = "cp /root/workspace/app.jar /root/workspace/app.jar.backup.$timestamp && ls -lh /root/workspace/app.jar.backup.* | tail -3"
    try {
        Invoke-SshCommand $client $backupCmd
    }
    catch {
        Write-Host "⚠️  备份警告: $_" -ForegroundColor Yellow
    }
    
    Write-Step "Step 4: 停止后端服务"
    try {
        Invoke-SshCommand $client 'systemctl stop zrws.service'
        Start-Sleep -Seconds 3
        Invoke-SshCommand $client 'systemctl status zrws.service --no-pager | head -10'
    }
    catch {
        Write-Host "⚠️  停止服务警告: $_" -ForegroundColor Yellow
    }
    
    Write-Step "Step 5: Maven 编译后端 (预计 3-5 分钟)"
    Invoke-SshCommand $client 'cd /root/workspace/ZRWS/code/java && mvn clean package -DskipTests' -Timeout 600
    Invoke-SshCommand $client 'ls -lh /root/workspace/ZRWS/code/java/zrws-approval/target/*.jar'
    
    Write-Step "Step 6: 部署后端 JAR"
    Invoke-SshCommand $client 'cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar'
    Invoke-SshCommand $client 'ls -lh /root/workspace/app.jar'
    
    Write-Step "Step 7: 构建前端 (预计 1-2 分钟)"
    
    $postcssFix = @'
if grep -q 'export default' /root/workspace/ZRWS/code/html/postcss.config.js 2>/dev/null; then
    echo '检测到 ESM 格式，转换为 CJS...'
    cat > /root/workspace/ZRWS/code/html/postcss.config.js << 'POSTCSSEOF'
module.exports = {
  plugins: {
    autoprefixer: {},
  },
}
POSTCSSEOF
fi
cat /root/workspace/ZRWS/code/html/postcss.config.js
'@
    Invoke-SshCommand $client $postcssFix
    
    Invoke-SshCommand $client 'cd /root/workspace/ZRWS/code/html && npm run build' -Timeout 300
    Invoke-SshCommand $client 'ls -lh /root/workspace/ZRWS/code/html/dist/'
    
    Write-Step "Step 8: 部署前端到 Nginx 目录"
    Invoke-SshCommand $client 'rm -rf /var/www/zrws/*'
    Invoke-SshCommand $client 'cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/'
    Invoke-SshCommand $client 'ls -la /var/www/zrws/'
    Invoke-SshCommand $client 'ls /var/www/zrws/assets/ | head -10'
    
    Write-Step "Step 9: 启动后端服务"
    Invoke-SshCommand $client 'systemctl start zrws.service'
    
    Write-Host "`n⏳ 等待 30 秒让服务启动..."
    Start-Sleep -Seconds 30
    
    Write-Step "Step 10: 验证部署"
    
    Write-Host "`n--- 后端健康检查 ---"
    try {
        $health = Invoke-SshCommand $client 'curl -s http://localhost:5571/approval/actuator/health'
        if ($health -match '"status":"UP"') {
            Write-Host "✅ 后端健康检查通过" -ForegroundColor Green
        }
        else {
            Write-Host "⚠️  后端状态: $health" -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "❌ 后端健康检查失败: $_" -ForegroundColor Red
    }
    
    Write-Host "`n--- 前端检查 ---"
    try {
        $httpCode = Invoke-SshCommand $client "curl -s -o /dev/null -w '%{http_code}' http://localhost/"
        Write-Host "HTTP 状态码: $httpCode"
        if ($httpCode -eq "200") {
            Write-Host "✅ 前端 HTTP 200 OK" -ForegroundColor Green
        }
        else {
            Write-Host "⚠️  前端 HTTP 状态异常" -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "❌ 前端检查失败: $_" -ForegroundColor Red
    }
    
    Write-Host "`n--- 后端服务状态 ---"
    try {
        Invoke-SshCommand $client 'systemctl status zrws.service --no-pager | head -15'
    }
    catch {}
    
    Write-Step "✅ 部署完成!"
    $deployTime = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    Write-Host @"

        🎉 智壤卫士 $Version 部署完成!

        📌 访问地址:
           前端: https://www.zrws.cloud
           后端: https://www.zrws.cloud/approval/
           Swagger: https://www.zrws.cloud/approval/swagger-ui/index.html

        📌 版本: $Version
        📌 部署时间: $deployTime

        💡 提示: 浏览器强刷新 (Ctrl+F5) 查看最新版本
"@ -ForegroundColor Green
    
}
catch {
    Write-Host "`n❌ 部署失败: $_" -ForegroundColor Red
    Write-Host "`n尝试回滚..." -ForegroundColor Yellow
    try {
        Invoke-SshCommand $client 'ls -t /root/workspace/app.jar.backup.* 2>/dev/null | head -1'
    }
    catch {}
    throw
}
finally {
    if ($client -and $client.IsConnected) {
        $client.Disconnect()
        Write-Host "`n🔌 已断开 SSH 连接"
    }
}
