@echo off
chcp 65001
echo ========================================
echo 安装 Spring AI 1.0.0-M2 到本地仓库
echo ========================================

set REPO_URL=https://repo.spring.io/milestone
set LIB_DIR=%~dp0lib
set MAVEN_CMD=mvn

:: 创建lib目录
if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"

echo.
echo 正在下载 Spring AI JAR 文件...
echo.

:: 1. spring-ai-core
if not exist "%LIB_DIR%\spring-ai-core-1.0.0-M2.jar" (
    echo [1/3] 下载 spring-ai-core-1.0.0-M2.jar ...
    powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%REPO_URL%/org/springframework/ai/spring-ai-core/1.0.0-M2/spring-ai-core-1.0.0-M2.jar' -OutFile '%LIB_DIR%\spring-ai-core-1.0.0-M2.jar'"
) else (
    echo [1/3] spring-ai-core-1.0.0-M2.jar 已存在
)

:: 2. spring-ai-openai
if not exist "%LIB_DIR%\spring-ai-openai-1.0.0-M2.jar" (
    echo [2/3] 下载 spring-ai-openai-1.0.0-M2.jar ...
    powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%REPO_URL%/org/springframework/ai/spring-ai-openai/1.0.0-M2/spring-ai-openai-1.0.0-M2.jar' -OutFile '%LIB_DIR%\spring-ai-openai-1.0.0-M2.jar'"
) else (
    echo [2/3] spring-ai-openai-1.0.0-M2.jar 已存在
)

:: 3. spring-ai-openai-spring-boot-starter
if not exist "%LIB_DIR%\spring-ai-openai-spring-boot-starter-1.0.0-M2.jar" (
    echo [3/3] 下载 spring-ai-openai-spring-boot-starter-1.0.0-M2.jar ...
    powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%REPO_URL%/org/springframework/ai/spring-ai-openai-spring-boot-starter/1.0.0-M2/spring-ai-openai-spring-boot-starter-1.0.0-M2.jar' -OutFile '%LIB_DIR%\spring-ai-openai-spring-boot-starter-1.0.0-M2.jar'"
) else (
    echo [3/3] spring-ai-openai-spring-boot-starter-1.0.0-M2.jar 已存在
)

echo.
echo ========================================
echo 安装到本地Maven仓库...
echo ========================================
echo.

cd /d "%~dp0"

:: 安装 spring-ai-core
echo 安装 spring-ai-core:1.0.0-M2 ...
call mvn install:install-file ^
    -Dfile="%LIB_DIR%\spring-ai-core-1.0.0-M2.jar" ^
    -DgroupId=org.springframework.ai ^
    -DartifactId=spring-ai-core ^
    -Dversion=1.0.0-M2 ^
    -Dpackaging=jar

:: 安装 spring-ai-openai
echo 安装 spring-ai-openai:1.0.0-M2 ...
call mvn install:install-file ^
    -Dfile="%LIB_DIR%\spring-ai-openai-1.0.0-M2.jar" ^
    -DgroupId=org.springframework.ai ^
    -DartifactId=spring-ai-openai ^
    -Dversion=1.0.0-M2 ^
    -Dpackaging=jar

:: 安装 spring-ai-openai-spring-boot-starter
echo 安装 spring-ai-openai-spring-boot-starter:1.0.0-M2 ...
call mvn install:install-file ^
    -Dfile="%LIB_DIR%\spring-ai-openai-spring-boot-starter-1.0.0-M2.jar" ^
    -DgroupId=org.springframework.ai ^
    -DartifactId=spring-ai-openai-spring-boot-starter ^
    -Dversion=1.0.0-M2 ^
    -Dpackaging=jar

echo.
echo ========================================
echo 安装完成！
echo ========================================
echo.
echo 现在可以移除 pom.xml 中的 system scope 配置了。
pause
