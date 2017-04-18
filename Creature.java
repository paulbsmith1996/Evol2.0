import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;

public class Creature extends GameObject {
    
    private double rot;
    
    private double[] xPoints;
    private double[] yPoints;
    
    private int width = 10;
    private int height = 20;
    
    private Random rand;
    
    private int count;
    
    private final int ROTATE_SPEED = 10;
    private final int MOVEMENT_SPEED = 5;
    private final int DIVISION_THRESHOLD = 30000;
    private final int DEFAULT_FP = 2000;
    
    private final int STARVING_RATE = 10;
    
    // How far off the Creature can be from the center of a food source
    private final int MARGIN_OF_ERROR = 25;
    
    private Evol game;
    
    private int gameWidth, gameHeight;
    
    // This will hold the x/y coordinates of two points:
    // the x/y coordinate of the creature and the edge of
    // the window that the creature is looking at
    private int[][] vision;
    
    private boolean stimulated;
    private boolean stimFood;
    private boolean eating;
    
    private Controller enviObjects;
    
    private int foodPoints;
    private int eatSpeed = 100;
    private int childCount = 1;
    
    public Creature(int x, int y, double rot, Evol game) {
	super(x, y);
	
	this.game = game;
	
	this.gameWidth= game.getWidth();
	this.gameHeight = game.getHeight();
	
	this.enviObjects = game.getController();
	
	this.vision = new int[2][2];
	
	this.xPoints = new double[3];
	this.yPoints = new double[3];
	
	this.rot = rot;
	
	this.stimulated = false;
	this.stimFood = false;
	this.eating = false;
	this.foodPoints = DEFAULT_FP;
	
	xPoints[0] = x;
	xPoints[1] = x - width / 2;
	xPoints[2] = x + width / 2;
	
	yPoints[0] = y - 3 * height / 4;
	yPoints[1] = yPoints[2] = y + height / 4;
	
	setSize(foodPoints);
	
	rotate(rot);
	
	count = 0;
	
	rand = new Random();
    }
    
    public int getFoodPoints() { return this.foodPoints; }
    
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
    
    public int[][] getVision() {
	return this.vision;
    }
    
    public void setVision(double rot) {
	
	vision[0][0] = getX();
	vision[0][1] = getY();
	
	if(rot == 0) {
	    // Facing up
	    vision[1][0] = vision[0][0];
	    vision[1][1] = 0;
	} else if(rot == Math.PI / 2) {
	    // Facing left
	    vision[1][0] = 0;
	    vision[1][1] = vision[0][1];
	} else if(rot == Math.PI) {
	    // Facing down
	    vision[1][0] = vision[0][0];
	    vision[1][1] = gameHeight;
	} else {
	    // Facing right
	    vision[1][0] = gameWidth;
	    vision[1][1] = vision[0][1];
	}
    }
    
    
    // The result is that when the creature is facing "towards"
    // an object, it is stimulated
    public void checkStimulus() {
	
	int numObjects = enviObjects.size();
	
	stimulated = false;
	stimFood = false;
	
	for(int i = 0; i < numObjects; i++) {
	    GameObject obj = enviObjects.elementAt(i);
	    
	    int objX = obj.getX();
	    int objY = obj.getY();
	    
	    // Check to see if Creature is on a food source
	    if (Math.abs(vision[0][0] - objX) < MARGIN_OF_ERROR 
		&& Math.abs(vision[0][1] - objY) < MARGIN_OF_ERROR) {
		
		// Make separate if check to check if food in case add more
		// objects later
		if (obj instanceof Food) {
		    eating = true;
		    eat((Food) obj);
		    break;
		}
		
	    }
	    
	    if(vision[1][1] - vision[0][1] < 0 && objY - vision[0][1] < 0 
	       && Math.abs(objX - vision[0][0]) < MARGIN_OF_ERROR) {
		
		// Creature is facing up and sees an object
		stimulated = true;
		
		if(obj instanceof Food) {
		    stimFood = true;
		}
		
	    } else if(vision[1][0] - vision[0][0] < 0 && objX - vision[0][0] < 0 
		      && Math.abs(objY - vision[0][1]) < MARGIN_OF_ERROR) {
		
		// Creature is facing left and sees an object
		stimulated = true;
		
		if(obj instanceof Food) {
		    stimFood = true;
		}
		
	    } else if(vision[1][1] - vision[0][1] > 0 && objY - vision[0][1] > 0 
		      && Math.abs(objX - vision[0][0]) < MARGIN_OF_ERROR) {
		
		// Creature is facing down and sees an object
		stimulated = true;
		
		if(obj instanceof Food) {
		    stimFood = true;
		}
		
	    } else if(vision[1][0] - vision[0][0] > 0 && objX - vision[0][0] > 0 
		      && Math.abs(objY - vision[0][1]) < MARGIN_OF_ERROR) {
		
		// Creature is facing right and sees an object
		stimulated = true;
		
		if(obj instanceof Food) {
		    stimFood = true;
		}
		
	    }
	    
	    eating = false;
	    
	}
	
    }
    
