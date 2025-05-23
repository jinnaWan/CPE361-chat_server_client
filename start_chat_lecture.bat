@echo off
echo ===============================================
echo     PA10 Chat Application (Lecture Style)
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
echo Step 2: Starting Chat Server (console-based)...
start "Chat Server" cmd /k "java -jar app/build/libs/ChatApp.jar chat-server"

echo Waiting for server to start...
timeout /t 3

echo.
echo Step 3: Starting Chat Client 1 (console-based)...
start "Chat Client 1" cmd /k "java -jar app/build/libs/ChatApp.jar chat-client"

echo.
echo Step 4: Starting Chat Client 2 (console-based)...
start "Chat Client 2" cmd /k "java -jar app/build/libs/ChatApp.jar chat-client"

echo.
echo ===============================================
echo PA10 Chat is now running (Lecture Style)!
echo ===============================================
echo.
echo INSTRUCTIONS:
echo 1. Two console chat client windows should open
echo 2. Both clients automatically connect to localhost:12345
echo 3. Start typing messages in either client window
echo 4. Messages will appear in both clients
echo 5. Type 'quit' to exit a client
echo.
echo CHAT FEATURES (following lecture requirements):
echo - ServerThread extends Thread (as in lecture)
echo - ClientHandler extends Thread (as in lecture)  
echo - ClientThread has two threads: sender and receiver (as required)
echo - Uses DataInputStream/DataOutputStream (as in lecture examples)
echo - Maintains list of client output streams for broadcasting
echo - Console-based interface (as in lecture examples)
echo.
echo TO STOP THE CHAT:
echo 1. Type 'quit' in client windows to exit
echo 2. Close the server terminal (Chat Server) with Ctrl+C
echo.
pause 