# Assignment 3: Multi-Screen Fish Tank - README

## Window Layout Logic

The application uses a **horizontal linear layout** where windows are logically positioned from left to right:

```
Window 0 ←→ Window 1 ←→ Window 2 ←→ Window 3 ...
```

- **Server Management**: Each connecting client receives a sequential window ID (0, 1, 2, ...)
- **Physical Positioning**: Windows automatically position themselves side-by-side on screen using `windowId * (WINDOW_WIDTH + 50)`
- **Adjacency Logic**: Window N has left neighbor (N-1) and right neighbor (N+1)
- **Boundary Mapping**: Left edge of Window 0 has no left neighbor, right edge of highest ID has no right neighbor

## Object Transfer Logic

**Step 1: Boundary Detection**
- Client detects when objects hit left/right window edges
- Objects moving left at x ≤ 0 trigger transfer to left neighbor
- Objects moving right at x ≥ windowWidth trigger transfer to right neighbor

**Step 2: Serialization & Network Transfer**
- Client serializes the `LivingThing` object using Java Object Serialization
- Sends command `"TRANSFER:direction"` followed by serialized object to server
- Object removed from source window's local collection

**Step 3: Server Routing**
- Server calculates target window: `targetId = sourceId ± 1`
- If target window exists: object positioned at opposite edge and forwarded
- If no target window: object bounced back with reversed horizontal velocity

**Step 4: Object Reception**
- Target client receives `"RECEIVE_OBJECT"` command + serialized object
- Object deserialized and added to target window's local collection
- Continues movement from new position

**Boundary Bouncing Behavior**
- Objects hitting the left edge of Window 0 (leftmost) bounce back with `dx = -dx`
- Objects hitting the right edge of the rightmost window bounce back with `dx = -dx`
- This prevents objects from disappearing at the "end of the world"

## Libraries Used

- **JavaFX**: Graphics rendering, animation (AnimationTimer), UI controls (Canvas, Stage, Scene)
- **Java Sockets**: TCP network communication between server and clients
- **Object Serialization**: Binary transmission of `LivingThing` objects over network streams
- **Multithreading**: Concurrent client handling (server) and separate network listener threads (client)
- **Gradle**: Build automation and dependency management 