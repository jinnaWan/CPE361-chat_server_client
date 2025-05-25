package fishtank;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fishtank.livingthings.LivingThing;

public class FishTankServer extends Thread {
    private ServerSocket serverSocket;
    private boolean running = true;
    private List<ClientWindowHandler> clientWindows = new CopyOnWriteArrayList<>();
    
    public FishTankServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Fish Tank Server started on port " + port);
    }
    
    @Override
    public void run() {
        while (running) {
            try {
                System.out.println("Waiting for fish tank client...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Fish tank client connected: " + clientSocket.getRemoteSocketAddress());
                
                // Create new window handler and assign window position
                int windowId = clientWindows.size();
                ClientWindowHandler windowHandler = new ClientWindowHandler(clientSocket, windowId, this);
                clientWindows.add(windowHandler);
                windowHandler.start();
                
                System.out.println("Window " + windowId + " created. Total windows: " + clientWindows.size());
                
            } catch (IOException e) {
                if (running) {
                    System.out.println("Server error: " + e.getMessage());
                }
                break;
            }
        }
        
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Handle object transfer between windows
    public synchronized void transferObject(LivingThing object, int fromWindowId, String direction) {
        System.out.println("Transferring " + object + " from window " + fromWindowId + " to " + direction);
        
        int targetWindowId = -1;
        double newX = object.getX();
        
        // Calculate target window based on direction
        if ("right".equals(direction)) {
            targetWindowId = fromWindowId + 1;
            newX = 10; // Place at left edge of target window
        } else if ("left".equals(direction)) {
            targetWindowId = fromWindowId - 1;
            newX = 750; // Place at right edge of target window (assuming 800px width)
        }
        
        // Check if target window exists and is valid
        if (targetWindowId >= 0 && targetWindowId < clientWindows.size()) {
            ClientWindowHandler targetWindow = clientWindows.get(targetWindowId);
            if (targetWindow != null && targetWindow.isConnected()) {
                object.setX(newX);
                targetWindow.receiveObject(object);
                System.out.println("Object transferred to window " + targetWindowId);
            } else {
                System.out.println("Target window " + targetWindowId + " not available, bouncing back");
                // Send object back to origin window to bounce
                bounceObjectBack(object, fromWindowId);
            }
        } else {
            System.out.println("No window " + direction + " of window " + fromWindowId + " (target would be " + targetWindowId + "), bouncing back");
            // Send object back to origin window to bounce
            bounceObjectBack(object, fromWindowId);
        }
    }
    
    // Send object back to origin window for bouncing
    private synchronized void bounceObjectBack(LivingThing object, int fromWindowId) {
        if (fromWindowId >= 0 && fromWindowId < clientWindows.size()) {
            ClientWindowHandler originWindow = clientWindows.get(fromWindowId);
            if (originWindow != null && originWindow.isConnected()) {
                // Reverse the object's horizontal direction for bouncing
                object.bounceHorizontal();
                originWindow.bounceObject(object);
                System.out.println("Object bounced back to window " + fromWindowId);
            }
        }
    }
    
    // Remove disconnected client
    public synchronized void removeClient(ClientWindowHandler client) {
        clientWindows.remove(client);
        System.out.println("Client removed. Total windows: " + clientWindows.size());
    }
    
    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        int port = 12346; // Different port from chat server
        try {
            FishTankServer server = new FishTankServer(port);
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down fish tank server...");
                server.stopServer();
            }));
            
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 