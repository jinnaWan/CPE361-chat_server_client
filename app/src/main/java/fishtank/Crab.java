package fishtank;

public class Crab extends LivingThing {
    private static final long serialVersionUID = 1L;
    
    public Crab(String id, double x, double y, double dx, double dy) {
        super(id, x, y, dx, 0, "/images/crab.png", 50, 35); // Set dy to 0 for horizontal-only movement
    }
    
    @Override
    public void move() {
        // Crabs move only horizontally
        x += dx;
        // No vertical movement for crabs
    }
} 