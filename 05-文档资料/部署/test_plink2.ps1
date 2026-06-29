$plinkPath = Join-Path $env:TEMP "plink.exe"
$hostKeyFile = Join-Path $env:TEMP "putty_hostkey.txt"

Write-Host "Getting host key..."

$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = $plinkPath
$psi.Arguments = "-ssh -P 22 -pw Test_admin -v root@8.163.137.149 echo hello"
$psi.UseShellExecute = $false
$psi.RedirectStandardInput = $true
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$psi.CreateNoWindow = $true

$p = New-Object System.Diagnostics.Process
$p.StartInfo = $psi
$p.Start() | Out-Null

Start-Sleep -Seconds 2

if (-not $p.HasExited) {
    Write-Host "Sending 'y' to accept host key..."
    $p.StandardInput.WriteLine("y")
    $p.StandardInput.Flush()
}

$p.WaitForExit(20000)

Write-Host "Exit Code: $($p.ExitCode)"
Write-Host "=== STDOUT ==="
Write-Host $p.StandardOutput.ReadToEnd()
Write-Host "=== STDERR ==="
Write-Host $p.StandardError.ReadToEnd()
