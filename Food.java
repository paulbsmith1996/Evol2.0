import java.awt.Color;
import java.awt.Graphics;

public class Food extends GameObject {

	private int amount;
	private int diameter;
	
	public Food(int x, int y, int amount) {
		super(x, y);
		
		this.amount = amount;
		this.diameter = (int)(2 * Math.sqrt(amount));
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
		this.diameter = (int)(2 * Math.sqrt(amount));
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(Color.BLACK);		
		g.drawOval(getX() - (diameter / 2), getY() - (diameter / 2), diameter, diameter);
	}
}
