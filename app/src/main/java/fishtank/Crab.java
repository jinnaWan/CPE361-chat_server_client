package fishtank;

public class Crab extends LivingThing {
    private static final long serialVersionUID = 1L;
    
    public Crab(String id, double x, double y, double dx, double dy) {
        super(id, x, y, dx, dy, "/images/crab.png", 50, 35);
    }
    
    @Override
    public void move() {
        // Crabs move sideways more than vertically
        x += dx;
        y += dy * 0.3; // Slower vertical movement
    }
} 