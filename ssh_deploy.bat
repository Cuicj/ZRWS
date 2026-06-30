@echo off
echo Test_admin | ssh -o StrictHostKeyChecking=no root@8.163.137.149 "echo Connected && uname -a"
