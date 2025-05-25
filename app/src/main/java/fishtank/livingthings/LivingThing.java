package fishtank.livingthings;

import java.io.Serializable;

public abstract class LivingThing implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected String id;
    protected double x, y;
    protected double dx, dy;
    protected String imagePath;
    protected double width, height;
    
    public LivingThing(String id, double x, double y, double dx, double dy, String imagePath, double width, double height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.imagePath = imagePath;
        this.width = width;
        this.height = height;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getDx() { return dx; }
    public void setDx(double dx) { this.dx = dx; }
    
    public double getDy() { return dy; }
    public void setDy(double dy) { this.dy = dy; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    // Abstract method for movement behavior
    public abstract void move();
    
    // Check if the living thing is at the edge of the window
    public boolean isAtLeftEdge() {
        return x <= 0;
    }
    
    public boolean isAtRightEdge(double windowWidth) {
        return x + width >= windowWidth;
    }
    
    public boolean isAtTopEdge() {
        return y <= 0;
    }
    
    public boolean isAtBottomEdge(double windowHeight) {
        return y + height >= windowHeight;
    }
    
    // Bounce off vertical walls
    public void bounceVertical() {
        dy = -dy;
    }
    
    // Bounce off horizontal walls  
    public void bounceHorizontal() {
        dx = -dx;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "', x=" + x + ", y=" + y + "}";
    }
} 