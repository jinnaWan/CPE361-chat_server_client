package fishtank;

public class Jellyfish extends LivingThing {
    private static final long serialVersionUID = 1L;
    private double time = 0;
    
    public Jellyfish(String id, double x, double y, double dx, double dy) {
        super(id, x, y, dx, dy, "/images/jellyfish.png", 45, 60);
    }
    
    @Override
    public void move() {
        // Jellyfish have a floating, wavy movement
        time += 0.1;
        x += dx;
        y += dy + Math.sin(time) * 0.5; // Add wavy vertical movement
    }
} 