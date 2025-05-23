@echo off
echo ===============================================
echo           PA10 Chat Application
echo ===============================================
echo.

echo Step 1: Building the chat application...
call gradlew clean shadowJar
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Starting Chat Server...
start "Chat Server" cmd /k "java -jar app/build/libs/ChatApp.jar chat-server"

echo Waiting for server to start...
timeout /t 3

echo.
echo Step 3: Starting Chat Client 1...
start "Chat Client 1" cmd /k "java --module-path "C:\Program Files\Eclipse Adoptium\javafx-sdk-23.0.2\lib" --add-modules javafx.controls,javafx.fxml -jar app/build/libs/ChatApp.jar chat-client"

echo.
echo Step 4: Starting Chat Client 2...
start "Chat Client 2" cmd /k "java --module-path "C:\Program Files\Eclipse Adoptium\javafx-sdk-23.0.2\lib" --add-modules javafx.controls,javafx.fxml -jar app/build/libs/ChatApp.jar chat-client"

echo.
echo ===============================================
echo PA10 Chat is now running!
echo ===============================================
echo.
echo INSTRUCTIONS:
echo 1. Two chat client windows should open
echo 2. Both clients connect to localhost:8080 by default
echo 3. Click "Connect" in each client to join the chat
echo 4. Type messages and press Enter to send
echo 5. Messages appear in all connected clients
echo.
echo TO STOP THE CHAT:
echo 1. Close all chat client windows
echo 2. Close the server terminal (Chat Server)
echo.
echo If JavaFX modules aren't found, install JavaFX SDK to:
echo C:\Program Files\Eclipse Adoptium\javafx-sdk-23.0.2\
echo.
pause 