$HostAddr = "8.163.137.149"
$Port = 22
$Username = "root"
$Password = "Test_admin"
$PlinkPath = ".\plink.exe"
$RandFile = ".\putty.rnd"

$env:PUTTY_RND = (Resolve-Path .).Path + "\putty.rnd"

function Invoke-SSHCommand {
    param(
        [string]$Command,
        [int]$Timeout = 300
    )
    
    Write-Host ""
    Write-Host "============================================================"
    Write-Host "Executing: $Command"
    Write-Host "============================================================"
    
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $PlinkPath
    $psi.Arguments = "-ssh -P $Port -pw $Password -batch -hostkey aa:bb:cc:dd:ee:ff:00:11:22:33:44:55:66:77:88:99 $Username@$HostAddr `"$Command`""
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.RedirectStandardInput = $true
    $psi.CreateNoWindow = $true
    
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi
    $process.Start() | Out-Null
    
    $output = ""
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    
    while (-not $process.HasExited) {
        if ($stopwatch.Elapsed.TotalSeconds -gt $Timeout) {
            $process.Kill()
            Write-Host ""
            Write-Host "[ERROR] Command timeout (${Timeout}s)"
            return $false
        }
        
        try {
            $line = $process.StandardOutput.ReadLine()
            if ($line -ne $null) {
                Write-Host $line
                $output += $line + "`n"
            }
        } catch {}
        
        try {
            while (-not $process.StandardError.EndOfStream) {
                $errLine = $process.StandardError.ReadLine()
                if ($errLine -ne $null) {
                    Write-Host "ERR: $errLine"
                }
            }
        } catch {}
        
        Start-Sleep -Milliseconds 100
    }
    
    try {
        $remaining = $process.StandardOutput.ReadToEnd()
        if ($remaining) {
            Write-Host $remaining
            $output += $remaining
        }
    } catch {}
    
    try {
        $errRemaining = $process.StandardError.ReadToEnd()
        if ($errRemaining) {
            Write-Host "ERR: $errRemaining"
        }
    } catch {}
    
    $exitCode = $process.ExitCode
    
    if ($exitCode -ne 0) {
        Write-Host ""
        Write-Host "[ERROR] Command failed with exit code: $exitCode"
        return $false
    }
    
    Write-Host ""
    Write-Host "[OK] Command succeeded"
    return $true
}

Write-Host "============================================================"
Write-Host "  ZRWS v4.0.0 Full Deployment"
Write-Host "  Server: $HostAddr`:$Port"
Write-Host "============================================================"
Write-Host ""

Write-Host "Testing plink..."
& $PlinkPath -V
Write-Host ""

Write-Host "Connecting to server (first connection, accept host key)..."

$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = $PlinkPath
$psi.Arguments = "-ssh -P $Port -pw $Password $Username@$HostAddr `"echo 'SSH connected' && uname -a`""
$psi.UseShellExecute = $false
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$psi.RedirectStandardInput = $true
$psi.CreateNoWindow = $true

$process = New-Object System.Diagnostics.Process
$process.StartInfo = $psi
$process.Start() | Out-Null

Start-Sleep -Seconds 5

try {
    $process.StandardInput.WriteLine("y")
    $process.StandardInput.Flush()
} catch {}

Start-Sleep -Seconds 5

try {
    $output = $process.StandardOutput.ReadToEnd()
    Write-Host $output
} catch {}

try {
    $err = $process.StandardError.ReadToEnd()
    Write-Host "STDERR: $err"
} catch {}

$process.WaitForExit(20000)
$exitCode = $process.ExitCode
Write-Host "Exit code: $exitCode"

if ($exitCode -ne 0) {
    Write-Host ""
    Write-Host "Connection test failed, trying with auto-accept..."
}

$stepNames = @(
    "Step 1/11: Stop backend service",
    "Step 2/11: Backup current JAR",
    "Step 3/11: Fetch code and checkout v4.0.0",
    "Step 4/11: Maven build backend (skip tests)",
    "Step 5/11: Deploy backend JAR",
    "Step 6/11: Fix postcss config",
    "Step 7/11: Build frontend",
    "Step 8/11: Deploy frontend",
    "Step 9/11: Deploy announcement",
    "Step 10/11: Start backend service"
)

$stepCommands = @(
    "systemctl stop zrws.service",
    'cp /root/workspace/app.jar /root/workspace/app.jar.backup.$(date +%Y%m%d_%H%M%S)',
    "cd /root/workspace/ZRWS && git fetch --tags -f && git checkout v4.0.0",
    "cd /root/workspace/ZRWS/code/java && mvn clean install -Dmaven.test.skip=true",
    "cp /root/workspace/ZRWS/code/java/zrws-approval/target/zrws-approval-*.jar /root/workspace/app.jar",
    "printf 'module.exports = { plugins: { autoprefixer: {}, }, }\n' > /root/workspace/ZRWS/code/html/postcss.config.js",
    "cd /root/workspace/ZRWS/code/html && npm ls autoprefixer || npm install autoprefixer && npm run build",
    "rm -rf /var/www/zrws/* && cp -r /root/workspace/ZRWS/code/html/dist/* /var/www/zrws/",
    "mkdir -p /app/announcement && cp /root/workspace/ZRWS/公告栏.html /app/announcement/公告栏.html",
    "systemctl start zrws.service"
)

$stepTimeouts = @(30, 30, 120, 600, 30, 10, 300, 30, 10, 30)

for ($i = 0; $i -lt $stepNames.Length; $i++) {
    Write-Host ""
    Write-Host "############################################################"
    Write-Host "# $($stepNames[$i])"
    Write-Host "############################################################"
    
    $success = Invoke-SSHCommand -Command $stepCommands[$i] -Timeout $stepTimeouts[$i]
    if (-not $success) {
        Write-Host ""
        Write-Host "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        Write-Host "[FATAL] $($stepNames[$i]) FAILED - deployment aborted!"
        Write-Host "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        exit 1
    }
}

Write-Host ""
Write-Host "############################################################"
Write-Host "# Step 11/11: Wait 60s then verify deployment"
Write-Host "############################################################"
Write-Host ""
Write-Host "Waiting 60 seconds for service to start..."

for ($i = 60; $i -gt 0; $i -= 10) {
    Write-Host "  $i seconds remaining..."
    Start-Sleep -Seconds 10
}

Write-Host ""
Write-Host "--- Backend health check ---"
Invoke-SSHCommand -Command "curl -s http://localhost:5571/approval/actuator/health" -Timeout 30

Write-Host ""
Write-Host "--- Frontend /TZ check ---"
Invoke-SSHCommand -Command "curl -I http://localhost/TZ" -Timeout 30

Write-Host ""
Write-Host "--- Service status ---"
Invoke-SSHCommand -Command "systemctl status zrws.service" -Timeout 10

Write-Host ""
Write-Host ""
Write-Host "============================================================"
Write-Host "  Deployment Summary"
Write-Host "============================================================"
Write-Host "  Version: v4.0.0"
Write-Host "  Type: Full deployment (frontend + backend)"
Write-Host "  Backend: Started"
Write-Host "  Frontend: https://www.zrws.cloud"
Write-Host "  Backend API: https://www.zrws.cloud/approval/"
Write-Host "  Health check: https://www.zrws.cloud/approval/actuator/health"
Write-Host "============================================================"
