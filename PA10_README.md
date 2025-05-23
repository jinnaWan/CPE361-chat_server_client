# PA10: Chat Client-Server Application (Lecture Style)

This is a chat application implementing the PA10 assignment requirements following the **exact structure and patterns from the lecture materials**:

> **PA10**: Create a chat client and server using Java threading and networking. Multiple clients should be able to connect to a single server and exchange messages in real-time.

## What This Implementation Does

✅ **ServerThread extends Thread**: Exactly as shown in lecture examples  
✅ **ClientHandler extends Thread**: Exactly as shown in lecture examples  
✅ **Console-based ClientThread**: Two threads (sender/receiver) as required  
✅ **DataInputStream/DataOutputStream**: Using exact same I/O classes as lectures  
✅ **List of client output streams**: For broadcasting as specified in lecture  
✅ **Real-time messaging**: Messages broadcast to all connected clients  

## Architecture (Following Lecture Structure)

### ServerThread.java
- **Extends Thread** (as in lecture examples)
- **TCP Server** listening on port 12345
- **Maintains static List<DataOutputStream>** for broadcasting (as required)
- **Creates ClientHandler thread** for each client (exactly like lecture)
- **Uses DataInputStream/DataOutputStream** (as in lecture examples)

### ClientHandler.java  
- **Extends Thread** (as in lecture examples)
- **One handler per client** (as specified in lecture)
- **Uses DataInputStream/DataOutputStream** (as in lecture examples)
- **Reads messages and broadcasts** to other clients
- **Manages client join/leave notifications**

### ClientThread.java
- **Console-based client** (as in lecture examples)  
- **Two threads as required**:
  - **MessageSender**: Handles keyboard input (as specified in lecture)
  - **MessageReceiver**: Listens for server messages (as specified in lecture)
- **Uses DataInputStream/DataOutputStream** (as in lecture examples)
- **Scanner for console input** (simple console interface)

## Key Features (Following Lecture Requirements)

### Server Features ✅
- **Accepts multiple client connections** ✓
- **Spawns new ClientHandler thread per client** ✓ 
- **Maintains list of client output streams for broadcasting** ✓
- Uses exact same structure as lecture ServerThread examples

### Client Features ✅
- **Connects to the server** ✓
- **Has two threads** ✓:
  - **One for sending messages (keyboard input)** ✓
  - **One for receiving messages (from server)** ✓
- Console-based interface matching lecture examples

## Running the Chat Application

### Method 1: Automatic Launch (Windows)
```bash
.\start_chat_lecture.bat
```
This will:
1. Build the application 
2. Start the console-based chat server (ServerThread)
3. Launch two console-based chat clients (ClientThread)

### Method 2: Manual Steps

1. **Build the application:**
   ```bash
   ./gradlew shadowJar
   ```

2. **Start the chat server:**
   ```bash
   java -jar app/build/libs/ChatApp.jar chat-server
   ```

3. **Start chat clients:**
   ```bash
   java -jar app/build/libs/ChatApp.jar chat-client
   ```

## How to Use

### Starting a Chat Session
1. Run the server first (console window will show "Waiting for client...")
2. Launch one or more chat clients (each opens in new console window)
3. Clients automatically connect to localhost:12345
4. Start typing messages in any client window!
5. Type `quit` to exit a client

### What You'll See

#### Server Console
```
Chat Server started on port 12345
Waiting for client...
Connected to /127.0.0.1:54321
Client added to broadcast list. Total clients: 1
Waiting for client...
Connected to /127.0.0.1:54322
Client added to broadcast list. Total clients: 2
Broadcasting: *** User 1 joined the chat ***
Received from User 1: Hello everyone!
Broadcasting: User 1: Hello everyone!
```

#### Client Console
```
=== PA10 Chat Client ===
Connecting to chat server...
Connecting to localhost on port 12345
Just connected to /127.0.0.1:12345
Welcome to the chat! You are User 1
*** You are now connected to the chat server ***
You can start typing messages (type 'quit' to exit):
Hello everyone!
*** User 2 joined the chat ***
User 2: Hi there!
```

## Technical Implementation (Lecture Compliance)

### Threading ✅
- **ServerThread Main Thread**: Accepts new client connections (extends Thread)
- **ClientHandler Threads**: One per client for message processing (extends Thread)  
- **Client Sender Thread**: Handles keyboard input (inner class implementing Runnable)
- **Client Receiver Thread**: Receives messages from server (inner class implementing Runnable)

### Networking ✅
- **TCP Sockets**: Reliable client-server communication
- **DataInputStream/DataOutputStream**: Exact I/O classes from lectures
- **Static List<DataOutputStream>**: For broadcasting (as specified in lecture)
- **Synchronized methods**: Thread-safe broadcasting

### Console Interface ✅
- **Scanner for input**: Simple keyboard input (as in lecture examples)
- **System.out for output**: Console-based display
- **Two-thread client design**: Exactly as specified in lecture requirements

## Assignment Requirements ✅

✅ **Server handles multiple clients concurrently** - ServerThread with ClientHandler threads  
✅ **Each client can send and receive messages in real-time** - Two-thread design  
✅ **Messages are broadcast to all connected clients** - Using list of output streams  
✅ **Server spawns new ClientHandler thread per client** - Exactly as in lecture  
✅ **Maintains list of client output streams for broadcasting** - Static CopyOnWriteArrayList  
✅ **Client has two threads** - MessageSender and MessageReceiver  
✅ **One for sending messages (keyboard input)** - Scanner-based input thread  
✅ **One for receiving messages (from server)** - DataInputStream reading thread  

## Lecture Structure Compliance 🎯

This implementation follows the **exact structure from lectures**:

1. **Same class names**: ServerThread, ClientHandler, ClientThread
2. **Same inheritance**: All extend Thread (as in examples)
3. **Same I/O classes**: DataInputStream/DataOutputStream (as in examples)  
4. **Same patterns**: Socket handling, thread management (as in examples)
5. **Same requirements**: Two-thread client, broadcast list (as specified)

## PA10 Complete! 🎉

This implementation demonstrates all the key concepts from the lecture:
- **Thread Programming**: Exact same patterns as lecture examples
- **Network Programming**: TCP client-server using same I/O classes
- **Console Interface**: Simple, functional interface matching lecture style
- **Broadcasting**: List of output streams exactly as specified in requirements

The chat system provides a practical example following the exact lecture structure and requirements. 