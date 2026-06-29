$plinkPath = Join-Path $env:TEMP "plink.exe"

$process = New-Object System.Diagnostics.Process
$process.StartInfo.FileName = $plinkPath
$process.StartInfo.Arguments = "-ssh -P 22 -pw Test_admin root@8.163.137.149 `"uname -a && uptime && df -h /root`""
$process.StartInfo.UseShellExecute = $false
$process.StartInfo.RedirectStandardInput = $true
$process.StartInfo.RedirectStandardOutput = $true
$process.StartInfo.RedirectStandardError = $true
$process.StartInfo.CreateNoWindow = $true

$process.Start() | Out-Null

$stdin = $process.StandardInput
$stdin.WriteLine("y")
$stdin.Close()

$output = $process.StandardOutput.ReadToEnd()
$error = $process.StandardError.ReadToEnd()
$process.WaitForExit(15000)

Write-Host "Exit Code: $($process.ExitCode)"
Write-Host "Output:"
Write-Host $output
if ($error) {
    Write-Host "Error:"
    Write-Host $error
}
