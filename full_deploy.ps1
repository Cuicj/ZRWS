$ErrorActionPreference = 'Continue'
$env:SSH_ASKPASS="e:\AIdeom\智壤卫士\askpass.bat"
$env:SSH_ASKPASS_REQUIRE="force"
$ssh = "C:\Program Files\Git\usr\bin\ssh.exe"
$server = "8.163.137.149"
$user = "root"

function Invoke-SSH {
    param([string]$Command)
    & $ssh -o StrictHostKeyChecking=no "$user@$server" $Command 2>&1
}

Write-Host "========================================" 
Write-Host "开始部署 v4.0.0 版本"
Write-Host "========================================"

# 步骤1: 拉取最新代码
Write-Host ""
Write-Host "[1/8] 拉取最新代码..."
$result = Invoke-SSH "cd /root/workspace/ZRWS; git pull origin main"
Write-Host $result

# 步骤2: Maven编译后端
Write-Host ""
Write-Host "[2/8] Maven编译后端 (预计3-5分钟)..."
$startTime = Get-Date
$result = Invoke-SSH "cd /root/workspace/ZRWS/code/java; mvn clean package -DskipTests"
$endTime = Get-Date
$duration = ($endTime - $startTime).TotalMinutes
Write-Host "Maven编译耗时: $([math]::Round($duration, 1)) 分钟"
if ($result -like "*BUILD SUCCESS*") {
    Write-Host "Maven编译成功"
} else {
    Write-Host "Maven编译输出: $result"
}

# 步骤3: 部署JAR
Write-Host ""
Write-Host "[3/8] 部署JAR..."
$result = Invoke-SSH "cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar"
Write-Host $result

# 步骤4: 构建前端
Write-Host ""
Write-Host "[4/8] 构建前端..."
$startTime = Get-Date
$result = Invoke-SSH "cd /root/workspace/ZRWS/code/html; npm run build"
$endTime = Get-Date
$duration = ($endTime - $startTime).TotalMinutes
Write-Host "前端构建耗时: $([math]::Round($duration, 1)) 分钟"
if ($result -like "*build complete*" -or $result -like "*dist*") {
    Write-Host "前端构建成功"
} else {
    Write-Host "前端构建输出: $result"
}

# 步骤5: 部署前端
Write-Host ""
Write-Host "[5/8] 部署前端..."
$result = Invoke-SSH "rm -rf /var/www/zrws/*; cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/"
Write-Host $result

# 步骤6: 部署公告栏
Write-Host ""
Write-Host "[6/8] 部署公告栏..."
$result = Invoke-SSH "mkdir -p /app/announcement; cp /root/workspace/ZRWS/公告栏.html /app/announcement/公告栏.html"
Write-Host $result

# 步骤7: 重启服务
Write-Host ""
Write-Host "[7/8] 重启服务 (等待30秒)..."
$result = Invoke-SSH "systemctl restart zrws.service; sleep 30"
Write-Host $result

# 步骤8: 验证
Write-Host ""
Write-Host "[8/8] 验证部署..."
Write-Host "检查后端健康状态..."
$result1 = Invoke-SSH "curl -s http://localhost:5571/approval/actuator/health"
Write-Host "后端健康状态: $result1"

Write-Host "检查前端访问..."
$result2 = Invoke-SSH "curl -I -s http://localhost/TZ"
Write-Host "前端响应: $result2"

Write-Host ""
Write-Host "========================================" 
Write-Host "部署完成"
Write-Host "========================================"
