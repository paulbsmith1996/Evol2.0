/*
<html>
<applet code="Evol.class" width="200" height="100"></applet>
</html>
*/

/*
 * The main applet driver.
 */

import javax.swing.JApplet;
import javax.swing.JTextPane;
import javax.swing.JSlider;
import javax.swing.JScrollPane;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Vector;


public class Evol extends JApplet implements Runnable {

    
    //========================
    // IMMUTABLE APPLET STATE
    //========================

    // The default frames per second.
    private final int FPS = 30;


    //======================
    // MUTABLE APPLET STATE
    //======================

    // Flag that gets updated during `start` and `stop`.
    private boolean running = false;

    // A thread used to render the correct number of frames per second.
    private Thread ticker;

    // The dimensions of the entire applet window.
    private int windowWidth = 1400;
    private int windowHeight = 800;

    // The dimensions of the interactive menu/display on the right side
    // of the applet.
    private int menuWidth = 300;
    private int menuHeight = windowHeight;

    // The dimensions of the text console at the bottom of the applet window.
    private int consoleWidth = windowWidth - menuWidth;
    private int consoleHeight = 150;

    // The dimensions of the simulation environment.
    private int enviWidth = windowWidth - menuWidth;
    private int enviHeight = windowHeight - consoleHeight;
    
    // The offset and dimensions of the slider within the applet menu.
    private int sliderXOffset = 20;
    private int sliderYOffset = 50;
    private int sliderWidth = menuWidth - (2 * sliderXOffset);
    private int sliderHeight = 50;

    // The offset of food within the simulation environment.
    private int enviOffset = 100;

    // The number of simulation iterations that occur per frame.
    private int movesPerFrame = 10;

    // A counter to keep track of the moves in the current frame.
    private int moveCount = 0;

    // The number of ancestors for the most-developed creature.
    private int mostAncestors = 0;

    // The most developed creature currently in the simulation.
    private Creature mostDevelopedCreature;

    // Whether or not applet graphics are toggled on.
    private boolean graphics = true;

    // A key handler for keyboard interaction with the applet.
    private KeyHandler keyHandler;

    // A string buffer for writing strings to the applet's console.
    private StringBuffer consoleBuffer;

    // The text printed in the applet's console.
    private JTextPane consoleText;

    // The scrollable console pane at the bottom of the applet.
    private JScrollPane console;

    // A slider used to modify the number of moves per frame.
    private JSlider mpfSlider;


    //============================
    // IMMUTABLE SIMULATION STATE
    //============================

    // Maximum number of moves that a creature can make before it dies.
    private final int MAX_TIME_ALIVE = 10000;

    // Initial prey count.
    private final int PREY_START_COUNT = 60;

    // Initial predator count.
    private final int PRED_START_COUNT = 3;

    // Maximum number of food sources that can occur in the environment.
    private final int MAX_FOOD_COUNT = 250;

    // Maximum amount of food that can spawn from any food source.
    private final int MAX_FOOD_SIZE = 10000;

    // Initial food on the board is the maximum amount of food.
    private final int FOOD_START_COUNT = MAX_FOOD_COUNT;

    // Number of food sources generated per frame rendering * 1000.
    private final int FOOD_GEN_RATE = 5000;
    
    //==========================
    // MUTABLE SIMULATION STATE
    //==========================

    // Maintains all game objects currently present in the simulation.
    private Controller controller;

    // Keeps track of the length of time it too each creature
    // in a generation to divide.
    public static Vector<Vector<Long>> generationDivisionTimes;

    // The integer version of generationDivisionTimes.
    public static Vector<Vector<Integer>> generationScores;

    // The creatures that have died in the current generation.
    public static Vector<Creature> currentGenDead;

    // A random number generator.
    private MersenneTwister rand;

    private boolean newGenCreated;
    
    // The number of living prey in the simulation.
    private int numLivingPrey;

    // Whether or not predators are spawned in the environment.
    private boolean predators = false;

    // The number of generations that have occurred.
    private int genCount = 0;

    //================
    // IMPLEMENTATION
    //================

    public MersenneTwister getRand() { return this.rand; }

    // Getter and setter for environment width.
    public void setEnviWidth(int width) { this.enviWidth = width; }
    public int getEnviWidth() { return this.enviWidth; }

