# 快速部署前端修复 - 仅构建和部署前端
param(
    [string]$HostName = "8.163.137.149",
    [int]$Port = 22,
    [string]$Username = "root",
    [string]$Password = "Test_admin"
)

$ErrorActionPreference = "Stop"

$sshNetPath = Join-Path $env:TEMP "SSH.NET.dll"
if (-not (Test-Path $sshNetPath)) {
    Write-Host "Downloading SSH.NET library..."
    $ProgressPreference = "SilentContinue"
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
    Write-Step "Step 1: 连接服务器"
    
    $authMethod = New-Object Renci.SshNet.PasswordAuthenticationMethod($Username, $Password)
    $connInfo = New-Object Renci.SshNet.ConnectionInfo($HostName, $Port, $Username, $authMethod)
    
    $client = New-Object Renci.SshNet.SshClient($connInfo)
    $client.Connect()
    Write-Host "Connected to ${Username}@${HostName}" -ForegroundColor Green

    Write-Step "Step 2: 上传修复的文件"
    
    $sftp = $client.CreateSftpClient()
    $sftp.Connect()
    
    $repoDir = "/root/workspace/ZRWS"
    
    # 上传 request.js
    Write-Host "  上传 request.js..."
    $localRequest = "e:\AIdeom\智壤卫士\code\html\src\utils\request.js"
    $remoteRequest = "$repoDir/code/html/src/utils/request.js"
    $fs = [System.IO.File]::OpenRead($localRequest)
    $sftp.UploadFile($fs, $remoteRequest, $true)
    $fs.Close()
    Write-Host "    ✓ request.js" -ForegroundColor Green
    
    # 上传 API 文件
    $apiFiles = @("geoStandard.js", "flowable.js", "dataImport.js", "report.js", "dataExport.js", "openApi.js")
    foreach ($f in $apiFiles) {
        Write-Host "  上传 $f..."
        $localFile = "e:\AIdeom\智壤卫士\code\html\src\api\$f"
        $remoteFile = "$repoDir/code/html/src/api/$f"
        $fs = [System.IO.File]::OpenRead($localFile)
        $sftp.UploadFile($fs, $remoteFile, $true)
        $fs.Close()
        Write-Host "    ✓ $f" -ForegroundColor Green
    }
    
    # 上传 vite.config.js
    Write-Host "  上传 vite.config.js..."
    $localVite = "e:\AIdeom\智壤卫士\code\html\vite.config.js"
    $remoteVite = "$repoDir/code/html/vite.config.js"
    $fs = [System.IO.File]::OpenRead($localVite)
    $sftp.UploadFile($fs, $remoteVite, $true)
    $fs.Close()
    Write-Host "    ✓ vite.config.js" -ForegroundColor Green
    
    $sftp.Disconnect()
    Write-Host "✅ 文件上传完成" -ForegroundColor Green

    Write-Step "Step 3: 构建前端 (预计 1-2 分钟)"
    Invoke-SshCmd -Client $client -Command "cd $repoDir/code/html && npm run build" -Timeout 300
    Invoke-SshCmd -Client $client -Command "ls -lh $repoDir/code/html/dist/"

    Write-Step "Step 4: 部署前端到 Nginx 目录"
    $frontendDir = "/var/www/zrws"
    Invoke-SshCmd -Client $client -Command "rm -rf $frontendDir/*"
    Invoke-SshCmd -Client $client -Command "cp -r $repoDir/code/html/dist/* $frontendDir/"
    Invoke-SshCmd -Client $client -Command "ls $frontendDir/assets/ | head -10"

    Write-Step "Step 5: 验证部署"
    $result = Invoke-SshCmd -Client $client -Command "curl -s -o /dev/null -w '%{http_code}' http://localhost/"
    $status = $result[-1]
    if ($status -eq "200") {
        Write-Host "✅ 前端 HTTP 200 OK" -ForegroundColor Green
    } else {
        Write-Host "⚠️  前端 HTTP 状态: $status" -ForegroundColor Yellow
    }

    Write-Step "部署完成！"
    Write-Host ""
    Write-Host "  修复内容:" -ForegroundColor White
    Write-Host "  1. 修复 request.js baseURL: /api → /approval/api"
    Write-Host "  2. 修复 6个API文件路径前缀问题"
    Write-Host "  3. 同步更新 vite.config.js 代理配置"
    Write-Host ""
    Write-Host "  请刷新浏览器页面 (Ctrl+F5) 查看效果" -ForegroundColor Green
    Write-Host ""
    
} catch {
    Write-Host "`n❌ 部署失败: $_" -ForegroundColor Red
    throw
} finally {
    if ($client -and $client.IsConnected) {
        $client.Disconnect()
        Write-Host "`n🔌 SSH 连接已关闭"
    }
}
