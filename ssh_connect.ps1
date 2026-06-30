$password = ConvertTo-SecureString "Test_admin" -AsPlainText -Force
$credential = New-Object System.Management.Automation.PSCredential ("root", $password)
Start-Process -FilePath "ssh" -ArgumentList "-o","StrictHostKeyChecking=no","root@8.163.137.149","echo Connected" -NoNewWindow -Wait -Credential $credential
