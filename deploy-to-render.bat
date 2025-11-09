@echo off
REM deploy-to-render.bat
REM Quick deployment script for Render (Windows)

echo 🚀 Preparing GK Tamil Chat for Render deployment...

REM Set the profile for Render
set SPRING_PROFILES_ACTIVE=render

REM Build the application
echo 📦 Building application...
gradlew.bat build -x test

if %ERRORLEVEL% EQU 0 (
    echo ✅ Build successful!
    echo 📋 Next steps:
    echo 1. Push your code to GitHub
    echo 2. Create services on Render:
    echo    - PostgreSQL database ^(recommended^)
    echo    - Web service connected to your GitHub repo
    echo 3. Set environment variables:
    echo    - SPRING_PROFILES_ACTIVE=render
    echo    - DATABASE_URL=^<your_postgres_url^>
    echo    - DB_USERNAME=^<your_db_user^>
    echo    - DB_PASSWORD=^<your_db_password^>
    echo.
    echo 📖 See RENDER-DEPLOYMENT.md for detailed instructions
) else (
    echo ❌ Build failed! Check the error messages above.
)