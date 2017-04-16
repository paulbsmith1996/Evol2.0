import java.awt.Graphics;
import java.util.Vector;

public class Controller extends Vector<GameObject> {

	public void draw(Graphics g) {
		for(GameObject obj: this) {
			obj.draw(g);
		}
	}
	
	public void testObjects() {
		int size = size();
		boolean modified = false;
		
		// This method needs fixing!! Weird solution to out of bounds error
		for(int i = 0; i < size; i++) {
			GameObject obj = elementAt(i);
			if(obj instanceof Food && ((Food) obj).getAmount() <= 0) {
				remove(obj);
				modified = true;
				break;
			} else if(obj instanceof Creature && ((Creature) obj).getFoodPoints() <= 0) {
				remove(obj);				
				modified = true;
				break;
			}
			
			
		}
		
		if(modified) {
			testObjects();
		}
	}
	
	public void printCreatureNum() {
		int count = 0;
		
		int size = size();
		
		for(int i = 0; i < size; i++) {
			GameObject obj = elementAt(i);
			if(obj instanceof Creature) {
				count++;
			}
		}
		
		System.out.println(count);
	}
}
