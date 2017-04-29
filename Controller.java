import java.awt.Graphics;
import java.util.Vector;

public class Controller extends Vector<GameObject> {

    // Calls draw() on every GameObject that is currently in the game,
    // Updating all positions graphically
    public void draw(Graphics g) {
        for(int i = 0; i < size(); i++) {
            GameObject obj = elementAt(i);
            obj.draw(g);
        }
    }

    // Tests for death or consumption of GameObject.
    // If the object should be considered dead, it is removed from the controller
    // Method is strangely implemented to deal with the case of multiple removes
    // in one call
    public void testObjects() {
        
        int size = size();

        // Keeps track of whether an object was removed. If this is the case, we will
        // make another pass to check that we do not need to remove another GameObject
        boolean modified = false;

        // This method needs fixing!! Weird solution to out of bounds error
        for(int i = 0; i < size; i++) {
            GameObject obj = elementAt(i);

            if(obj instanceof Food && ((Food) obj).getAmount() <= 0) {
                // Food source has been consumed

                remove(obj);

                // Update modified to let controller know it needs to make another pass
                modified = true;
                break;
            } else if(obj instanceof Creature && ((Creature) obj).getFoodPoints() <= 0) {
                // Creature has starved or been eaten

		Creature toDie = (Creature)obj;
		
		if(!toDie.divided()) {
		    toDie.setTimeToDivide(Creature.EATEN);
		    int toDieAncestors = toDie.getNumAncestors();
		    
		    if(toDieAncestors >= Evol.generationDivisionTimes.size()) {
			Evol.generationDivisionTimes.add(new Vector<Long>());
		    }
		    
		    Evol.generationDivisionTimes.elementAt(toDie.getNumAncestors()).add(toDie.getTimeToDivide());
		}

                remove(obj);

                // Update modified to let controller know it needs to make another pass
                modified = true;
                break;
            }


        }

        // We remove one GameObject per pass. If a GameObject has been removed, we need to make
        // another pass to make sure that we do not need to remove another. Otherwise, if our pass
        // is clean, we know that we have removed all GameObjects we need to remove.
        if(modified) {
            testObjects();
        }
    }

    // Helpful method that prints out the current number of Creatures that are alive.
    public void printCreatureNum() {
        int count = 0;

        int size = size();

        for(int i = 0; i < size; i++) {
            GameObject obj = elementAt(i);
            if(obj instanceof Creature && ((Creature) obj).getSpecies() == 0) {
                count++;
            }
        }

        System.out.println(count);
    }
}
