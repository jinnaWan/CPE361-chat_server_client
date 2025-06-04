package fishtank;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import fishtank.livingthings.Crab;
import fishtank.livingthings.Fish;
import fishtank.livingthings.Jellyfish;
import fishtank.livingthings.LivingThing;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FishTankClient extends Application {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12346;
    
    private Canvas canvas;
    private GraphicsContext gc;
    private List<LivingThing> livingThings = new CopyOnWriteArrayList<>();
    private Map<String, Image> imageCache = new HashMap<>();
    
    // Network components
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int windowId = -1;
    private boolean connected = false;
    
    @Override
    public void start(Stage primaryStage) {
        // Setup JavaFX UI
        canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        StackPane root = new StackPane();
        root.getChildren().add(canvas);
        
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Fish Tank - Window " + windowId);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Connect to server
        connectToServer();
        
        // Create initial living things
        createInitialLivingThings();
        
        // Start animation loop
        startAnimationLoop();
        
        // Handle window close
        primaryStage.setOnCloseRequest(e -> {
            disconnect();
            Platform.exit();
        });
    }
    
    private void connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            // Start network listener thread
            Thread networkThread = new Thread(this::networkListener);
            networkThread.setDaemon(true);
            networkThread.start();
            
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }
    
    private void networkListener() {
        try {
            Object message;
            while (connected && (message = in.readObject()) != null) {
                if (message instanceof String) {
                    String command = (String) message;
                    if (command.startsWith("WINDOW_ID:")) {
                        windowId = Integer.parseInt(command.split(":")[1]);
                        Platform.runLater(() -> {
                            Stage stage = (Stage) canvas.getScene().getWindow();
                            stage.setTitle("Fish Tank - Window " + windowId);
                            stage.setX(windowId * (WINDOW_WIDTH + 50)); // Position windows side by side
                        });
                    } else if ("RECEIVE_OBJECT".equals(command)) {
                        // Receive transferred object
                        Object obj = in.readObject();
                        if (obj instanceof LivingThing) {
                            LivingThing receivedObject = (LivingThing) obj;
                            Platform.runLater(() -> {
                                livingThings.add(receivedObject);
                                System.out.println("Received object: " + receivedObject);
                            });
                        }
                    } else if ("BOUNCE_OBJECT".equals(command)) {
                        // Receive bounced object (failed transfer)
                        Object obj = in.readObject();
                        if (obj instanceof LivingThing) {
                            LivingThing bouncedObject = (LivingThing) obj;
                            Platform.runLater(() -> {
                                livingThings.add(bouncedObject);
                                System.out.println("Bounced object: " + bouncedObject);
                            });
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                System.out.println("Network listener error: " + e.getMessage());
            }
        }
    }
    
    private void createInitialLivingThings() {
        // Create some initial living things for this window
        Random random = new Random();
        
        // Add a fish
        Fish fish = new Fish("fish_" + windowId + "_1", 
                           random.nextDouble() * (WINDOW_WIDTH - 60), 
                           random.nextDouble() * (WINDOW_HEIGHT - 40),
                           (random.nextBoolean() ? 1 : -1) * (1 + random.nextDouble() * 2),
                           (random.nextBoolean() ? 1 : -1) * (0.5 + random.nextDouble()));
        livingThings.add(fish);
        
        // Add a crab
        Crab crab = new Crab("crab_" + windowId + "_1",
                           random.nextDouble() * (WINDOW_WIDTH - 50),
                           random.nextDouble() * (WINDOW_HEIGHT - 35),
                           (random.nextBoolean() ? 1 : -1) * (0.5 + random.nextDouble() * 1.5),
                           0); // Crabs have no vertical movement
        livingThings.add(crab);
        
        // Add a jellyfish
        Jellyfish jellyfish = new Jellyfish("jellyfish_" + windowId + "_1",
                                          random.nextDouble() * (WINDOW_WIDTH - 45),
                                          random.nextDouble() * (WINDOW_HEIGHT - 60),
                                          (random.nextBoolean() ? 1 : -1) * (0.3 + random.nextDouble()),
                                          (random.nextBoolean() ? 1 : -1) * (0.2 + random.nextDouble() * 0.5));
        livingThings.add(jellyfish);
    }
    
    private void startAnimationLoop() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        timer.start();
    }
    
    private void update() {
        List<LivingThing> toTransfer = new ArrayList<>();
        
        for (LivingThing thing : livingThings) {
            thing.move();
            
            // Check boundaries and handle transfers
            if (thing.isAtLeftEdge() && thing.getDx() < 0) {
                // Transfer to left window (server will handle bouncing if no window exists)
                toTransfer.add(thing);
                transferObject(thing, "left");
            } else if (thing.isAtRightEdge(WINDOW_WIDTH) && thing.getDx() > 0) {
                // Transfer to right window (server will handle bouncing if no window exists)
                toTransfer.add(thing);
                transferObject(thing, "right");
            } else {
                // Bounce off top/bottom walls
                if (thing.isAtTopEdge() || thing.isAtBottomEdge(WINDOW_HEIGHT)) {
                    thing.bounceVertical();
                }
                
                // Keep within bounds
                if (thing.getY() < 0) thing.setY(0);
                if (thing.getY() + thing.getHeight() > WINDOW_HEIGHT) {
                    thing.setY(WINDOW_HEIGHT - thing.getHeight());
                }
            }
        }
        
        // Remove transferred objects
        livingThings.removeAll(toTransfer);
    }
    
    private void transferObject(LivingThing object, String direction) {
        try {
            if (connected && out != null) {
                out.writeObject("TRANSFER:" + direction);
                out.writeObject(object);
                out.flush();
                System.out.println("Transferred " + object + " to " + direction);
            }
        } catch (IOException e) {
            System.out.println("Error transferring object: " + e.getMessage());
        }
    }
    
    private Image getImage(String imagePath) {
        // Check if image is already cached
        if (!imageCache.containsKey(imagePath)) {
            try {
                // Load and cache the image
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                imageCache.put(imagePath, image);
            } catch (Exception e) {
                System.out.println("Error loading image: " + imagePath + " - " + e.getMessage());
                imageCache.put(imagePath, null); // Cache null to avoid repeated attempts
            }
        }
        return imageCache.get(imagePath);
    }
    
    private void render() {
        // Clear canvas with blue background (water)
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Draw living things
        for (LivingThing thing : livingThings) {
            Image image = getImage(thing.getImagePath());
            if (image != null) {
                gc.drawImage(image, thing.getX(), thing.getY(), thing.getWidth(), thing.getHeight());
            } else {
                // Fallback: draw colored rectangle if image fails to load
                gc.setFill(Color.ORANGE);
                gc.fillRect(thing.getX(), thing.getY(), thing.getWidth(), thing.getHeight());
            }
        }
        
        // Draw window info
        gc.setFill(Color.BLACK);
        gc.fillText("Window " + windowId + " - Objects: " + livingThings.size(), 10, 20);
    }
    
    private void disconnect() {
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error during disconnect: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 