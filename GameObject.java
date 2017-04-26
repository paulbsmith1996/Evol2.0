import java.awt.Graphics;

public class GameObject {

    // x and y coordinates of the GameObject
    private int x, y;

    private final int DEFAULT_DIAMETER = 10;

    // Initialize x and y coordinates of the game object
    public GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    // Getter and Setter for x
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    // Getter and Setter for y
    public int getY() {	return y; }
    public void setY(int y) { this.y = y; }

    // Method to be overridden depending on the gameobject
    public void draw(Graphics g) {
        g.drawOval(x, y, DEFAULT_DIAMETER, DEFAULT_DIAMETER);
    }

}
