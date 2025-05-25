# Assignment 3: Multi-Screen Fish Tank

## 📌 Objective

A Java-based graphical application that simulates a fish tank across multiple windows, where graphical objects (fish, crabs, jellyfish) move across screens and are transferred between clients via a central server using TCP sockets and object serialization.

## 🎯 Core Requirements Implementation

### ✅ Multiple Client Windows
- **Requirement**: At least two separate client windows, each acting as a local fish tank
- **Implementation**: Supports unlimited client windows with automatic window ID assignment
- Each client runs independently with its own JavaFX animation loop
- Windows are automatically positioned side by side for seamless visual experience

### ✅ Graphical Objects  
- **Requirement**: Visual objects (fish, turtle, etc.) displayed in each client
- **Implementation**: Three types of living creatures with actual PNG images:
  - **Fish**: 60x40px sprite with omnidirectional movement
  - **Crab**: 50x35px sprite with horizontal-only movement  
  - **Jellyfish**: 45x60px sprite with floating wave patterns
- Objects rendered using JavaFX Canvas and GraphicsContext
- Real-time animation at 60 FPS using JavaFX AnimationTimer

### ✅ Server Coordination
- **Requirement**: Server tracks logical window positions and forwards objects
- **Implementation**: `FishTankServer` maintains:
  - `Map<Integer, ClientWindowHandler>` for active window connections
  - Window ID assignment (0, 1, 2, ...) representing left-to-right layout
  - Object transfer routing logic between adjacent windows

### ✅ Cross-Window Movement
- **Requirement**: Objects hitting borders are sent to server for transfer to adjacent windows
- **Implementation**: Seamless object handoff with boundary detection and server-mediated transfer

## 🧠 Technical Implementation Details

### Window Layout Logic

The application uses a **linear horizontal layout** where windows are logically arranged from left to right:

```
Window 0 ←→ Window 1 ←→ Window 2 ←→ Window 3 ...
```

**Server-Side Window Management:**
```java
// FishTankServer.java
private Map<Integer, ClientWindowHandler> clients = new ConcurrentHashMap<>();
private int nextWindowId = 0;

// Window ID assignment on client connection
ClientWindowHandler handler = new ClientWindowHandler(clientSocket, nextWindowId++, this);
clients.put(handler.getWindowId(), handler);
```

**Client-Side Window Positioning:**
```java
// FishTankClient.java - Automatic window positioning
Platform.runLater(() -> {
    Stage stage = (Stage) canvas.getScene().getWindow();
    stage.setTitle("Fish Tank - Window " + windowId);
    stage.setX(windowId * (WINDOW_WIDTH + 50)); // Side-by-side positioning
});
```

### Object Transfer Logic

**Step-by-Step Transfer Process:**

1. **Boundary Detection** (Client-Side):
```java
// FishTankClient.java - Boundary checking in animation loop
if (thing.isAtLeftEdge() && thing.getDx() < 0) {
    transferObject(thing, "left");
} else if (thing.isAtRightEdge(WINDOW_WIDTH) && thing.getDx() > 0) {
    transferObject(thing, "right");
}
```

2. **Object Serialization & Network Transfer**:
```java
// Client sends serialized object to server
private void transferObject(LivingThing object, String direction) {
    out.writeObject("TRANSFER:" + direction);  // Command
    out.writeObject(object);                   // Serialized LivingThing
    out.flush();
}
```

3. **Server-Side Routing Logic**:
```java
// FishTankServer.java - Calculate target window
public void transferObject(LivingThing object, int sourceWindowId, String direction) {
    int targetWindowId = "left".equals(direction) ? 
        sourceWindowId - 1 : sourceWindowId + 1;
    
    ClientWindowHandler targetClient = clients.get(targetWindowId);
    if (targetClient != null && targetClient.isConnected()) {
        // Position object at opposite edge of target window
        if ("left".equals(direction)) {
            object.setX(WINDOW_WIDTH - object.getWidth()); // Right edge
        } else {
            object.setX(0); // Left edge
        }
        targetClient.receiveObject(object);
    } else {
        // Bounce back if no adjacent window
        ClientWindowHandler sourceClient = clients.get(sourceWindowId);
        object.bounceHorizontal();
        sourceClient.bounceObject(object);
    }
}
```

4. **Object Reception** (Target Client):
```java
// ClientWindowHandler.java - Send to target client
public void receiveObject(LivingThing object) {
    out.writeObject("RECEIVE_OBJECT");
    out.writeObject(object);
    out.flush();
}
```

