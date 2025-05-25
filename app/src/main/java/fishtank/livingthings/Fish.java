package fishtank.livingthings;

public class Fish extends LivingThing {
    private static final long serialVersionUID = 1L;
    
    public Fish(String id, double x, double y, double dx, double dy) {
        super(id, x, y, dx, dy, "/images/fish.png", 60, 40);
    }
    
    @Override
    public void move() {
        // Simple linear movement
        x += dx;
        y += dy;
    }
} 