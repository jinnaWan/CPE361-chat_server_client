package fishtank;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import fishtank.livingthings.LivingThing;

public class ClientWindowHandler extends Thread {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int windowId;
    private FishTankServer server;
    private boolean connected = false;
    
    public ClientWindowHandler(Socket socket, int windowId, FishTankServer server) {
        this.clientSocket = socket;
        this.windowId = windowId;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            // Setup object streams for serialization
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            
            connected = true;
            
            // Send window ID to client
            out.writeObject("WINDOW_ID:" + windowId);
            out.flush();
            
            // Listen for messages from client
            Object message;
            while (connected && (message = in.readObject()) != null) {
                if (message instanceof String) {
                    String command = (String) message;
                    if (command.startsWith("TRANSFER:")) {
                        // Handle transfer request
                        handleTransferRequest(command);
                    }
                } else if (message instanceof LivingThing) {
                    // Handle object transfer from client
                    LivingThing object = (LivingThing) message;
                    System.out.println("Received object from window " + windowId + ": " + object);
                }
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("ClientWindowHandler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void handleTransferRequest(String command) {
        try {
            // Parse command: "TRANSFER:direction:object"
            String[] parts = command.split(":", 3);
            if (parts.length >= 2) {
                String direction = parts[1];
                
                // Read the object to transfer
                Object obj = in.readObject();
                if (obj instanceof LivingThing) {
                    LivingThing object = (LivingThing) obj;
                    server.transferObject(object, windowId, direction);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error handling transfer request: " + e.getMessage());
        }
    }
    
    // Send object to this client window
    public void receiveObject(LivingThing object) {
        try {
            if (connected && out != null) {
                out.writeObject("RECEIVE_OBJECT");
                out.writeObject(object);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Error sending object to window " + windowId + ": " + e.getMessage());
        }
    }
    
    // Send object back to client when transfer fails (for bouncing)
    public void bounceObject(LivingThing object) {
        try {
            if (connected && out != null) {
                out.writeObject("BOUNCE_OBJECT");
                out.writeObject(object);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Error sending bounce object to window " + windowId + ": " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public int getWindowId() {
        return windowId;
    }
    
    private void cleanup() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            
            // Remove this client from server
            server.removeClient(this);
            
        } catch (IOException e) {
            System.out.println("Error during cleanup: " + e.getMessage());
        }
    }
} 