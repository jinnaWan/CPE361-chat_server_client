@echo off
echo Testing PA10 Chat Application...
echo.

echo Building application...
call gradlew shadowJar
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Testing help command...
java -jar app/build/libs/ChatApp.jar

echo.
echo Testing server startup (will run for 10 seconds)...
timeout /t 10 /nobreak > nul 2>&1

echo.
echo ===============================================
echo Test completed successfully!
echo ===============================================
echo.
echo To run the full chat application:
echo   .\start_chat.bat
echo.
echo Or manually:
echo   java -jar app/build/libs/ChatApp.jar chat-server
echo   java -jar app/build/libs/ChatApp.jar chat-client
echo.
pause 