    public void eat(Food food) {
	
	int foodAmount = food.getAmount();
	
	if(eatSpeed <= foodAmount) {
	    // There is enough for the Creature to have a full bite
	    foodPoints += eatSpeed;
	    food.setAmount(foodAmount - eatSpeed);
	} else {
	    // Not enough food for the Creature to have a full bite
	    foodPoints += foodAmount;
	    food.setAmount(0);
	}
	
	setSize(foodPoints);
    }
    
    public void setSize(int size) {
	width = ((int)Math.pow(size, 0.25));
	height = 2 * width;
	
	xPoints[0] = getX();
	xPoints[1] = getX() - width / 2;
	xPoints[2] = getX() + width / 2;
	
	yPoints[0] = getY() - 3 * height / 4;
	yPoints[1] = yPoints[2] = getY() + height / 4;
	
	for(int i = 0; i < 3; i++) {
	    double xTemp = xPoints[i];
	    double yTemp = yPoints[i];
	    
	    xPoints[i] = (Math.cos(rot) * (xTemp - getX()) + Math.sin(rot) * (yTemp - getY())) + getX();
	    yPoints[i] = (-Math.sin(rot) * (xTemp - getX()) + Math.cos(rot) * (yTemp - getY())) + getY();			
	    
	}
	
    }
    
    public void rotate(double angle) {
	
	for(int i = 0; i < 3; i++) {
	    double xTemp = xPoints[i];
	    double yTemp = yPoints[i];
	    
	    xPoints[i] = (Math.cos(angle) * (xTemp - getX()) + Math.sin(angle) * (yTemp - getY())) + getX();
	    yPoints[i] = (-Math.sin(angle) * (xTemp - getX()) + Math.cos(angle) * (yTemp - getY())) + getY();			
	    
	}
	
	rot += angle;
	rot %= (2 * Math.PI);
	
	setVision(rot);
	
    }
    
    public void move() {
	int turn = rand.nextInt(100);
	
	
	checkStimulus();
	
	if (!eating) {
	    if (stimFood) {
		advance(game.getBounds());
	    } else {
		if (count == 0) {
		    if (turn < 30) {
			rotate((Math.PI / 2) * (2 * rand.nextInt(2) - 1));
		    } else {
			advance(game.getBounds());
			count = rand.nextInt(10);
		    }
		} else {
		    advance(game.getBounds());
		    count--;
		}
	    }
	    
	    foodPoints -= STARVING_RATE;
	    setSize(foodPoints);
	} else {
	    count = 0;
	}
	
	if(checkDivide()) {
	    divide();
	}
	
    }
    
    public void advance(Rectangle bounds) {
	
	// The tip of the creature
	int point = 3 * height / 4;
	
	if(rot == 0 && getY() - MOVEMENT_SPEED > point) {
	    // Facing up
	    incY(-MOVEMENT_SPEED);
	} else if(rot == Math.PI / 2 && getX() - MOVEMENT_SPEED > point) {
	    // Facing left
	    incX(-MOVEMENT_SPEED);
	} else if(rot == Math.PI && getY() + MOVEMENT_SPEED < bounds.getHeight() - point) {
	    // Facing down
	    incY(MOVEMENT_SPEED);
	} else if(rot == 3 * Math.PI / 2 && getX() + MOVEMENT_SPEED < bounds.getWidth() - point) {
	    // Facing right
	    incX(MOVEMENT_SPEED);
	} else {
	    rotate(Math.PI / 2);
	}
	
	setVision(rot);
    }
    
    public boolean checkDivide() {
	return foodPoints > DIVISION_THRESHOLD;
    }
    
    public void divide() {
	foodPoints = DEFAULT_FP;
	setSize(foodPoints);
	
	Creature child = new Creature(getX(), getY(), -rot, game);
	
	enviObjects.add(child);
	enviObjects.printCreatureNum();
    }
    
    @Override
	public void draw(Graphics g) {	
	
	int[] approxX = new int[3];
	int[] approxY = new int[3];
	
	for(int i = 0; i < 3; i++) {
	    approxX[i] = (int)xPoints[i];
	    approxY[i] = (int)yPoints[i];
	}
	
	g.setColor(Color.BLACK);
	g.drawPolygon(approxX, approxY, 3);
	
	g.setColor(Color.BLUE);
	g.fillPolygon(approxX, approxY, 3);
	
	if(stimulated) {
	    if(stimFood) {
		g.setColor(Color.GREEN);
	    } else {
		// Sees another Creature
		g.setColor(Color.ORANGE);
	    }
	} else {
	    g.setColor(Color.RED);
	}
	
	g.drawLine(vision[0][0], vision[0][1], vision[1][0], vision[1][1]);
    }
}