    // Getter and setter for environment height.
    public void setEnviHeight(int height) { this.enviHeight = height; }
    public int getEnviHeight() { return this.enviHeight; }

    // Computes the rectangular frame bounding the environment.
    public Rectangle getEnviBounds() { return new Rectangle(0, 0, enviWidth, enviHeight); }

    // Denerates an x-coordinate for a new food source.
    public int nextFoodXPos() {
	//return rand.nextInt(enviWidth - 6 * enviOffset) + 3 * enviOffset; 
	return enviWidth / 2;
	//return rand.nextInt(enviWidth - enviOffset);
	//return rand.nextInt(enviWidth - 6 * enviOffset) + 3 * enviOffset;
    }

    // Generates a y-coordinate for a new food source.
    public int nextFoodYPos() {
	//return 2 * enviHeight / 3; 
	//return rand.nextInt(enviHeight - enviOffset);
	return 2 * enviHeight / 3;
    }

    // Generates a size for a new food source.
    public int nextFoodSize() {
	return rand.nextInt(MAX_FOOD_SIZE);
    }

    // Randomly generates a Creature x-coordinate.
    public int nextCreatureXPos() {
	//return rand.nextInt(enviWidth - 4 * enviOffset) + 2 * enviOffset; 
	//return rand.nextInt(enviWidth - enviOffset);
	return enviWidth / 2;
	//return rand.nextInt(enviWidth - 6 * enviOffset) + 3 * enviOffset;
	// return rand.nextInt(enviHeight - enviOffset);
    }

    // Generates a Creature y-coordinate.
    public int nextCreatureYPos() {
	//return 50 + rand.nextInt(100); 
	//return rand.nextInt(enviHeight - enviOffset);
	return enviHeight / 3;
    }

    // Randomly generates an orientation for a creature to spawn with.
    public double nextCreatureAngle() {
	// return rand.nextInt(4) * Math.PI / 2; 
	return 0;
    }

    // Applet initialization.
    public void init() {

	// Allows the user to click on and set focus to the applet.
        setFocusable(true);

        // Sets the title of the applet.
        Frame title = (Frame) this.getParent().getParent();
        title.setTitle("EVOL: A Simulator of Darwinian Evolution");

	// Initialize a string buffer for writing strings to the applet's console.
	consoleBuffer = new StringBuffer();
	
	// Initializes the key handler and sets the applet to response to 
	// our keystrokes.
        keyHandler = new KeyHandler();
        this.addKeyListener(keyHandler);

	// Initialize a pane for the text printed in the applet's console.
	consoleText = new JTextPane();
	//
	// THIS DOESN'T WORK ???
	//
        consoleText.setEditable(true);
        consoleText.setLocation(0, enviHeight);
        consoleText.setSize(consoleWidth, consoleHeight);

	// Adds the console pane to the applet.
        console = new JScrollPane(consoleText);
        this.add(console);

	// Initializes and adds the moves per frame slider.
        mpfSlider = new JSlider(1, 100);
        mpfSlider.setLocation(enviWidth + sliderXOffset, sliderYOffset);
        mpfSlider.setSize(sliderWidth, sliderHeight);
        mpfSlider.setValue(1);
        this.add(mpfSlider);

	// Initializes the random number generator.
        rand = new MersenneTwister();

	// Initializes a new simulation object controller.
        controller = new Controller();

	newGenCreated = false;

	// Initializes information-logging vectors.
	generationDivisionTimes = new Vector<Vector<Long>>();
	generationScores = new Vector<Vector<Integer>>();
	currentGenDead = new Vector<Creature>();

        // Generate FOOD_START_COUNT random Food sources within reasonable bounds.
        for (int i = 0; i < FOOD_START_COUNT; i++) {
            controller.add(new Food(nextFoodXPos(),
				    nextFoodYPos(),
				    nextFoodSize()));
        }

        // Generate PREY_START_COUNT random Creatures within reasonable bounds.
        for (int i = 0; i < PREY_START_COUNT; i++) {
	    Creature newPrey = new Creature(nextCreatureXPos(),
					    nextCreatureYPos(),
					    nextCreatureAngle(), 0, this);
	    controller.add(newPrey);
        }

	// Set the number of living prey.
        numLivingPrey = PREY_START_COUNT;

        // Generate PRED_START_COUNT random predator creatures within reasonable bounds.
        if (predators) {
            for (int i = 0; i < PRED_START_COUNT; i++) {
                Creature newPred = new Creature(rand.nextInt(enviWidth - enviOffset),
						rand.nextInt(enviHeight - enviOffset),
						0, 1, this);
                controller.add(newPred);
            }
        }

    }

