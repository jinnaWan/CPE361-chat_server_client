@echo off
echo Starting Fish Tank Application
echo ===============================

echo.
echo Building the application...
call gradlew.bat shadowJar
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Starting Fish Tank Server...
start "Fish Tank Server" cmd /k "java -jar app\build\libs\ChatApp.jar fishtank-server"

echo Waiting for server to start...
timeout /t 3 /nobreak > nul

echo.
echo Starting Fish Tank Client 1...
start "Fish Tank Client 1" cmd /k "java -jar app\build\libs\ChatApp.jar fishtank-client"

echo Waiting before starting next client...
timeout /t 2 /nobreak > nul

echo.
echo Starting Fish Tank Client 2...
start "Fish Tank Client 2" cmd /k "java -jar app\build\libs\ChatApp.jar fishtank-client"

echo.
echo Fish Tank application started!
echo - Server is running in a separate window
echo - Two client windows should appear
echo - Watch the fish move between windows!
echo.
echo Press any key to start a third client window...
pause > nul

echo Starting Fish Tank Client 3...
start "Fish Tank Client 3" cmd /k "java -jar app\build\libs\ChatApp.jar fishtank-client"

echo.
echo All done! Close this window when finished testing.
pause 