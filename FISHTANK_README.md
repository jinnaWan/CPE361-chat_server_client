# Multi-Screen Fish Tank Application

## Overview

This is a Java-based graphical application that simulates a fish tank across multiple windows, where living things (fish, crabs, jellyfish) move across screens and are transferred between clients via a central server.

## Features

- **Multiple Client Windows**: Each client acts as a local fish tank with independently moving creatures
- **Cross-Window Movement**: When creatures hit left/right borders, they are transferred to adjacent windows
- **Three Types of Living Things**:
  - **Fish**: Simple linear movement
  - **Crab**: Sideways movement with slower vertical motion
  - **Jellyfish**: Floating movement with wavy patterns
- **JavaFX Graphics**: Beautiful visual representation with actual creature images
- **Network Communication**: TCP sockets with object serialization for creature transfer

## Architecture

### Window Layout Logic
- Windows are arranged horizontally from left to right
- Window 0 is the leftmost, Window 1 is to its right, etc.
- Each new client connects and gets assigned the next available window ID
- Windows are automatically positioned side by side on screen

### Object Transfer Logic
1. When a creature hits the left edge (moving left), it's sent to the window on the left
2. When a creature hits the right edge (moving right), it's sent to the window on the right
3. The server calculates the target window and forwards the creature
4. The creature appears at the opposite edge of the target window
5. Creatures bounce off top/bottom walls within each window

### Technical Implementation
- **Server**: `FishTankServer` manages window coordination and object transfers
- **Client**: `FishTankClient` provides JavaFX-based graphical fish tank
- **Living Things**: Serializable classes (`Fish`, `Crab`, `Jellyfish`) that can be sent over network
- **Communication**: Object streams for sending serialized creatures between windows

## How to Run

### Option 1: Using the Batch File (Recommended)
```bash
start_fishtank.bat
```
This will:
1. Build the application
2. Start the server
3. Launch two client windows automatically
4. Allow you to start a third client

### Option 2: Manual Startup

1. **Build the application:**
   ```bash
   gradlew shadowJar
   ```

2. **Start the server:**
   ```bash
   java -jar app/build/libs/ChatApp.jar fishtank-server
   ```

3. **Start client windows (in separate terminals):**
   ```bash
   java -jar app/build/libs/ChatApp.jar fishtank-client
   java -jar app/build/libs/ChatApp.jar fishtank-client
   java -jar app/build/libs/ChatApp.jar fishtank-client
   ```

## What to Expect

1. **Server Console**: Shows connection messages and transfer logs
2. **Client Windows**: 
   - Blue background representing water
   - Moving creatures (fish, crab, jellyfish) with actual images
   - Window ID and creature count displayed
   - Automatic positioning side by side

3. **Cross-Window Transfer**: 
   - Watch creatures move between windows seamlessly
   - Console logs show transfer activity
   - Creatures maintain their movement direction after transfer

## Libraries Used

- **JavaFX**: For graphics, animation, and UI
- **Java Sockets**: For network communication
- **Object Serialization**: For sending creatures over network
- **Gradle**: For build management

## File Structure

```
app/src/main/java/
├── fishtank/
│   ├── LivingThing.java          # Abstract base class for all creatures
│   ├── Fish.java                 # Fish implementation
│   ├── Crab.java                 # Crab implementation  
│   ├── Jellyfish.java            # Jellyfish implementation
│   ├── FishTankServer.java       # Server managing window coordination
│   ├── ClientWindowHandler.java  # Server-side client handler
│   └── FishTankClient.java       # JavaFX client application
├── chat/                         # Original chat application
└── App.java                      # Main launcher with both chat and fishtank options

app/src/main/resources/images/
├── fish.png                      # Fish sprite
├── crab.png                      # Crab sprite
└── jellyfish.png                 # Jellyfish sprite
```

## Key Features Implemented

✅ **Multiple Windows**: Support for 2+ client windows  
✅ **Graphical Objects**: Visual creatures with images  
✅ **Server Coordination**: Central server tracks window positions  
✅ **Cross-Window Movement**: Seamless creature transfer between windows  
✅ **Serializable Objects**: Creatures can be sent over network  
✅ **TCP Communication**: Reliable network protocol  
✅ **JavaFX Graphics**: Modern UI with animations  
✅ **Object Movement**: Realistic movement patterns for different creatures  
✅ **Boundary Detection**: Proper edge detection and transfer logic  

## Bonus Features

✅ **Multiple Object Types**: Fish, Crab, and Jellyfish with different behaviors  
✅ **Dynamic Window Support**: Can add 3+ windows at runtime  
✅ **Automatic Window Positioning**: Windows arrange themselves side by side  

## Testing

1. Start the server and 2-3 client windows
2. Watch creatures move around in each window
3. Observe creatures transferring between windows when they hit edges
4. Check console logs for transfer confirmation
5. Try closing and reopening client windows

## Troubleshooting

- **Images not loading**: Ensure image files are in `app/src/main/resources/images/`
- **Connection issues**: Make sure server is running before starting clients
- **JavaFX issues**: Ensure JavaFX is properly configured in build.gradle
- **Port conflicts**: Server uses port 12346 (different from chat server's 12345)

## Assignment Requirements Met

This implementation fulfills all core requirements of Assignment 3:
- ✅ At least two separate client windows
- ✅ Graphical objects with visual representation
- ✅ Server tracking logical window positions
- ✅ Cross-window movement with proper handoff
- ✅ TCP socket communication
- ✅ Object serialization for network transfer
- ✅ Proper boundary detection and transfer logic 