    // Starts the applet.
    public void start() {

	// Start/initialize the ticker.
        if (ticker == null || !ticker.isAlive()) {
            running = true;
            ticker = new Thread(this);
            ticker.setPriority(Thread.MIN_PRIORITY);
            ticker.start();
        }

	// Resize the applet window.
        resize(windowWidth, windowHeight);

	// Bring focus to the applet.
        requestFocusInWindow();

    }

    // Runs the applet simulation until it's stopped.
    public void run() {

	// Loop infinitely until an interrupt is created.
        while (running) {

	    // Bring focus to the applet.
	    //
	    // CHECK IF THIS IS NECESSARY !!!
	    //
            requestFocusInWindow();
	    
	    // If the simulation has been paused, skip this and do nothing.
            if (!keyHandler.getPaused()) {

                // Keep track of number of GameObjects, predators, prey,
		// and food sources.
                int contSize = controller.size();
                int predCount = 0;
                int preyCount = 0;
                int foodCount = 0;

		// Increment the number of "moves" made for th current frame.
                moveCount++;

		// Reset the max number of ancestors so it can be recomputed
		// assuming the best creature died.
                mostAncestors = 0;

                // Get correct counts for food, predators, and prey.
                for (int i = 0; i < contSize; i++) {

                    // Fetch a game object from the controller.
                    GameObject obj = controller.elementAt(i);

		    // Do different work depending on the type of `obj`.
                    if (obj instanceof Creature) {

			// Cast the object as a creature.
			Creature creature = (Creature) obj;

			// Increment the counts and set color, based on
			// whether the creature is a predator or a prey.
                        if (creature.getSpecies() == 1) {
                            predCount++;
                        } else {
                            preyCount++;
                            creature.setColor(Color.BLUE);
                        }

			// Move the creature. If the return value is greater than 0,
			// this means the creature has divided.
			long divideTime = creature.move();
                        if (divideTime > 0) {

			    // Fetch the number of ancestors the creature had before
			    // dividing.
			    int numAncestors = creature.getNumAncestors();

			    // If this is the first in it's generation, add a new vector
			    // to represent a new generation of creatures.
			    if (numAncestors >= generationDivisionTimes.size()) {
				generationDivisionTimes.add(new Vector<Long>());
			    }

			    // Get the generation corresponding with the divided creature.
			    Vector<Long> creatureGen = generationDivisionTimes.elementAt(numAncestors);

			    // Insert the divide time into the sorted vector of divide times.
			    int index = 0;
			    int creatureGenSize = creatureGen.size();
			    while (index < creatureGenSize) {
				if (divideTime <= creatureGen.elementAt(index)) break;
				index++;
			    }
			    creatureGen.insertElementAt(divideTime, index);
			    // generationDivisionTimes.elementAt(numAncestors).add(divideTime);
			
			}

			// If the creature has been alive for more than it's allowed,
			// kill it.
			if (creature.getTimeToDivide() > MAX_TIME_ALIVE) {
			    creature.die();
			}

			// If the creature hasn't died yet, increment it's time alive.
			if (creature.getTimeToDivide() < Creature.EATEN) {
			    creature.setTimeToDivide(creature.getTimeToDivide() + 1);
			}

			// If this creature has the most number of ancestors, it is
			// the most developed, and we should keep track of that.
                        if (creature.getNumAncestors() > mostAncestors) {
                            mostAncestors = creature.getNumAncestors();
                            mostDevelopedCreature = creature;
                        }

                    } else if (obj instanceof Food) {

			// Increment the food count.
                        foodCount++;

                    }

                }

		// Update the number of living prey.
                numLivingPrey = preyCount;

                // Controller removes all dead creatures and consumed food sources.
                controller.testObjects();

		/*
                // Generate food according to if there is enough space for it 
		// (foodCount < MAX_FOOD_COUNT) and according to how quickly it should 
		// be generated. On each frame, there is a (FOOD_GEN_RATE / 1000) probability 
		// of a new food source being randomly generated in the game.
                if (foodCount < MAX_FOOD_COUNT && rand.nextInt(1000) < FOOD_GEN_RATE) {
                    controller.add(new Food(nextFoodXPos(),
					    nextFoodYPos(),
					    nextFoodSize()));

                }
		*/


                // Repopulate the game with more predators if they all die out.
                if (predCount < 1 && rand.nextInt(10) < 4 && predators) {

                    // Create new predator with random x and y coordinates.
                    Creature newPred = new Creature(rand.nextInt(enviWidth - enviOffset),
						    rand.nextInt(enviHeight - enviOffset),
						    0, 1, this);

                    // Experimental code. Assume that number of prey will be very high 
		    // after 2000 divisions and so help predator evolution by making more of them.
                    if (Creature.divideCount < 2000) {
                        controller.add(newPred);
                    } else {
                        for (int i = 0; i < 3; i++) {
                            newPred = new Creature(rand.nextInt(enviWidth - enviOffset),
						   rand.nextInt(enviHeight - enviOffset),
						   0, 1, this);
                            controller.add(newPred);
                        }
                    }
                }

		
		// If all prey has died, create a new generation.
                if (preyCount == 0) createNewGeneration();
            
	    }
	    
	    // If a most-developed creature exists, set it's color to green.
	    if (mostDevelopedCreature != null) {
		mostDevelopedCreature.setColor(Color.GREEN);
	    }
	    
	    // If the genome needs to be printed, add the genome of the most
	    // developed creature to the console buffer (if one exists).
	    if (keyHandler.getPrintGenome()) {
		keyHandler.setPrintGenome(false);
		if (mostDevelopedCreature != null) {
		    consoleBuffer.append(mostDevelopedCreature.getGenome());
		}
	    }
	
	    // We either want to print something to the console or write it to a file.
	    if (keyHandler.getPrintTimes() || keyHandler.getWriteToFile()) {

		// Toggle off print times so it doesn't print multiple times.
		keyHandler.setPrintTimes(false);

		// Add the divide count to the console buffer.
		//consoleBuffer.append(Creature.divideCount + " divisions have occured\n");
		//consoleBuffer.append("Division times for each generation:\n\n");
		
		// Add the generation division times to the console buffer.
		int gdtSize = generationDivisionTimes.size();
		for (int i = 0; i < gdtSize; i++) {
		    Vector<Integer> generationScore = generationScores.elementAt(i);
		    if (generationScore.size() > 30) {
			// consoleBuffer.append(i + ": ");
			
			/*
			  int sum = 0;
			  for (int score: generationScore) {
			      //consoleBuffer.append(l + ", ");
			      //if(l < 100000) {
			      //sum += l;
			      sum += score;
			      //}
			  }

			  consoleBuffer.append((double) sum / (double) generationScore.size());
			*/
			
			consoleBuffer.append("Generation " + i);
			consoleBuffer.append(",  Best Creature: ");
			consoleBuffer.append(generationScore.elementAt(0));
			consoleBuffer.append(",  Median Creature: ");
			consoleBuffer.append(generationScore.elementAt(generationScore.size() / 2));
			//consoleBuffer.append("\n\n");

			if (generationScore.size() != PREY_START_COUNT) {
			    consoleBuffer.append(" num creatures: " + generationScore.size());
			}
			consoleBuffer.append("\n");
		    }
		}
		
		// Add some extra newlines at the end.
		consoleBuffer.append("\n\n\n");
		
		// Print the buffer context to the applet console.
		System.out.print(consoleBuffer.toString());
		
	    }
	    
	    // If we want to write the data to a file.
	    if (keyHandler.getWriteToFile()) {
	
		// Toggle off the flag so this doesn't happen more than once.	
		keyHandler.setWriteToFile(false);
		
		// Open and write the buffer contents to a file.
		try {
		    File f = new File("test.txt");
		    if (f.createNewFile()) {
			System.out.println("file created");
		    }
		    f.setWritable(true);
		    BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
		    out.write(consoleBuffer.toString());
		    out.close();
		    /*
		      PrintWriter pw = new PrintWriter("text.txt", "UTF-8");
		      pw.print(consoleBuffer.toString());
		      pw.close();
		    */
		} catch (Exception e) { 
		    System.out.println("ERROR: Could not write to file."); 
		    e.printStackTrace();
		}

	    }

	    // If the graphics are turned on and the number of moves has reached
	    // its limit for the current frame, repaint the simulation environment.
	    if (graphics && moveCount >= movesPerFrame) {
		
		// Set move count to be the remainding moves per frame.
		moveCount %= movesPerFrame;
		
		// Call draw for all GameObjects in our controller.
		repaint();
		
		// Sleep to allow user's eyes to actually process what is going on.
		try {
		    Thread.sleep(1000 / FPS);
		} catch (InterruptedException e) {
		    //
		    // TODO: Auto-generated catch block
		    //
		    e.printStackTrace();
		}
	    }
	    
	    // Update the moves per frame if the slider has been changed.
	    movesPerFrame = mpfSlider.getValue();
	    
	}

    }

