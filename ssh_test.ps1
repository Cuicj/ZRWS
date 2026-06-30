$ErrorActionPreference = 'Stop'

$password = ConvertTo-SecureString "Test_admin" -AsPlainText -Force
$server = "8.163.137.149"
$user = "root"

$pinfo = New-Object System.Diagnostics.ProcessStartInfo
$pinfo.FileName = "ssh"
$pinfo.Arguments = "-o StrictHostKeyChecking=no ${user}@${server} ""echo 'SSH Connected Successfully' && uname -a"""
$pinfo.RedirectStandardInput = $true
$pinfo.RedirectStandardOutput = $true
$pinfo.RedirectStandardError = $true
$pinfo.UseShellExecute = $false
$pinfo.CreateNoWindow = $false
$pinfo.Username = $user
$pinfo.Password = $password

$p = New-Object System.Diagnostics.Process
$p.StartInfo = $pinfo
$p.Start() | Out-Null

Start-Sleep -Seconds 5

if (!$p.HasExited) {
    Write-Host "Process still running, output so far:"
    Write-Host $p.StandardOutput.ReadToEnd()
    Write-Host $p.StandardError.ReadToEnd()
    $p.Kill()
} else {
    Write-Host "Exit code: $($p.ExitCode)"
    Write-Host "Output: $($p.StandardOutput.ReadToEnd())"
    Write-Host "Error: $($p.StandardError.ReadToEnd())"
}
