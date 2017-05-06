import java.awt.Graphics;

public class GameObject {

    // Spatial coordinates of the object within the game environment.
    private int x, y;

    // The default diameter of the object (which, by default, has an
    // oval shape).
    private final int DEFAULT_DIAMETER = 10;

    // Initialization.
    public GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Gets the object's x-coordinate.
    public int getX() {
        return x;
    }

    // Sets the object's x-coordinate.
    public void setX(int x) {
        this.x = x;
    }

    // Gets the object's y-coordinate.
    public int getY() {
        return y;
    }

    // Sets the object's y-coordinate.
    public void setY(int y) {
        this.y = y;
    }

    // A default draw method, to be overridden.
    public void draw(Graphics g) {
        g.drawOval(x, y, DEFAULT_DIAMETER, DEFAULT_DIAMETER);
    }

}
