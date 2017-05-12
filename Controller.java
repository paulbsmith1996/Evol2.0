/*
 * Manages all simulation objects.
 */

import java.awt.Graphics;
import java.util.Vector;


public class Controller extends Vector<GameObject> {

    // Calls draw() on every GameObject that is currently in the game,
    // updating all positions graphically.
    public void draw(Graphics g) {
	for (int i = 0; i < size(); i++) {
	    GameObject obj = elementAt(i);
	    obj.draw(g);
	}
    }

    // Tests for death or consumption of GameObject.
    // If the object should be considered dead, it is removed from the controller.
    // NOTE: This method is strangely implemented to deal with the case of multiple
    // removes in one call.

    // Implement new version by creating new vector and adding only alive objects
    public void testObjects() {

        // Keeps track of whether an object was removed. If this is the case, we will
        // make another pass to check that we do not need to remove another GameObject.
        boolean modified = false;

        // NOTE: THIS METHOD NEEDS FIXING! (weird solution to out of bounds error)
	for (int i = 0; i < size(); i++) {

	    // Fetch element.
	    GameObject obj = elementAt(i);

            // Remove food source if it has been consumed.
            if (obj instanceof Food && ((Food) obj).getAmount() <= 0) {
                remove(obj);
                modified = true;
                break;
            } else if (obj instanceof Creature && ((Creature) obj).getFoodPoints() <= 0) {

                // Creature has starved or been eaten
                Creature toDie = (Creature) obj;
                int creatNumAncestors = toDie.getNumAncestors();
                int amountEaten = toDie.getAmountEaten();

		// Add score to sorted vectors
                if (creatNumAncestors >= Evol.generationScores.size()) {
                    Evol.generationScores.add(new Vector<Integer>());
                }

		// Get the vector holding generation scores
                Vector<Integer> creatureGen = Evol.generationScores.elementAt(creatNumAncestors);

                int creatureGenSize = creatureGen.size();
                int index = 0;

                // Get the index where we need to put divideTime to get a sorted vector
                //while (index < creatureGenSize) {
		while(index < creatureGenSize) {
                    if (amountEaten >= creatureGen.elementAt(index)) break;
                    index++;
                }

		// Insert score into sorted vector
                creatureGen.insertElementAt(amountEaten, index);

		// Insert creature with score at position i to ith place
                Evol.currentGenDead.insertElementAt(toDie, index);
                if (!toDie.divided()) {
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
        if (modified) testObjects();

    }

    public void removeFood() {

        boolean modified = false;

	for (int i = 0; i < size(); i++) {
	    GameObject obj = elementAt(i);
            if (obj instanceof Food) {
                remove(obj);
                modified = true;
                break;
            }
        }

        if (modified) removeFood();

    }

    public void removePreds() {

        boolean modified = false;

	for (int i = 0; i < size(); i++) {
	    GameObject obj = elementAt(i);
            if (obj instanceof Creature) {
		if(((Creature)obj).getSpecies() == 1) {
		    remove(obj);
		    modified = true;
		    break;
		}
            }
        }

        if (modified) removePreds();

    }

    // Helpful method that prints out the current number of Creatures that are alive.
    public void printCreatureNum() {
        int count = 0;
	for (int i = 0; i < size(); i++) {
	    GameObject obj = elementAt(i);
            if (obj instanceof Creature && ((Creature) obj).getSpecies() == 0) {
		count++;
            }
        }
        System.out.println(count);
    }
    
}
