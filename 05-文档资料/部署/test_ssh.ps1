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
    Write-Host "SSH.NET downloaded successfully"
}

Add-Type -Path $sshNetPath
Write-Host "SSH.NET loaded"

$auth = New-Object Renci.SshNet.PasswordAuthenticationMethod("root", "Test_admin")
$connInfo = New-Object Renci.SshNet.ConnectionInfo("8.163.137.149", 22, "root", $auth)

$client = New-Object Renci.SshNet.SshClient($connInfo)
$client.Connect()
Write-Host "Connected to server!" -ForegroundColor Green

$cmd = $client.CreateCommand("uname -a && uptime && df -h /root")
$result = $cmd.EndExecute($cmd.BeginExecute())
Write-Host $result

$client.Disconnect()
Write-Host "Disconnected"
