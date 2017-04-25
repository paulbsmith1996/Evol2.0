import java.awt.event.*;

public class KeyHandler extends KeyAdapter implements KeyListener{

    private boolean paused = false;

    public boolean getPaused() { return this.paused; }

    public void keyPressed(KeyEvent e) {
	
	int key = e.getKeyCode();
	if(key == KeyEvent.VK_SPACE) {
	    paused = !paused;
	}
    }

    public void keyReleased(KeyEvent e) {

    }

    public void keyTyped(KeyEvent e) {

    }
}