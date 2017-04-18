import java.awt.Graphics;

public class GameObject {

	private int x, y;
	
	public GameObject(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	// Method to be overridden depending on the gameobject
	public void draw(Graphics g) {
		g.drawOval(x, y, 10, 10);
	}
	
}