### Serialization Implementation

**LivingThing Base Class:**
```java
// fishtank/livingthings/LivingThing.java
public abstract class LivingThing implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected String id;           // Unique identifier
    protected double x, y;         // Position coordinates
    protected double dx, dy;       // Velocity vectors
    protected String imagePath;    // Image resource path
    protected double width, height; // Dimensions
    
    // Serializable state includes all movement and visual properties
}
```

**Concrete Implementations:**
```java
// Fish.java - Simple linear movement
public class Fish extends LivingThing {
    private static final long serialVersionUID = 1L;
    
    public Fish(String id, double x, double y, double dx, double dy) {
        super(id, x, y, dx, dy, "/images/fish.png", 60, 40);
    }
}

// Crab.java - Horizontal-only movement
public class Crab extends LivingThing {
    public Crab(String id, double x, double y, double dx, double dy) {
        super(id, x, y, dx, 0, "/images/crab.png", 50, 35); // dy = 0
    }
}
```

### Network Communication Protocol

**TCP Socket Architecture:**
- **Server Port**: 12346 (dedicated fish tank port)
- **Protocol**: Object streams over TCP for reliable delivery
- **Message Types**:
  - `"WINDOW_ID:n"` - Server assigns window ID to client
  - `"TRANSFER:direction"` + `LivingThing` - Client requests object transfer
  - `"RECEIVE_OBJECT"` + `LivingThing` - Server delivers object to target
  - `"BOUNCE_OBJECT"` + `LivingThing` - Server bounces object back

**Thread Architecture:**
```java
// Each client maintains two threads:
// 1. JavaFX Application Thread - UI rendering and animation
// 2. Network Listener Thread - Server communication

private void networkListener() {
    while (connected && (message = in.readObject()) != null) {
        // Handle server messages on background thread
        // Update UI via Platform.runLater() for thread safety
    }
}
```

## ⚙️ Libraries and Technologies Used

- **JavaFX**: Graphics rendering, animation, and UI framework
- **Java Sockets**: TCP network communication
- **Object Serialization**: Binary object transmission over network
- **Multithreading**: Concurrent client handling and UI responsiveness
- **Gradle**: Build automation and dependency management

## 🧪 Bonus Features Implemented

✅ **Multiple Object Types**: Fish, Crab, and Jellyfish with distinct behaviors  
✅ **Dynamic Window Support**: Supports 3+ windows at runtime  
✅ **Automatic Window Positioning**: Self-arranging window layout  
✅ **Bidirectional Transfer**: Objects can move both left and right  
✅ **Bounce Logic**: Objects bounce back when no adjacent window exists  

## 📂 File Structure

```
app/src/main/java/fishtank/
├── livingthings/
│   ├── LivingThing.java          # Abstract serializable base class
│   ├── Fish.java                 # Fish with omnidirectional movement
│   ├── Crab.java                 # Crab with horizontal-only movement
│   └── Jellyfish.java            # Jellyfish with floating patterns
├── FishTankServer.java           # TCP server with window coordination
├── ClientWindowHandler.java     # Server-side client connection handler
└── FishTankClient.java           # JavaFX client with graphics and networking

app/src/main/resources/images/
├── fish.png                      # 60x40 fish sprite
├── crab.png                      # 50x35 crab sprite
└── jellyfish.png                 # 45x60 jellyfish sprite
```

## 🚀 How to Run

### Quick Start (Recommended)
```bash
start_fishtank.bat
```

### Manual Startup
1. **Build**: `gradlew build`
2. **Server**: `java -jar app/build/libs/FishTank.jar fishtank-server`
3. **Clients**: `java -jar app/build/libs/FishTank.jar fishtank-client` (multiple terminals)

## 🧪 Testing & Demonstration

1. **Start server and 2-3 client windows**
2. **Observe cross-window transfers**: Watch creatures seamlessly move between windows
3. **Check console logs**: Server logs all transfer operations
4. **Test boundary conditions**: Verify bouncing when no adjacent window exists
5. **Dynamic window addition**: Add new clients at runtime

## 📸 Expected Behavior

- **Visual**: Blue water background with animated creatures
- **Movement**: Creatures move continuously with realistic physics
- **Transfer**: Seamless handoff between adjacent windows
- **Positioning**: Windows automatically arrange side by side
- **Logging**: Console shows detailed transfer and connection activity

This implementation fully satisfies all Assignment 3 requirements with additional bonus features for enhanced functionality and user experience. 