    // Creates a new generation of creatures.
    public void createNewGeneration() {
	
	/*
	  for (Creature c: currentGenDead) {
	  System.out.print(c.getAmountEaten() + " ");
	  }
	  System.out.println("\n");
	*/

	// Increment the generation count.
	genCount++;

	// Remove all food from the environment so it can be re-spawned.
	controller.removeFood();
	
	// Important to compute this outside of for loop. We only want the
	// ancestors to have their genomes passed on, not the current generation's.
	int currentGenDeadSize = currentGenDead.size();
	
	int currentGenDeadIndex = 0;
	
	// boolean restart = true;
	
	/*
	  for (Creature creature: currentGenDead) {
	  if (creature.getAmountEaten() != 0) {
	  restart = false;
	  }
	  }
	*/
	
	/*
	  if(restart) {
	  consoleBuffer.append("No creature ate food. \n");
	  }
	*/
	
	//double eliteProportion = 0.10;
	double eliteProportion = 0.5 - (1 / (2 * genCount + 3));
	
	// Repopulate creatures
	for (int i = 0; i < PREY_START_COUNT * eliteProportion; i++) {

	    // Get an ancestor creature in top eliteProportion% we know to b
	    Creature ancestor = currentGenDead.elementAt(i);
	    
	    // Generate eliteProporiton - 1 mutates from original creature
	    for (int j = 0; j < (1.0 / eliteProportion) - 1; j++) {

		Creature newPrey = new Creature(nextCreatureXPos(), 
						nextCreatureYPos(), 
						nextCreatureAngle(), 0, this);

		
		// Set the genes of the new Creature
		for(int geneNum = 0; geneNum < Creature.NUM_GENES; geneNum++) {
		    newPrey.setGene(geneNum, ancestor.getGene(geneNum));
		}
		
		
		//int numMutations = rand.nextInt(Creature.MUTATION_RATE);
	       
		for (int k = 0; k < Creature.MUTATION_RATE; k++) {
		    newPrey.mutate();
		}

		if(rand.nextInt(1000) > 836) {
		    newPrey.transpose(rand.nextInt(Creature.NUM_GENES), "A");
		}

		/*
		boolean changed = false;

		for(int geneNum = 0; geneNum < Creature.NUM_GENES; geneNum++) {
		    if(newPrey.getGene(geneNum) != ancestor.getGene(geneNum)) {
			changed = true;
		    }
		}

		if(!changed) {
		    System.out.println("Child " + j + " is identical to parent");
		} else {
		    System.out.println("New Genome! For Child " + j);
		    System.out.println("Original genome: " + ancestor.getGenome());
		    System.out.println("Child genome: " + newPrey.getGenome());
		}
		*/
		

		newPrey.setNumAncestors(genCount);
		
		controller.add(newPrey);
	    }
	    
	    
	    // Make new creature that will have same genome as the ancestor
	    Creature ancestorCopy = new Creature(nextCreatureXPos(), 
						 nextCreatureYPos(), 
						 nextCreatureAngle(), 0, this);
	    
	    // Set genes to be the same as the ancestor's and do not mutate
	    for(int geneNum = 0; geneNum < Creature.NUM_GENES; geneNum++) {
		ancestorCopy.setGene(geneNum, ancestor.getGene(geneNum));
	    }	

	    ancestorCopy.setNumAncestors(genCount);
	    
	    controller.add(ancestorCopy);	    
	    
	    
	}

	// Reset/clear the current generation's dead creatures.
	this.currentGenDead = new Vector<Creature>();
	
	// Generate FOOD_START_COUNT random Food sources within reasonable bounds.
	for (int i = 0; i < FOOD_START_COUNT * Math.sqrt(genCount); i++) {
	    controller.add(new Food(nextFoodXPos(),
				    nextFoodYPos(),
				    nextFoodSize()));
	}

	newGenCreated = true;

    }
    
