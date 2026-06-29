# ZRWS v3.4.0 Deploy Script
param(
    [string]$HostName = "8.163.137.149",
    [int]$Port = 22,
    [string]$Username = "root",
    [string]$Password = "Test_admin",
    [string]$Version = "v3.4.0"
)

$ErrorActionPreference = "Stop"

$sshNetPath = Join-Path $env:TEMP "SSH.NET.dll"
if (-not (Test-Path $sshNetPath)) {
    Write-Host "Downloading SSH.NET library..."
    $ProgressPreference = "SilentlyContinue"
    $zipPath = Join-Path $env:TEMP "sshnet.zip"
    Invoke-WebRequest -Uri "https://www.nuget.org/api/v2/package/SSH.NET/2024.1.0" -OutFile $zipPath
    $extractPath = Join-Path $env:TEMP "sshnet"
    Expand-Archive -Path $zipPath -DestinationPath $extractPath -Force
    $dllPath = Get-ChildItem -Path $extractPath -Recurse -Filter "Renci.SshNet.dll" | Select-Object -First 1 -ExpandProperty FullName
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

function Invoke-SshCmd {
    param(
        [Renci.SshNet.SshClient]$Client,
        [string]$Command,
        [int]$Timeout = 300
    )
    
    Write-Host "CMD: $Command" -ForegroundColor Gray
    $cmd = $Client.CreateCommand($Command)
    $cmd.CommandTimeout = [TimeSpan]::FromSeconds($Timeout)
    $asyncResult = $cmd.BeginExecute()
    $result = $cmd.EndExecute($asyncResult)
    $exitCode = $cmd.ExitStatus
    $stderr = $cmd.Error
    
    if ($result) {
        $lines = $result -split "`n"
        foreach ($line in $lines) {
            if ($line.Trim()) {
                Write-Host "  $line"
            }
        }
    }
    
    if ($stderr -and $exitCode -ne 0) {
        Write-Host "  [ERR] $stderr" -ForegroundColor Red
    }
    
    if ($exitCode -ne 0) {
        throw "Command failed (exit $exitCode): $Command"
    }
    
    return $result
}

try {
    Write-Step "Step 1: Connect to server"
    
    $authMethod = New-Object Renci.SshNet.PasswordAuthenticationMethod($Username, $Password)
    $connInfo = New-Object Renci.SshNet.ConnectionInfo($HostName, $Port, $Username, $authMethod)
    
    $client = New-Object Renci.SshNet.SshClient($connInfo)
    $client.Connect()
    Write-Host "Connected to ${Username}@${HostName}" -ForegroundColor Green
    
    $cmd1 = 'uname -a && uptime && df -h /root'
    Invoke-SshCmd -Client $client -Command $cmd1
    
    Write-Step "Step 2: Fetch and checkout $Version"
    Invoke-SshCmd -Client $client -Command 'cd /root/workspace/ZRWS && git fetch --tags'
    Invoke-SshCmd -Client $client -Command "cd /root/workspace/ZRWS && git checkout $Version"
    Invoke-SshCmd -Client $client -Command 'cd /root/workspace/ZRWS && git log --oneline -3'
    
    Write-Step "Step 3: Backup current JAR"
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupCmd = "cp /root/workspace/app.jar /root/workspace/app.jar.backup.$timestamp && ls -lh /root/workspace/app.jar.backup.* | tail -3"
    try {
        Invoke-SshCmd -Client $client -Command $backupCmd
    } catch {
        Write-Host "Backup warning: $_" -ForegroundColor Yellow
    }
    
    Write-Step "Step 4: Stop backend service"
    try {
        Invoke-SshCmd -Client $client -Command 'systemctl stop zrws.service'
        Start-Sleep -Seconds 3
        Invoke-SshCmd -Client $client -Command 'systemctl status zrws.service --no-pager | head -10'
    } catch {
        Write-Host "Stop service warning: $_" -ForegroundColor Yellow
    }
    
    Write-Step "Step 5: Maven build backend (3-5 min)"
    Invoke-SshCmd -Client $client -Command 'cd /root/workspace/ZRWS/code/java && mvn clean package -DskipTests' -Timeout 600
    Invoke-SshCmd -Client $client -Command 'ls -lh /root/workspace/ZRWS/code/java/zrws-approval/target/*.jar'
    
    Write-Step "Step 6: Deploy backend JAR"
    Invoke-SshCmd -Client $client -Command 'cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar'
    Invoke-SshCmd -Client $client -Command 'ls -lh /root/workspace/app.jar'
    
    Write-Step "Step 7: Build frontend (1-2 min)"
    
    $postcssFix = @'
if grep -q 'export default' /root/workspace/ZRWS/code/html/postcss.config.js 2>/dev/null; then
    echo "Converting postcss.config.js to CJS..."
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
    Invoke-SshCmd -Client $client -Command $postcssFix
    
    Invoke-SshCmd -Client $client -Command 'cd /root/workspace/ZRWS/code/html && npm run build' -Timeout 300
    Invoke-SshCmd -Client $client -Command 'ls -lh /root/workspace/ZRWS/code/html/dist/'
    
    Write-Step "Step 8: Deploy frontend to Nginx"
    Invoke-SshCmd -Client $client -Command 'rm -rf /var/www/zrws/*'
    Invoke-SshCmd -Client $client -Command 'cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/'
    Invoke-SshCmd -Client $client -Command 'ls -la /var/www/zrws/'
    Invoke-SshCmd -Client $client -Command 'ls /var/www/zrws/assets/ | head -10'
    
    Write-Step "Step 9: Start backend service"
    Invoke-SshCmd -Client $client -Command 'systemctl start zrws.service'
    
    Write-Host "Waiting 30 seconds for service startup..."
    Start-Sleep -Seconds 30
    
    Write-Step "Step 10: Verify deployment"
    
    Write-Host "`n--- Backend health check ---"
    try {
        $health = Invoke-SshCmd -Client $client -Command 'curl -s http://localhost:5571/approval/actuator/health'
        if ($health -match '"status":"UP"') {
            Write-Host "Backend health check: PASS" -ForegroundColor Green
        } else {
            Write-Host "Backend status: $health" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "Backend health check failed: $_" -ForegroundColor Red
    }
    
    Write-Host "`n--- Frontend check ---"
    try {
        $httpCode = Invoke-SshCmd -Client $client -Command "curl -s -o /dev/null -w '%{http_code}' http://localhost/"
        Write-Host "HTTP status: $httpCode"
        if ($httpCode -eq "200") {
            Write-Host "Frontend HTTP 200 OK" -ForegroundColor Green
        } else {
            Write-Host "Frontend HTTP status abnormal" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "Frontend check failed: $_" -ForegroundColor Red
    }
    
    Write-Host "`n--- Service status ---"
    try {
        Invoke-SshCmd -Client $client -Command 'systemctl status zrws.service --no-pager | head -15'
    } catch {}
    
    Write-Step "Deployment Complete!"
    $deployTime = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    Write-Host ""
    Write-Host "  ZRWS $Version deployed successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "  Frontend: https://www.zrws.cloud"
    Write-Host "  Backend:  https://www.zrws.cloud/approval/"
    Write-Host "  Swagger:  https://www.zrws.cloud/approval/swagger-ui/index.html"
    Write-Host ""
    Write-Host "  Version: $Version"
    Write-Host "  Time:    $deployTime"
    Write-Host ""
    Write-Host "  Tip: Hard refresh browser (Ctrl+F5) to see latest version"
    
} catch {
    Write-Host "`nDeployment FAILED: $_" -ForegroundColor Red
    throw
} finally {
    if ($client -and $client.IsConnected) {
        $client.Disconnect()
        Write-Host "`nSSH connection closed"
    }
}
