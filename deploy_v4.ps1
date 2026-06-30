# ZRWS v4.0.0 Deploy Script
param(
    [string]$HostName = "8.163.137.149",
    [int]$Port = 22,
    [string]$Username = "root",
    [string]$Password = "Test_admin",
    [string]$Version = "v4.0.0"
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
    
    Write-Step "Step 9: Deploy announcement board to /TZ"
    # Create announcement directory
    Invoke-SshCmd -Client $client -Command 'mkdir -p /app/announcement'
    
    # Copy announcement HTML to /app/announcement
    Invoke-SshCmd -Client $client -Command 'cp /root/workspace/ZRWS/公告栏.html /app/announcement/公告栏.html'
    Invoke-SshCmd -Client $client -Command 'ls -la /app/announcement/'
    
    # Update nginx config for /TZ path
    $nginxConfig = @'
# ZRWS Nginx Configuration with /TZ announcement path
server {
    listen 80;
    server_name www.zrws.cloud zrws.cloud;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name www.zrws.cloud zrws.cloud;

    ssl_certificate /etc/nginx/ssl/zrws.crt;
    ssl_certificate_key /etc/nginx/ssl/zrws.key;
    ssl_session_timeout 1d;
    ssl_protocols TLSv1.2 TLSv1.3;

    # Announcement board /TZ
    location /TZ {
        alias /app/announcement;
        index 公告栏.html;
        try_files $uri $uri/ /TZ/公告栏.html;
    }

    # Announcement API proxy
    location /TZ/api/ {
        rewrite ^/TZ/api/(.*) /$1 break;
        proxy_pass http://127.0.0.1:5571/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Main system API
    location /approval/ {
        proxy_pass http://127.0.0.1:5571/approval/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        client_max_body_size 50m;
    }

    # Main frontend
    location / {
        root /var/www/zrws;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
}
'@
    Invoke-SshCmd -Client $client -Command "cat > /etc/nginx/sites-available/zrws << 'NGINXEOF'"
    Write-Host "Nginx config prepared, updating..."
    
    # Write nginx config directly
    Invoke-SshCmd -Client $client -Command @"
cat > /etc/nginx/sites-available/zrws << 'NGINXEOF'
# ZRWS Nginx Configuration
upstream zrws_backend {
    server 127.0.0.1:5571;
    keepalive 32;
}

server {
    listen 80;
    server_name www.zrws.cloud zrws.cloud;
    return 301 https://\$server_name\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name www.zrws.cloud zrws.cloud;

    ssl_certificate /etc/nginx/ssl/zrws.crt;
    ssl_certificate_key /etc/nginx/ssl/zrws.key;

    # /TZ Announcement Board
    location /TZ {
        alias /app/announcement;
        index 公告栏.html;
        try_files \$uri \$uri/ /TZ/公告栏.html;
        
        location ~* \.(js|css|png|jpg|gif|ico|svg|woff|woff2)$ {
            expires 30d;
            add_header Cache-Control "public, immutable";
        }
        
        location ~* \.html$ {
            expires -1;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }
    }

    location /TZ/api/ {
        rewrite ^/TZ/api/(.*) /\$1 break;
        proxy_pass http://zrws_backend/;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # Main API
    location /approval/ {
        proxy_pass http://zrws_backend/approval/;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        client_max_body_size 50m;
    }

    # Frontend
    location / {
        root /var/www/zrws;
        index index.html;
        try_files \$uri \$uri/ /index.html;
        
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
            expires 7d;
            add_header Cache-Control "public, immutable";
        }
        
        location ~* \.html$ {
            expires -1;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }
    }
}
NGINXEOF
"@

    Invoke-SshCmd -Client $client -Command 'nginx -t'
    Invoke-SshCmd -Client $client -Command 'nginx -s reload'
    
    Write-Step "Step 10: Start backend service"
    Invoke-SshCmd -Client $client -Command 'systemctl start zrws.service'
    
    Write-Host "Waiting 30 seconds for service startup..."
    Start-Sleep -Seconds 30
    
    Write-Step "Step 11: Verify deployment"
    
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
        }
    } catch {
        Write-Host "Frontend check failed: $_" -ForegroundColor Red
    }
    
    Write-Host "`n--- Announcement /TZ check ---"
    try {
        $tzCode = Invoke-SshCmd -Client $client -Command "curl -s -o /dev/null -w '%{http_code}' http://localhost/TZ"
        Write-Host "TZ HTTP status: $tzCode"
        if ($tzCode -eq "200") {
            Write-Host "Announcement /TZ HTTP 200 OK" -ForegroundColor Green
        }
    } catch {
        Write-Host "TZ check failed: $_" -ForegroundColor Red
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
    Write-Host "  Main System:  https://www.zrws.cloud"
    Write-Host "  Announcement: https://www.zrws.cloud/TZ"
    Write-Host "  Backend API:  https://www.zrws.cloud/approval/"
    Write-Host "  Swagger:      https://www.zrws.cloud/approval/swagger-ui/index.html"
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