    // Draws applet GUI.
    public void paint(Graphics g) {
	
	//=================
	// DRAW BACKGROUND
	//=================
	
	g.setColor(Color.LIGHT_GRAY);
	g.fillRect(0, 0, enviWidth, enviHeight);

	//===================
	// DRAW GAME OBJECTS
	//===================
	
	if (graphics) controller.draw(g);

	//==============
	// DRAW CONSOLE
	//==============
	
	g.setColor(Color.BLACK);
	console.getVerticalScrollBar().setEnabled(false);
	consoleText.setFont(new Font("Monospaced", 1, 12));
	consoleText.setText(consoleBuffer.toString());
	consoleText.setEditable(true);
	console.setLocation(0, enviHeight);
	console.setSize(consoleWidth, consoleHeight - 1);
	console.getVerticalScrollBar().setEnabled(true);

	//=======================================
	// DRAW MOVES-PER-FRAME SLIDER AND LABEL
	//=======================================
	
	g.setFont(new Font("Times", 1, 12));
	String mpf = "Moves per Frame";
	FontMetrics fm = g.getFontMetrics();
	int fontHeight = fm.getHeight();
	
	mpfSlider.setLocation(enviWidth + sliderXOffset, sliderYOffset);
	mpfSlider.setSize(sliderWidth, sliderHeight);
	mpfSlider.repaint();
	
	g.setColor(Color.WHITE);
	g.fillRect((int) mpfSlider.getX(), (int) mpfSlider.getY() - 5 - fontHeight,
		   mpfSlider.getWidth(), fontHeight);
	
	g.setColor(Color.BLACK);
	g.drawString(mpf + ": " + mpfSlider.getValue(),
		     (int)mpfSlider.getX(),
		     (int)mpfSlider.getY() - 5);

	//============================
	// DRAW SIMULATION STATISTICS
	//============================
	
	g.setColor(Color.WHITE);
	g.fillRect(enviWidth + sliderXOffset, menuHeight - 40 - fontHeight,
		   sliderWidth, fontHeight);
	
	g.setColor(Color.BLACK);
	g.drawString("Max food points hits: " + Creature.divideCount,
		     enviWidth + sliderXOffset, menuHeight - 40);
	
	g.setColor(Color.WHITE);
	g.fillRect(enviWidth + sliderXOffset, menuHeight - 60 - fontHeight,
		   sliderWidth, fontHeight);
	
	g.setColor(Color.BLACK);
	g.drawString("Generation Number: " + mostAncestors,
		     enviWidth + sliderXOffset, menuHeight - 60);
	
	g.setColor(Color.WHITE);
	g.fillRect(enviWidth + sliderXOffset, menuHeight - 80 - fontHeight,
		   sliderWidth, fontHeight);
	
	g.setColor(Color.BLACK);
	g.drawString("Number of Prey: " + numLivingPrey,
		     enviWidth + sliderXOffset, menuHeight - 80);
	
	//==============
	// DRAW BORDERS
	//==============
	
	g.drawRect(enviWidth, 0, menuWidth, menuHeight);
	g.drawRect(0, enviHeight, consoleWidth, consoleHeight);
	
    }
    
    // Stops the applet.
    public void stop() {
	running = false;
    }
    
    // Returns the controller object containing all of the GameObjects
    // currently alive and not consumed.
    public Controller getController() {
	return this.controller;
    }

}
