@echo off
setlocal enabledelayedexpansion

echo ================================================
echo Spring AI 1.0.0-M2 JAR包下载脚本
echo ================================================
echo.

set LIB_DIR=E:\AIdeom\智壤卫士\code\java\lib
set REPO_URL=https://repo.spring.io/milestone

echo 下载目录: %LIB_DIR%
echo.

echo 开始下载Spring AI 1.0.0-M2相关依赖...

:: 创建lib目录（如果不存在）
if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"

:: Spring AI Core
powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%REPO_URL%/org/springframework/ai/spring-ai-core/1.0.0-M2/spring-ai-core-1.0.0-M2.jar' -OutFile '%LIB_DIR%\spring-ai-core-1.0.0-M2.jar'"
echo [1/6] spring-ai-core-1.0.0-M2.jar

:: Spring AI OpenAI
powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%REPO_URL%/org/springframework/ai/spring-ai-openai/1.0.0-M2/spring-ai-openai-1.0.0-M2.jar' -OutFile '%LIB_DIR%\spring-ai-openai-1.0.0-M2.jar'"
echo [2/6] spring-ai-openai-1.0.0-M2.jar

:: Spring AI OpenAI Starter
powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%REPO_URL%/org/springframework/ai/spring-ai-openai-spring-boot-starter/1.0.0-M2/spring-ai-openai-spring-boot-starter-1.0.0-M2.jar' -OutFile '%LIB_DIR%\spring-ai-openai-spring-boot-starter-1.0.0-M2.jar'"
echo [3/6] spring-ai-openai-spring-boot-starter-1.0.0-M2.jar

:: Spring AI JSON Schema
powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%REPO_URL%/org/springframework/ai/spring-ai-json-schema/1.0.0-M2/spring-ai-json-schema-1.0.0-M2.jar' -OutFile '%LIB_DIR%\spring-ai-json-schema-1.0.0-M2.jar'"
echo [4/6] spring-ai-json-schema-1.0.0-M2.jar

:: Spring AI Transformers
powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%REPO_URL%/org/springframework/ai/spring-ai-transformers/1.0.0-M2/spring-ai-transformers-1.0.0-M2.jar' -OutFile '%LIB_DIR%\spring-ai-transformers-1.0.0-M2.jar'"
echo [5/6] spring-ai-transformers-1.0.0-M2.jar

:: Spring AI Vector Store
powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%REPO_URL%/org/springframework/ai/spring-ai-vector-store/1.0.0-M2/spring-ai-vector-store-1.0.0-M2.jar' -OutFile '%LIB_DIR%\spring-ai-vector-store-1.0.0-M2.jar'"
echo [6/6] spring-ai-vector-store-1.0.0-M2.jar

echo.
echo ================================================
echo 下载完成!
echo ================================================
echo 下载的JAR包已保存到: %LIB_DIR%
echo.
pause