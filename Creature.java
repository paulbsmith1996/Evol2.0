/*
 * A creature trying to eat and survive in the simulation environment.
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Scanner;
import java.util.Vector;
import java.math.BigInteger;


public class Creature extends GameObject {

    private double rot;

    private double[] xPoints;
    private double[] yPoints;

    private int width = 10;
    private int height = 20;

    private MersenneTwister rand;

    private final String[] INSTRUCTIONS = {"A", "C", "R", "L"};

    private int count;
    protected static int divideCount = 0;
    protected static int herbDivCount = 0;
    protected static double instCountinGene = 0;
    private int numAncestors;

    private final int ROTATE_SPEED = 10;

    // Original default division threshold is 10000
    private final int DEFAULT_DIVISION_THRESHOLD = 30000;
    private final int START_MOVE_SPEED = 10;
    private final int START_EAT_SPEED = 100;
    private final int DEFAULT_FP = 3000;
    // Original default starving rate is 20
    private final int DEFAULT_STARVING_RATE = 5;
    private final int VISION_DISTANCE = 100;

    // Original default mutation rate is 10
    protected static final int MUTATION_RATE = 5;

    private int starvingRate = DEFAULT_STARVING_RATE;
    // Usually just set to default
    private int divisionThreshold = 10 * DEFAULT_DIVISION_THRESHOLD;

    // How far off the Creature can be from the center of a food source
    private final int MARGIN_OF_ERROR = 25;

    private Evol game;

    private int enviWidth, enviHeight;
    private Rectangle enviBounds;

    // This will hold the x/y coordinates of two points:
    // the x/y coordinate of the creature and the edge of
    // the window that the creature is looking at
    private int[][] vision;

    private int[] genes;
    protected static final int NUM_GENES = 4;
    private int stimuli = 0;

    private boolean stimulated;

    private final int STIM_FOOD = 1;
    private final int STIM_FLEE = 2;
    private final int STIM_FIGHT = 4;
    private final int ON_FOOD = 8;

    // NUM_INSTRUCTIONS * INSTRUCTION_LENGTH <= 32
    private final int NUM_INSTRUCTIONS = 16;
    private final int INSTRUCTION_LENGTH = 2;

    private int fleeing;
    private boolean dead;
    private boolean divided;

    private Controller enviObjects;

    private int foodPoints;
    private int amountEaten;

    private int eatSpeed;
    private int movementSpeed;

    private int species;

    private int instCount;

    private Color color;

    // This will get updated every move, so that it increases as time goes on
    private long timeToDivide;
    protected static final long EATEN = 100000;

    public Creature(int x, int y, double rot, int species, Evol game) {
        super(x, y);

	// Get variables from the Evol app
        rand = game.getRand();
        this.game = game;
        this.enviWidth = game.getEnviWidth();
        this.enviHeight = game.getEnviHeight();
        this.enviBounds = game.getEnviBounds();
        this.enviObjects = game.getController();

        this.numAncestors = 0;

        this.species = species;
        this.genes = new int[NUM_GENES];

        this.vision = new int[2][2];

	// Set Color
        if(species == 0) {
            this.color = Color.BLUE;
        } else {
            this.color = Color.RED;
        }

        this.xPoints = new double[3];
        this.yPoints = new double[3];

        this.rot = 0;

        this.stimulated = false;

        // Set random behavior for genes
        for(int i = 0; i < genes.length; i++) {
            genes[i] = rand.nextInt();
        }

	// Set default parameters
        this.fleeing = 0;
        this.dead = false;
	this.divided = false;

        this.foodPoints = DEFAULT_FP * (species + 1) + (2 * species * DEFAULT_FP);
	this.amountEaten = 0;
        this.movementSpeed = START_MOVE_SPEED;
        this.eatSpeed = START_EAT_SPEED;

        this.starvingRate *= (2 * species + 1);

        this.divisionThreshold *= (4 * species) + 1;

	// Get coordinates of points for Creature
        xPoints[0] = x;
        xPoints[1] = x - width / 2;
        xPoints[2] = x + width / 2;

        yPoints[0] = y - 3 * height / 4;
        yPoints[1] = yPoints[2] = y + height / 4;

	// Set initial size and angle
        setSize(foodPoints);
        rotate(rot);

	// Start all counts to 0
        count = 0;
        instCount = 0;
	this.timeToDivide = 0;
    }

    //===========================
    // GETTER AND SETTER METHODS
    //===========================

    public void setAmountEaten(int ae) { this.amountEaten = ae; }
    public int getAmountEaten() { return this.amountEaten; }

    public void setGene(int geneNum, int gene) { genes[geneNum] = gene; }
    public int getGene(int geneNum) { return this.genes[geneNum]; }

    public void setStimFood() { this.stimuli |= STIM_FOOD; }
    public boolean getStimFood() { return (this.stimuli& STIM_FOOD) != 0; }

    public void setStimFlee() { this.stimuli |= STIM_FLEE; }
    public boolean getStimFlee() { return (this.stimuli & STIM_FLEE) != 0; }

    public void setStimFight() { this.stimuli |= STIM_FIGHT; }
    public boolean getStimFight() { return (this.stimuli & STIM_FIGHT) != 0; }

    public void setOnFood() { this.stimuli |= ON_FOOD; }
    public boolean getOnFood() { return (this.stimuli & ON_FOOD) != 0; }

    public int getNumAncestors() { return this.numAncestors; }
    public void setNumAncestors(int ancestors) { this.numAncestors = ancestors; }

    public void setColor(Color c) { this.color = c; }
    public Color getColor() { return this.color; }

    public long getTimeToDivide() { return this.timeToDivide; }
    public void setTimeToDivide(long ttd) { this.timeToDivide = ttd; }

    public boolean isDead() { return this.dead; }
    public void setDead(boolean d) { this.dead = d; }

    public boolean divided() { return this.divided; }
    public void setDivided(boolean d) { this.divided = d; }
    
    public int getStarvingRate() { return starvingRate; }
    public void setStarvingRate(int starvingRate) { this.starvingRate = starvingRate; }

    public int getSpecies() { return species; }
    public void setSpecies(int species) { this.species = species; }

    public int getFoodPoints() { return this.foodPoints; }
    public void setFoodPoints(int points) { this.foodPoints = points; }

    //=================================
    // INCREMENTER METHODS FOR X AND Y
    //=================================

    public void incX(int increment) {
        for(int i = 0; i < 3; i++) {
            xPoints[i] += increment;
        }

        setX(getX() + increment);
    }

    public void incY(int increment) {
        for(int i = 0; i < 3; i++) {
            yPoints[i] += increment;
        }

        setY(getY() + increment);
    }

    // Getter method for vision
    public int[][] getVision() {
        return this.vision;
    }

    // Set vision at the angle rot
    public void setVision(double rot) {

        vision[0][0] = getX();
        vision[0][1] = getY();

        if (rot == 0) {
            // Facing up
            vision[1][0] = vision[0][0];
            vision[1][1] = Math.max(0, vision[0][1] - VISION_DISTANCE);
        } else if (rot == Math.PI / 2) {
            // Facing left
            vision[1][0] = Math.max(0, vision[0][0] - VISION_DISTANCE);
            vision[1][1] = vision[0][1];
        } else if (rot == Math.PI) {
            // Facing down
            vision[1][0] = vision[0][0];
            vision[1][1] = Math.min(enviHeight, vision[0][1] + VISION_DISTANCE);
        } else {
            // Facing right
            vision[1][0] = Math.min(enviWidth, vision[0][0] + VISION_DISTANCE);
            vision[1][1] = vision[0][1];
        }
    }


    // The result is that when the creature is facing "towards"
    // an object, it is stimulated
    public void checkStimulus() {

        int numObjects = enviObjects.size();

        stimulated = false;

        // Sets the first 4 bits to 0
        stimuli &= (~15);

        for (int i = 0; i < numObjects; i++) {
            GameObject obj = enviObjects.elementAt(i);

            int objX = obj.getX();
            int objY = obj.getY();

            // Check to see if Creature is on a food source
            if (Math.abs(vision[0][0] - objX) < MARGIN_OF_ERROR
            && Math.abs(vision[0][1] - objY) < MARGIN_OF_ERROR) {

                // Make separate if check to check if food in case add more
                // objects later
                if (obj instanceof Food && species < 1 && fleeing <= 0) {
                    //eating = true;
                    setOnFood();

                    eat((Food) obj);

                    break;
                } else if (obj instanceof Creature) {

                    // Creature is on a prey
                    if (((Creature) obj).getSpecies() < species) {
                        kill((Creature) obj);
                    }
                }

            }

            if (vision[1][1] - vision[0][1] < 0 && objY - vision[0][1] < 0
            && objY - vision[0][1] > -VISION_DISTANCE
            && Math.abs(objX - vision[0][0]) < MARGIN_OF_ERROR) {

                // Creature is facing up and sees an object
                stimulated = true;

                if (obj instanceof Food && species < 1) {
                    //stimFood = true;
                    setStimFood();
                } else if (obj instanceof Creature) {
                    if (((Creature) obj).getSpecies() > species) {
                        //stimFlee = true; // die() if too close;
                        setStimFlee();
                    } else if (((Creature) obj).getSpecies() < species) {
                        //stimFight = true; // kill((Creature)obj) if close
                        // enough;
                        setStimFight();
                    }
                }

            } else if (vision[1][0] - vision[0][0] < 0 && objX - vision[0][0] < 0
            && objX - vision[0][0] > -VISION_DISTANCE
            && Math.abs(objY - vision[0][1]) < MARGIN_OF_ERROR) {

                // Creature is facing left and sees an object
                stimulated = true;

                if (obj instanceof Food && species < 1) {
                    //stimFood = true;
                    setStimFood();
                } else if (obj instanceof Creature) {
                    if (((Creature) obj).getSpecies() > species) {
                        //stimFlee = true; // die() if too close;
                        setStimFlee();
                    } else if (((Creature) obj).getSpecies() < species) {
                        //stimFight = true; // kill((Creature)obj) if close
                        // enough;
                        setStimFight();
                    }
                }

            } else if (vision[1][1] - vision[0][1] > 0 && objY - vision[0][1] > 0
            && objY - vision[0][1] < VISION_DISTANCE
            && Math.abs(objX - vision[0][0]) < MARGIN_OF_ERROR) {

                // Creature is facing down and sees an object
                stimulated = true;

                if (obj instanceof Food && species < 1) {
                    setStimFood();
                } else if (obj instanceof Creature) {
                    if (((Creature) obj).getSpecies() > species) {
                        // die() if too close;
                        setStimFlee();
                    } else if (((Creature) obj).getSpecies() < species) {
                        // kill((Creature)obj) if close
                        // enough;
                        setStimFight();
                    }
                }

            } else if (vision[1][0] - vision[0][0] > 0 && objX - vision[0][0] > 0
            && objX - vision[0][0] < VISION_DISTANCE
            && Math.abs(objY - vision[0][1]) < MARGIN_OF_ERROR) {

                // Creature is facing right and sees an object
                stimulated = true;

                if (obj instanceof Food && species < 1) {
                    //stimFood = true;
                    setStimFood();
                } else if (obj instanceof Creature) {
                    if (((Creature) obj).getSpecies() > species) {
                        // die() if too close;
                        setStimFlee();
                    } else if (((Creature) obj).getSpecies() < species) {
                        //kill((Creature)obj) if close
                        // enough;
                        setStimFight();
                    }
                }

            }

            stimuli &= (~ON_FOOD);

        }

    }

    // Set Creature to be dead
    public void die() {
        this.foodPoints = 0;

	if(!divided){
	    this.timeToDivide = EATEN;
	}

        dead = true;
    }

    // Kill the target creature
    public void kill(Creature enemy) {
        foodPoints += enemy.getFoodPoints();
        enemy.die();
    }

    // Called by prey when it is on food and consumes maximum amount
    // possible for a bite
    public void eat(Food food) {

        int foodAmount = food.getAmount();

        if (eatSpeed <= foodAmount) {
            // There is enough for the Creature to have a full bite
            foodPoints += eatSpeed;
	    amountEaten += eatSpeed;
            food.setAmount(foodAmount - eatSpeed);
        } else {
            // Not enough food for the Creature to have a full bite
            foodPoints += foodAmount;
	    amountEaten += foodAmount;
            food.setAmount(0);
        }

    }

    // Set the size of the Creature to size
    public void setSize(int size) {
        width = ((int) Math.pow(size, 0.25));
        height = 2 * width;

        xPoints[0] = getX();
        xPoints[1] = getX() - width / 2;
        xPoints[2] = getX() + width / 2;

        yPoints[0] = getY() - 3 * height / 4;
        yPoints[1] = yPoints[2] = getY() + height / 4;

        for (int i = 0; i < 3; i++) {
            double xTemp = xPoints[i];
            double yTemp = yPoints[i];

            xPoints[i] = (Math.cos(rot) * (xTemp - getX())
            + Math.sin(rot) * (yTemp - getY())) + getX();
            yPoints[i] = (-Math.sin(rot) * (xTemp - getX())
            + Math.cos(rot) * (yTemp - getY())) + getY();

        }

        int dec = 6000 * (species + 1);

        if((START_MOVE_SPEED - (foodPoints / dec)) / (species + 1) > 0) {
            movementSpeed = (START_MOVE_SPEED - (foodPoints / dec)) / (species + 1);
        }

    }

    // Rotate the Creature clockwise by angle
    public void rotate(double angle) {

        for (int i = 0; i < 3; i++) {
            double xTemp = xPoints[i];
            double yTemp = yPoints[i];

            xPoints[i] = (Math.cos(angle) * (xTemp - getX())
            + Math.sin(angle) * (yTemp - getY())) + getX();
            yPoints[i] = (-Math.sin(angle) * (xTemp - getX())
            + Math.cos(angle) * (yTemp - getY())) + getY();

        }

        rot += angle;
        rot %= (2 * Math.PI);

        setVision(rot);

    }
    
    // Execute the next instruction
    public long move() {
        if (!dead) {

	    if(species == 1) {
		checkStimulus();
	    }

            if (species < 1) {
                foodPoints -= starvingRate;
                //setSize(foodPoints);

                // 32 bits in an int - 2 bits per instruction = 16 instructions max
		if(getStimFlee()) {
		    
		    // Spotted a predator
		    execute(genes[0], instCount);

                } else if (getOnFood()) {

                    // On a food source
                    execute(genes[1], instCount);

                } else if (getStimFood()) {

                    // Spotted food
                    execute(genes[2], instCount);

                } else {

                    // Unstimulated
                    execute(genes[3], instCount);

                }

		// Prep to read next instruction
                instCount++;
                instCount %= NUM_INSTRUCTIONS;

		
	    }
        }

        return -1;
	
    }
    
    // Will execute the moves corresponding to the sequence of moves
    // starting at the indicated start bit in the gene
    public void execute(long gene, int inst) {

	// Get the bitcode for the instruction at inst
        int move = (int)((gene >> (INSTRUCTION_LENGTH * inst)) & 3);

        if(move == 3) {
	    // Instruction is "L"
            rotate(Math.PI / 2);
        } else if(move == 2) {
	    // Instruction is "R"
            rotate(3 * Math.PI / 2);
        } else if(move == 1) {
	    // Instruction is "C"
            checkStimulus();
        } else {
	    // Instrcution is "A"
            advance(enviBounds);
        }
    }

    // Move creature forward within the given bounds
    public void advance(Rectangle bounds) {

        // The front tip of the creature
        int point = 3 * height / 4;

        if (rot == 0 && getY() - movementSpeed > point) {
            // Facing up
            incY(-movementSpeed);
        } else if (rot == Math.PI / 2 && getX() - movementSpeed > point) {
            // Facing left
            incX(-movementSpeed);
        } else if (rot == Math.PI && getY() + movementSpeed < bounds.getHeight() - point) {
            // Facing down
            incY(movementSpeed);
        } else if (rot == 3 * Math.PI / 2 && getX() + movementSpeed < bounds.getWidth() - point) {
            // Facing right
            incX(movementSpeed);
        } else {
	    // Running into a wall
            rotate(Math.PI);
        }

	// Update the vision of the creature in case it turned
        setVision(rot);
    }

    // Check that the creature is large enough to divide
    public boolean checkDivide() {
        return foodPoints > divisionThreshold;
    }

    // Obsolete method from Evol1.0 used to kill a creature once it is too large
    // Now no longer called from anywhere
    public long divide() {
	divided = true;
	divideCount++;
	die();
        return timeToDivide;
    }

    // Mutate one instruction in one gene
    public void mutate() {

        // Mutate an instruction randomly
        int mutIndex = (1 + rand.nextInt(3)) << rand.nextInt(32);
	
	// Pick a random gene from the genome to mutate
        int geneNum = rand.nextInt(NUM_GENES);

        // Toggles the bit at the mutIndex index
        setGene(geneNum, genes[geneNum] ^= mutIndex);
    }

    // Performs transposition on gene at geneNum in Creature's genome.
    // Takes a transposon in instruction form(not bitcode form) to simplify calls
    // to transpose().
    // Searches for a sequence of instructions surrounded by transposonAlpha or the
    // reverse of transposonAlpha on both sides and moves it to either prece or follow
    // a new instance of transposonAlpha or its reverse:
    // Gene 2: C R L A R R A C A L R C
    // transpose(2, "A") -> C R L A A C A R R L R C
    // In this example, we transposed "R R" to follow the third "A" instruction
    public int transpose(int geneNum, String transposonAlpha) {

	// Get the gene and create the char[]
	int gene = genes[geneNum];
	char[] transposonLetterArr = transposonAlpha.toCharArray();
	String transposon = "";

	// Translate each letter in transposonAlpha to the bit code
	// it represents
	for(char letter: transposonLetterArr) {
	    
	    String translation = "";
	    if(letter == 'A') {
		translation = "00";
	    } else if(letter == 'C') {
		translation = "01";		
	    } else if(letter == 'R') {
		translation = "10";		
	    } else if(letter == 'L') {
		translation = "11";
	    }
	    
	    transposon += translation;
	}

	int tLength = transposon.length();

	// Compute reverse String of transposon
	char[] transposonLetters = transposon.toCharArray();
	String transposonR = "";
	for(int index = transposonLetters.length - 1; index >= 0; index--) {
	    transposonR += transposonLetters[index];
	}

	// Get the binary representation of the gene
	String geneStr = Integer.toBinaryString(gene);
	while(geneStr.length() < 32) {
	    geneStr = "0" + geneStr;
	}

	// Find first flanking sequence
	int FSIndex = geneStr.indexOf(transposon);
	String geneStrCopy = "" + geneStr;

	// Create a String of "M"s to replace instances of transposon and transposonR
	// We will search for these when trying to find possible positions to transpose
	// our sequence to.
	String mList = "";
	for(int mNum = 0; mNum < tLength; mNum++) {
	    mList += "M";
	}
	
	// Find replications of the flanking sequence and replace with "M"s
	int gscLen = geneStrCopy.length();
	for(int ii = 0; ii + tLength < gscLen; ii += INSTRUCTION_LENGTH) {

	    String instructionCode = geneStrCopy.substring(ii, ii + tLength);
	    if(instructionCode.equals(transposon) || instructionCode.equals(transposonR)) {
		geneStrCopy = geneStrCopy.substring(0, ii) + mList 
		    + geneStrCopy.substring(ii + tLength);
	    }

	}

	// Get all indices preceded or followed by the transposon or its reverse
	// and save in a Vector
	Vector<Integer> possiblePositions = new Vector<Integer>();
	int cutOffFromStart = 0;
	while(geneStrCopy.contains(mList)) {

	    int firstInstanceIndex = geneStrCopy.indexOf(mList);
	    
	    if(!possiblePositions.contains(firstInstanceIndex + cutOffFromStart)) {
		possiblePositions.add(firstInstanceIndex + cutOffFromStart);
	    }

	    if(!possiblePositions.contains(firstInstanceIndex + cutOffFromStart + tLength)
	       && firstInstanceIndex + cutOffFromStart + tLength < geneStr.length()) {
		possiblePositions.add(firstInstanceIndex + cutOffFromStart + tLength);
	    }

	    geneStrCopy = geneStrCopy.substring(firstInstanceIndex + mList.length());
	    cutOffFromStart += firstInstanceIndex + tLength;
	}

	// Choose a random place to move the sequence to
	if(possiblePositions.size() > 0) {

	    int oldPosIndex = rand.nextInt(possiblePositions.size() - 1);
	    int oldPos = possiblePositions.elementAt(oldPosIndex);

	    String seqTransposing = geneStr.substring(oldPos, 
						   possiblePositions.elementAt(oldPosIndex + 1));

	    int newPos = possiblePositions.elementAt(rand.nextInt(possiblePositions.size()));

	    
	    // Move transposon to the new spot in the gene
	    if(oldPos < newPos) {
		geneStr = geneStr.substring(0, oldPos)
		    + geneStr.substring(oldPos + seqTransposing.length(), newPos)
		    + seqTransposing
		    + geneStr.substring(newPos);
	    } else if(oldPos > newPos) {
		geneStr = geneStr.substring(0, newPos) 
		    + seqTransposing
		    + geneStr.substring(newPos, oldPos)
		    + geneStr.substring(oldPos + seqTransposing.length());
	    } else {
		return -1;
	    }

	    // Update Creature's genome
	    setGene(geneNum, new BigInteger(geneStr, 2).intValue());

	    return 0;
	} else {
	    return -1;
	}
	

    }

    // Print the species of the creature, followed by its genome in readable form
    public void printGenome() {
        System.out.print("Species: " + species + "\n\n"
        + "Gene 1 (See Predator): " + toInst(genes[0], NUM_INSTRUCTIONS) + "\n"
        + "Gene 2 (On Food)     : " + toInst(genes[1], NUM_INSTRUCTIONS) + "\n"
        + "Gene 3 (See Food)    : " + toInst(genes[2], NUM_INSTRUCTIONS) + "\n"
        + "Gene 4 (Unstimulated): " + toInst(genes[3], NUM_INSTRUCTIONS) + "\n"
        + "----------------------------------------------\n\n");
    }

    // Return the species of the creature, followed by its genome in readable form    
    public String getGenome() {
        return "\n\nSpecies: " + species + "\n\n"
        + "Gene 1 (See Predator): " + toInst(genes[0], NUM_INSTRUCTIONS) + "\n"
        + "Gene 2 (On Food)     : " + toInst(genes[1], NUM_INSTRUCTIONS) + "\n"
        + "Gene 3 (See Food)    : " + toInst(genes[2], NUM_INSTRUCTIONS) + "\n"
        + "Gene 4 (Unstimulated): " + toInst(genes[3], NUM_INSTRUCTIONS) + "\n\n"
        + "----------------------------------------------\n";

    }

    // Print the gene at geneNum in the creature's genome in readable form
    public void printGene(int geneNum) {
        System.out.println(toInst(genes[geneNum], NUM_INSTRUCTIONS));
    }

    // Get the count of inst in the gene at geneNum in the creature's genome
    public int getInstCountinGene(int geneNum, String inst) {

	// Evaluates to true if the passed String is not a valid instruction
        boolean err = true;

	// Count of instructions in the gene
        int count = 0;

	// Check that we have a valid instruction
        for(String s: INSTRUCTIONS) {
            if(inst.equals(s)) {
                err = false;
                break;
            }
        }

	// Error print if inst is not a valid instruction
        if(err) {
            System.err.println("PrintInstCountinGene given invalid instruction String");
        }

	// Count the number of occurences of the given instructions
        String instList = toInst(genes[geneNum - 1], NUM_INSTRUCTIONS);
        Scanner listScan = new Scanner(instList);
        String next = "";

        while(listScan.hasNext()) {
            next = listScan.next();
            if(next.equals(inst)) {
                count++;
            }
        }

        return count;
    }

    // Return a readable version of the gene, which is an integer representation of
    // a binary String
    public String toInst(int gene, int numInst) {

        String result = "";

        for(int i = 0; i < numInst; i++) {

	    // Convert each bit code iteratively
            int toConvert = gene >> (i * INSTRUCTION_LENGTH) & 3;
            String inst = INSTRUCTIONS[toConvert];
	    result += inst + " ";

	}

	return result;
    }

    @Override
    // Method to draw a creature onto the environment. Overrides the GameObject draw()
    // method. Draws the Creature body, as well as its vision
	public void draw(Graphics g) {
	
	// Get integer approximations of the x and y coordinates of the Creature
	int[] approxX = new int[3];
	int[] approxY = new int[3];
	
	for (int i = 0; i < 3; i++) {
	    approxX[i] = (int) xPoints[i];
	    approxY[i] = (int) yPoints[i];
	}
	
	// Draw Creature border
	g.setColor(Color.BLACK);
	g.drawPolygon(approxX, approxY, 3);
	
	// Color the Creature appropriately
	g.setColor(this.color);
	g.fillPolygon(approxX, approxY, 3);

	// Determine color of vision according to stimulus and draw vision
	if (stimulated) {
	    if (getStimFlee()) {
		// Negatively Stimulated
		g.setColor(Color.RED);
	    } else if (getStimFood() || getStimFight()) {
		// Positively Stimulated
		g.setColor(Color.GREEN);
	    } else {
		// Unstimulated
		g.setColor(Color.ORANGE);
	    }
	} else {
	    // If no stimulation, then unstimulated
	    g.setColor(Color.ORANGE);
	}
	
	g.drawLine(vision[0][0], vision[0][1], vision[1][0], vision[1][1]);
    }

    // Tests utility methods. Currently testing effectiveness of transposition
    public static void main(String[] args) {

	for(int temp = 0; temp < 1000; temp++) {
	    Creature c = new Creature(0, 0, 0, 0, new Evol());

	    String originalGenome = c.getGenome();
	    
	    if(c.transpose(0, "AA") == -1) {
		System.out.println("Could not transpose on this gene.");
	    }
	}
    }
}
