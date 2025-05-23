package chat;

import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class ServerThread extends Thread {
    private ServerSocket servsock;
    private boolean running = true;
    
    // Maintain list of client output streams for broadcasting (as required in lecture)
    private static List<DataOutputStream> clientOutputs = new CopyOnWriteArrayList<>();
    
    public ServerThread(int port) throws IOException {
        servsock = new ServerSocket(port);
        System.out.println("Chat Server started on port " + port);
    }
    
    @Override
    public void run() {
        while (running) {
            try {
                System.out.println("Waiting for client...");
                Socket clientSocket = servsock.accept();
                System.out.println("Connected to " + clientSocket.getRemoteSocketAddress());
                
                // Start a new ClientHandler thread for each client (as in lecture)
                new ClientHandler(clientSocket).start();
                
            } catch (IOException e) {
                if (running) {
                    System.out.println("Error: " + e.getMessage());
                }
                break;
            }
        }
        
        try {
            servsock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Broadcast message to all connected clients (as required in lecture)
    public static synchronized void broadcastMessage(String message, DataOutputStream excludeClient) {
        System.out.println("Broadcasting: " + message);
        
        List<DataOutputStream> toRemove = new java.util.ArrayList<>();
        
        for (DataOutputStream clientOut : clientOutputs) {
            if (clientOut != excludeClient) { // Don't send back to sender
                try {
                    clientOut.writeUTF(message);
                    clientOut.flush();
                } catch (IOException e) {
                    System.out.println("Client disconnected, removing from broadcast list");
                    toRemove.add(clientOut);
                }
            }
        }
        
        // Remove disconnected clients
        clientOutputs.removeAll(toRemove);
    }
    
    // Add client to broadcast list
    public static synchronized void addClient(DataOutputStream clientOut) {
        clientOutputs.add(clientOut);
        System.out.println("Client added to broadcast list. Total clients: " + clientOutputs.size());
    }
    
    // Remove client from broadcast list  
    public static synchronized void removeClient(DataOutputStream clientOut) {
        clientOutputs.remove(clientOut);
        System.out.println("Client removed from broadcast list. Total clients: " + clientOutputs.size());
    }
    
    public void stopServer() {
        running = false;
        try {
            if (servsock != null && !servsock.isClosed()) {
                servsock.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        int port = 12345;
        try {
            ServerThread server = new ServerThread(port);
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down chat server...");
                server.stopServer();
            }));
            
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 