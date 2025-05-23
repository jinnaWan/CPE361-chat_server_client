package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientThread extends Thread {
    private final String url;
    private final int port;
    private Socket client;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean connected = false;
    
    public ClientThread(String ip, int port) {
        this.url = ip;
        this.port = port;
    }
    
    @Override
    public void run() {
        try {
            System.out.println("Connecting to " + url + " on port " + port);
            client = new Socket(url, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            
            // Setup input and output streams (as in lecture examples)
            InputStream inFromServer = client.getInputStream();
            in = new DataInputStream(inFromServer);
            
            OutputStream outToServer = client.getOutputStream();
            out = new DataOutputStream(outToServer);
            
            connected = true;
            
            // Start receiver thread (as required in lecture - two threads)
            Thread receiverThread = new Thread(new MessageReceiver());
            receiverThread.start();
            
            // Start sender thread (keyboard input thread as required in lecture)
            Thread senderThread = new Thread(new MessageSender());
            senderThread.start();
            
            // Wait for threads to complete
            receiverThread.join();
            senderThread.join();
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }
    
    // Receiver thread - receives messages from server (as required in lecture)
    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while (connected && (message = in.readUTF()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                if (connected) {
                    System.out.println("Connection lost: " + e.getMessage());
                }
            }
        }
    }
    
    // Sender thread - sends messages from keyboard input (as required in lecture)
    private class MessageSender implements Runnable {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            try {
                System.out.println("You can start typing messages (type 'quit' to exit):");
                
                while (connected) {
                    String message = scanner.nextLine();
                    
                    if (message.equalsIgnoreCase("quit")) {
                        break;
                    }
                    
                    if (!message.trim().isEmpty()) {
                        out.writeUTF(message);
                        out.flush();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error sending message: " + e.getMessage());
            } finally {
                scanner.close();
            }
        }
    }
    
    private void disconnect() {
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (client != null) client.close();
        } catch (IOException e) {
            System.out.println("Error during disconnect: " + e.getMessage());
        }
    }
    
    // Main method for quick test (as in lecture examples)
    public static void main(String[] args) {
        String serverIp = "localhost";
        int serverPort = 12345;
        
        System.out.println("=== PA10 Chat Client ===");
        System.out.println("Connecting to chat server...");
        
        Thread client = new ClientThread(serverIp, serverPort);
        client.start();
        
        try {
            client.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("Chat client terminated.");
    }
} 