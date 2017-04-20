/*
<html>
<applet code="Evol.class" width="200" height="100"></applet> 
</html>
*/

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Evol extends Applet implements Runnable {

    /************************************/
    // VARIABLES USED TO MAINTAIN APP CONSTANTS
    /************************************/
    
    private boolean running;
    private Thread ticker;
    
    private final int WINDOW_WIDTH = 1000, WINDOW_HEIGHT = 700;
    private final int OFFSET = 50;
    private final int FPS = 30;
    
    private final boolean GRAPHICS = false;
    
    /************************************/
    // VARIABLES USED TO MAINTAIN GAME OBJECTS
    /************************************/
    
    // Holds all GameObjects currently being taken into account by game
    private Controller controller;
    
    // Used to generate random values
    private Random r;
    
    // Objects used for testing
    private Creature test, test2, enemy, enemy2;
    private Food testFood, testFood2;
    
    /************************************/
    // STARTING CONDITIONS
    /************************************/
    
    
    private final int HERB_START_COUNT = 300;
    private final int PRED_START_COUNT = 10;
    private final int FOOD_START_COUNT = 700; 
    private final int MAX_FOOD = 700;
    private final boolean PREDS_ON= true;
    
    // Number of food sources generated per frame rendering * 1000
    // Default is 2000
    private final int FOOD_GEN_RATE = 1000;
    
    // Bookkeeping for starting main game thread
    public void start() {
	
	if (ticker == null || !ticker.isAlive()) {
	    running = true;
	    ticker = new Thread(this);
	    ticker.setPriority(Thread.MIN_PRIORITY);
	    ticker.start();
	}
    }
    
    // Initialize Random object and Controller object
    public void init() {
	resize(WINDOW_WIDTH, WINDOW_HEIGHT);
	
	r = new Random();
	
	controller = new Controller();
	
	// Generate FOOD_START_COUNT random Food sources within reasonable bounds
	for(int i = 0; i < FOOD_START_COUNT; i++) {
	    controller.add(new Food(r.nextInt(WINDOW_WIDTH - OFFSET), // x coordinate
				    r.nextInt(WINDOW_HEIGHT - OFFSET), // y coordinate
				    r.nextInt(10000))); // size
	}
	
	// Generate HERB_START_COUNT random Creatures within reasonable bounds
	for(int i = 0; i < HERB_START_COUNT; i++) {
	    controller.add(new Creature(r.nextInt(WINDOW_WIDTH - OFFSET), // x coordinate
					r.nextInt(WINDOW_HEIGHT - OFFSET), // y coordinate
					0, 0, this));
	}
	
	// Generate PRED_START_COUNT random predator creatures within reasonable bounds
	if(PREDS_ON) {
	    for(int i = 0; i < PRED_START_COUNT; i++) {
		enemy = new Creature(r.nextInt(WINDOW_WIDTH - OFFSET), // x coordinate
				     r.nextInt(WINDOW_HEIGHT - OFFSET), // y coordinate
				     0, 1, this);
		controller.add(enemy);
	    }
	}
	
	
    }
    
    /**
     * Main method executing game logic
     * Main Thread sleeps for 1000 / FPS millis between each call to run()
     */
    public void run() {
	while(running) {
	    
	    // Keep track of number of GameObjects, predators, food sources, 
	    // and herbivores
	    int contSize = controller.size();
	    int predCount = 0;
	    int vegCount = 0;
	    int foodCount = 0;
	    
	    // Get correct count for foods, herbivores, and preds
	    for(int i = 0; i < contSize; i++) {
		// Loop through all current GameObjects
		GameObject obj = controller.elementAt(i);
		
		
		if(obj instanceof Creature) {
		    
		    // GameObject is a Creature
		    if(((Creature) obj).getSpecies() == 1) {
			// Creature is a predator
			predCount++;
		    } else {
			// Creature is a herbivore
			vegCount++;
		    }
		    ((Creature) obj).move();
		} else if(obj instanceof Food) {
		    
		    // GameObject is Food
		    foodCount++;
		}
	    }
	    
	    // Controller removes all dead creatures and consumed food sources
	    controller.testObjects();
	    
	    // Generate food according to if there is enough space for it (foodCount < MAX_FOOD)
	    // and according to how quickly it should be generated. On each frame, there is a
	    // (FOOD_GEN_RATE / 1000) probability of a new food source being randomly generated
	    // in the game
	    if(foodCount < MAX_FOOD && r.nextInt(1000) < FOOD_GEN_RATE) {
		controller.add(new Food(r.nextInt(WINDOW_WIDTH - OFFSET), 
					r.nextInt(WINDOW_HEIGHT - OFFSET), 
					r.nextInt(10000)));
	    }
	    
	    
	    // Repopulate the game with more predators if they all die out
	    if(predCount < 1 && r.nextInt(10) < 4 && PREDS_ON) {
		
		// Create new preadtor Creature with random x,y coordinates
		Creature newPred = new Creature(r.nextInt(WINDOW_WIDTH - OFFSET), 
						r.nextInt(WINDOW_HEIGHT - OFFSET), 
						0, 1, this);
		
		// Experimental code. Assume that number of herbivores will be very high after 2000 divisions
		// and so help predator evolution by making more of them
		if(Creature.divideCount < 2000) {
		    controller.add(newPred);
		} else {
		    for(int i = 0; i < 10; i++) {
			newPred = new Creature(r.nextInt(WINDOW_WIDTH - OFFSET), 
					       r.nextInt(WINDOW_HEIGHT - OFFSET), 
					       0, 1, this);
			controller.add(newPred);
		    }
		}
	    }
	    
	    
	    // Code to repopulate game with herbivores. Commented out but could be used if needed.
	    /*
	      if(vegCount < 1 && r.nextInt(10) < 4) {
	      Creature newVeg = new Creature(r.nextInt(450), r.nextInt(450), 0, 0, this);
	      controller.add(newVeg);
	      }
	    */
	    
	    if(vegCount == 0) {
		System.out.println("----------ALL HERBIVORES DIED--------------");
		stop();
	    }
	    
	    if (GRAPHICS) {
		// Call draw for all GameObjects in our controller
		repaint();

		// Sleep to allow user's eyes to actually process what is going
		// on.
		// If only we could see more frames per second...
		try {
		    Thread.sleep(1000 / FPS);
		} catch (InterruptedException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}
    }
    

    /**
     * Draw all GameObjects that are currently alive and not consumed
     */
    public void paint(Graphics g) {
	
	/********************* Draw background **********************/
	
	
	g.setColor(Color.LIGHT_GRAY);
	g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
	
	
	

	/********** Draw GameObjects in front of background *********/
	
	
	controller.draw(g);
	
    }

    
    // App should stop running
    public void stop() {
	running = false;
    }
    

    // Returns the controller object containing all of the GameObjects
    // currently alive and not consumed
    public Controller getController() {
	return this.controller;
    }
}
