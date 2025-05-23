# PA10: Chat Client-Server Application

This is a simple chat application implementing the PA10 assignment requirements:

> **PA10**: Create a chat client and server using Java threading and networking. Multiple clients should be able to connect to a single server and exchange messages in real-time.

## What This Implementation Does

✅ **Multi-client Chat Server**: TCP server that handles multiple chat clients simultaneously  
✅ **JavaFX Chat Client**: Modern graphical user interface for chatting  
✅ **Real-time Messaging**: Messages are instantly broadcast to all connected users  
✅ **Thread-based**: Server uses separate threads for each client connection  
✅ **User Management**: Tracks users joining/leaving the chat  

## Architecture

### ChatServer.java
- **TCP Server** listening on port 8080
- **ClientHandler Threads**: One thread per connected client
- **Message Broadcasting**: Sends messages to all connected clients
- **User Notifications**: Announces when users join/leave
- **Concurrent Safe**: Uses thread-safe data structures

### ChatClient.java
- **JavaFX Application** with modern GUI
- **Connection Management**: Connect/disconnect to server
- **Real-time Updates**: Listens for incoming messages on separate thread
- **User-friendly Interface**: Text area for chat history, input field for typing
- **Auto-scroll**: Automatically scrolls to show latest messages

## Key Features

### Server Features
- Handles unlimited simultaneous clients
- Broadcasts messages to all users except sender
- Assigns unique user IDs to each client
- Notifies all users when someone joins/leaves
- Graceful shutdown handling

### Client Features
- Easy connection setup (server/port input)
- Real-time chat with other users
- Clean, modern JavaFX interface
- Automatic message formatting
- Connection status indicator
- Enter key to send messages

## Running the Chat Application

### Method 1: Automatic Launch (Windows)
```bash
.\start_chat.bat
```
This will:
1. Build the application 
2. Start the chat server
3. Launch two chat client windows

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

### Method 3: With JavaFX Module Path
If you encounter JavaFX errors:
```bash
java --module-path "path/to/javafx/lib" --add-modules javafx.controls,javafx.fxml -jar app/build/libs/ChatApp.jar chat-client
```

## How to Use

### Starting a Chat Session
1. Run the server first
2. Launch one or more chat clients
3. In each client, make sure server is "localhost" and port is "8080"
4. Click "Connect" to join the chat
5. Start typing messages!

### Chat Commands
- Type any message and press Enter to send
- Messages appear as "User X: your message"
- System messages show user join/leave notifications
- Click "Disconnect" to leave the chat

## What You'll See

### Server Console
```
Chat Server started on port 8080
Waiting for clients to connect...
Client 1 connected from /127.0.0.1
Broadcasting: *** User 1 joined the chat ***
Client 2 connected from /127.0.0.1  
Broadcasting: *** User 2 joined the chat ***
Broadcasting: User 1: Hello everyone!
Broadcasting: User 2: Hi there!
```

### Client Windows
- **Connection panel** at top with server/port fields
- **Chat area** showing all messages and notifications
- **Message input** at bottom for typing
- **Status indicator** showing connection state

## Technical Implementation

### Threading
- **Server Main Thread**: Accepts new client connections
- **ClientHandler Threads**: One per client for message processing
- **Client UI Thread**: JavaFX Application Thread for GUI updates
- **Client Listener Thread**: Receives messages from server

### Networking
- **TCP Sockets**: Reliable client-server communication
- **BufferedReader/PrintWriter**: Text-based message protocol
- **Concurrent Collections**: Thread-safe client management
- **Graceful Shutdown**: Proper resource cleanup

### JavaFX GUI
- **BorderPane Layout**: Organized UI structure
- **Event Handling**: Button clicks and keyboard input
- **Platform.runLater()**: Thread-safe GUI updates
- **Real-time Updates**: Messages appear instantly

## Assignment Requirements ✅

✅ **Chat client and server** - Complete TCP-based chat system  
✅ **Multiple clients** - Server handles unlimited simultaneous users  
✅ **Threading** - Server uses threads for each client connection  
✅ **Real-time messaging** - Instant message broadcasting  
✅ **User interface** - Modern JavaFX client application  

## PA10 Complete! 🎉

This implementation demonstrates all the key concepts from the lecture:
- **Thread Programming**: Multi-threaded server handling concurrent clients
- **Network Programming**: TCP client-server communication with sockets
- **JavaFX GUI**: Modern graphical user interface
- **Real-time Systems**: Instant message delivery and user notifications

The chat system provides a practical example of how threads and networking work together to create interactive applications that multiple users can use simultaneously. 