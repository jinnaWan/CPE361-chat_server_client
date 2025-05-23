# WebSocket Server and Client

This Java application demonstrates a basic WebSocket server and client implementation using only standard Java libraries (no external WebSocket libraries).

## Overview

The application consists of:

1. **WebSocketServer** - Implements a server that follows the WebSocket protocol
2. **WebSocketClient** - Implements a client that connects to a WebSocket server
3. **App** - Main class with examples for running both server and client

## How to Run

### Building the Project

```bash
./gradlew build
```

### Running the Server

```bash
./gradlew run --args="server"
```

This will start a WebSocket server on port 8080 that:
- Listens for incoming WebSocket connections
- Handles WebSocket handshakes
- Processes WebSocket frames (text messages, pings, etc.)
- Echoes back any received messages

### Running the Client

```bash
./gradlew run --args="client"
```

This will start a WebSocket client that:
- Connects to the local WebSocket server
- Sends a few test messages
- Receives and displays server responses
- Closes the connection after sending all messages

## Implementation Details

The implementation follows the WebSocket protocol (RFC 6455) and includes:

1. HTTP-based handshake with the Sec-WebSocket-Key/Accept mechanism
2. Message framing with proper headers
3. Text message encoding/decoding
4. Masking of client-to-server messages
5. Handling of control frames (ping/pong, close)

## Multi-threaded Design

- The server creates a new thread for each client connection
- The client uses a separate thread for receiving messages

## Limitations

As this is a basic implementation:
- It only supports text messages (no binary frames)
- There's minimal error handling
- It doesn't implement all optional WebSocket features
- No SSL/TLS support for secure WebSockets (wss://)

For production use, consider using established WebSocket libraries such as:
- Tyrus (Jakarta WebSocket reference implementation)
- Jetty WebSocket Client/Server
- Undertow 