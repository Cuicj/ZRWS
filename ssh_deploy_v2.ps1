$ErrorActionPreference = 'Continue'
$output = ""

function Run-SSHCommand {
    param(
        [string]$Host,
        [string]$User,
        [string]$Password,
        [string]$Command
    )
    
    $secPassword = ConvertTo-SecureString $Password -AsPlainText -Force
    $credential = New-Object System.Management.Automation.PSCredential($User, $secPassword)
    
    # Use Start-Process with some workaround
    $tempScript = @"
cd /root/workspace/ZRWS && git pull origin main
"@
    
    $processInfo = New-Object System.Diagnostics.ProcessStartInfo
    $processInfo.FileName = "ssh"
    $processInfo.Arguments = "-o StrictHostKeyChecking=no ${User}@${Host} `"$Command`""
    $processInfo.RedirectStandardInput = $true
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true
    $processInfo.UseShellExecute = $false
    $processInfo.CreateNoWindow = $true
    $processInfo.WindowStyle = 'Hidden'
    
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $processInfo
    $process.Start() | Out-Null
    
    # Give password
    Start-Sleep -Milliseconds 500
    $process.StandardInput.WriteLine($Password)
    $process.StandardInput.Close()
    
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit(60000)
    
    return @{
        ExitCode = $process.ExitCode
        StdOut = $stdout
        StdErr = $stderr
    }
}

# Test connection
Write-Host "=== 测试SSH连接 ==="
$result = Run-SSHCommand -Host "8.163.137.149" -User "root" -Password "Test_admin" -Command "echo 'Connected' && uname -a"
Write-Host "Exit Code: $($result.ExitCode)"
Write-Host "Output: $($result.StdOut)"
Write-Host "Error: $($result.StdErr)"
