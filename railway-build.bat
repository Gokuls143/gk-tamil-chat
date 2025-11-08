@echo off
echo ==========================================
echo Railway Deployment Build Script
echo ==========================================

echo Cleaning previous builds...
call ./gradlew clean

echo Building application...
call ./gradlew bootJar -x test

if %errorlevel% neq 0 (
    echo Build failed!
    exit /b 1
)

echo Build completed successfully!
echo JAR file location: build/libs/
dir build\libs\*.jar

echo ==========================================
echo Ready for Railway deployment!
echo ==========================================
echo.
echo Next steps for Railway:
echo 1. railway login
echo 2. railway link [your-project-id]
echo 3. railway up
echo.
echo Health check URL: https://your-app.railway.app/health
echo ==========================================