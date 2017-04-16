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

    private boolean running;
    private Thread ticker;
    
    private final int WINDOW_WIDTH = 500, WINDOW_HEIGHT = 500;
    private final int FPS = 30;
    
    private Controller controller;
    
    private Creature test;
    private Food testFood, testFood2;
    
    private Random r;
    
    // Number of food sources generated per frame rendering * 1000
    private final int FOOD_GEN_RATE = 30;
    
    public void start() {
	if (ticker == null || !ticker.isAlive()) {
	    running = true;
	    ticker = new Thread(this);
	    ticker.setPriority(Thread.MIN_PRIORITY);
	    ticker.start();
	}
    }
    
    public void init() {
	resize(WINDOW_WIDTH, WINDOW_HEIGHT);
	
	r = new Random();
	
	controller = new Controller();
	
	test = new Creature(450, 300, 0, this);
	testFood = new Food(150, 400, 10000);
	testFood2 = new Food(350, 50, 5000);
	
	controller.add(test);
	controller.add(testFood);
	controller.add(testFood2);
    }
    
    public void run() {
	while(running) {
	    
	    int contSize = controller.size();
	    
	    for(int i = 0; i < contSize; i++) {
		GameObject obj = controller.elementAt(i);
		if(obj instanceof Creature) {
		    ((Creature) obj).move();
		}
	    }
	    controller.testObjects();
	    
	    if(r.nextInt(1000) < FOOD_GEN_RATE) {
		controller.add(new Food(r.nextInt(450), r.nextInt(450), r.nextInt(10000)));
	    }
	    
	    repaint();
	    
	    try {
		Thread.sleep(1000 / FPS);
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }
    
    public void paint(Graphics g) {
	
	// Draw background
	g.setColor(Color.LIGHT_GRAY);
	g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
	
	
	
	// Draw objects onto background
	controller.draw(g);
	
    }
    
    public void stop() {
	running = false;
    }
    
    public Controller getController() {
	return this.controller;
    }
}
