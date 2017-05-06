import java.awt.Color;
import java.awt.Graphics;

public class Food extends GameObject {

    // The amount of food at a given 'food source'.
    private int amount;

    // The corresponding diameter of the food source.
    private int diameter;

    // Initialization.
    public Food(int x, int y, int amount) {
        super(x, y);
        this.amount = amount;
        this.diameter = (int) (2 * Math.sqrt(amount));
    }

    // Returns the amount of food left at the food source.
    public int getAmount() {
        return amount;
    }

    // Sets the amount of food at the food source and recomputes the
    // diameter accordingly.
    public void setAmount(int amount) {
        this.amount = amount;
        this.diameter = (int) (2 * Math.sqrt(amount));
    }

    // Custom draw method for drawing food objects.
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawOval(getX() - (diameter / 2), getY() - (diameter / 2), diameter, diameter);
    }
    
}
