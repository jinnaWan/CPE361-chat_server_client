package chat;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

public class ChatServer {
    private ServerSocket serverSocket;
    private final int port;
    private boolean running = false;
    
    // Client management
    private static final AtomicInteger clientIdGenerator = new AtomicInteger(0);
    private final Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    
    public ChatServer(int port) {
        this.port = port;
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Chat Server started on port " + port);
            System.out.println("Waiting for clients to connect...");
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    int clientId = clientIdGenerator.incrementAndGet();
                    
                    ClientHandler handler = new ClientHandler(clientId, clientSocket, this);
                    clients.put(clientId, handler);
                    
                    new Thread(handler).start();
                    System.out.println("Client " + clientId + " connected from " + 
                                     clientSocket.getInetAddress());
                    
                    // Notify all clients about new user
                    broadcastMessage("*** User " + clientId + " joined the chat ***", -1);
                    
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
    
    // Broadcast message to all clients except sender
    public synchronized void broadcastMessage(String message, int senderId) {
        System.out.println("Broadcasting: " + message);
        
        for (Map.Entry<Integer, ClientHandler> entry : clients.entrySet()) {
            int clientId = entry.getKey();
            if (clientId != senderId) { // Don't send back to sender
                ClientHandler handler = entry.getValue();
                handler.sendMessage(message);
            }
        }
    }
    
    // Send welcome message to specific client
    public synchronized void sendWelcomeMessage(int clientId) {
        ClientHandler handler = clients.get(clientId);
        if (handler != null) {
            handler.sendMessage("Welcome to the chat! You are User " + clientId);
            handler.sendMessage("Type your messages and press Enter to send.");
            
            // Tell them how many users are online
            handler.sendMessage("*** " + clients.size() + " users currently online ***");
        }
    }
    
    public synchronized void removeClient(int clientId) {
        clients.remove(clientId);
        System.out.println("Client " + clientId + " disconnected");
        
        // Notify all remaining clients
        broadcastMessage("*** User " + clientId + " left the chat ***", -1);
    }
    
    public static void main(String[] args) {
        ChatServer server = new ChatServer(8080);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down chat server...");
            server.stop();
        }));
        
        server.start();
    }
}

// Client handler for each connected chat user
class ClientHandler implements Runnable {
    private final int clientId;
    private final Socket socket;
    private final ChatServer server;
    private BufferedReader reader;
    private PrintWriter writer;
    
    public ClientHandler(int clientId, Socket socket, ChatServer server) {
        this.clientId = clientId;
        this.socket = socket;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            
            // Send welcome message
            server.sendWelcomeMessage(clientId);
            
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.trim().isEmpty()) continue;
                
                // Format message with user ID and broadcast to others
                String formattedMessage = "User " + clientId + ": " + message;
                server.broadcastMessage(formattedMessage, clientId);
            }
            
        } catch (IOException e) {
            System.err.println("Client " + clientId + " error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }
    
    private void cleanup() {
        server.removeClient(clientId);
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error cleaning up client " + clientId + ": " + e.getMessage());
        }
    }
} 