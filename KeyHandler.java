import java.awt.event.*;

public class KeyHandler extends KeyAdapter implements KeyListener{

    private boolean paused = false;
    private boolean printTimes = false;
    private boolean printGenome = false;
    private boolean writeToFile = false;

    public boolean getPaused() { return this.paused; }
    public void setPaused(boolean paused) { this.paused = paused; }

    public boolean getPrintTimes() { return this.printTimes; }
    public void setPrintTimes(boolean p) { this.printTimes = p; }

    public boolean getPrintGenome() { return this.printGenome; }
    public void setPrintGenome(boolean g) { this.printGenome = g; }

    public boolean getWriteToFile() { return this.writeToFile; }
    public void setWriteToFile(boolean w) { this.writeToFile = w; }

    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        if(key == KeyEvent.VK_SPACE) {
            paused = !paused;
        } else if(key == KeyEvent.VK_P) {
	    printTimes = true;
	} else if(key == KeyEvent.VK_W) {
	    writeToFile = true;
	} else if(key == KeyEvent.VK_G) {
	    printGenome = true;
	}
    }

    public void keyReleased(KeyEvent e) {

    }

    public void keyTyped(KeyEvent e) {

    }
}
