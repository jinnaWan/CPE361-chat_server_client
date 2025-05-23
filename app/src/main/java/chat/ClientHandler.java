package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }
    
    @Override
    public void run() {
        try {
            // Setup input and output streams (as in lecture examples)
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            
            // Add this client to the broadcast list
            ServerThread.addClient(out);
            
            // Send welcome message
            out.writeUTF("Welcome to the chat! Type your messages and press Enter.");
            out.writeUTF("*** You are now connected to the chat server ***");
            out.flush();
            
            // Notify other clients about new user
            ServerThread.broadcastMessage("*** A new user joined the chat ***", out);
            
            // Read messages from client and broadcast them
            String message;
            while ((message = in.readUTF()) != null) {
                if (message.trim().isEmpty()) continue;
                
                System.out.println("Received from " + clientSocket.getRemoteSocketAddress() + ": " + message);
                
                // Format and broadcast message to all other clients
                String broadcastMessage = "Client: " + message;
                ServerThread.broadcastMessage(broadcastMessage, out);
            }
            
        } catch (IOException e) {
            System.out.println("ClientHandler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void cleanup() {
        try {
            // Remove client from broadcast list
            if (out != null) {
                ServerThread.removeClient(out);
                
                // Notify other clients about user leaving
                ServerThread.broadcastMessage("*** A user left the chat ***", out);
            }
            
            // Close streams and socket
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            
        } catch (IOException e) {
            System.out.println("Error during cleanup: " + e.getMessage());
        }
    }
} 