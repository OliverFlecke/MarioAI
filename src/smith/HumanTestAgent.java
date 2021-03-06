package smith;

import java.awt.event.KeyEvent;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.human.HumanKeyboardAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class HumanTestAgent extends HumanKeyboardAgent implements Agent {
	private boolean[] action = null;
	private int[] marioPos = new int[2];
	
	public HumanTestAgent()
	{
		setName("Human Test Agent");
	}

	public void reset()
	{
		action = new boolean[Environment.numberOfKeys];
	}
	
	public boolean[] getAction()
	{
//		marioPos[0] = ((int) marioFloatPos[0] / 16);
//		marioPos[1] = ((int) marioFloatPos[1] / 16);
		byte lv = getField(1, 0);
		
		if (lv != 0)
		{			
//			System.out.println(lv);//+ " X: " + marioPos[0] + " Y: " + marioPos[1]);
		}
		return action;
	}
	
	private byte getField(int x, int y)
	{
		return levelScene[9 + y][9 + x];
	}
	
	public void keyPressed(KeyEvent e)
	{
	    toggleKey(e.getKeyCode(), true);
	}

	public void keyReleased(KeyEvent e)
	{
	    toggleKey(e.getKeyCode(), false);
	}

	protected void toggleKey(int keyCode, boolean isPressed)
	{
		switch(keyCode)
		{
	        case KeyEvent.VK_LEFT:
	            action[Mario.KEY_LEFT] = isPressed;
	            break;
	        case KeyEvent.VK_RIGHT:
	            action[Mario.KEY_RIGHT] = isPressed;
	            break;
	        case KeyEvent.VK_DOWN:
	            action[Mario.KEY_DOWN] = isPressed;
	            break;
	        case KeyEvent.VK_UP:
	            action[Mario.KEY_UP] = isPressed;
	            break;
	
	        case KeyEvent.VK_S:
	            action[Mario.KEY_JUMP] = isPressed;
	            break;
	        case KeyEvent.VK_A:
	            action[Mario.KEY_SPEED] = isPressed;
	            break;
			case KeyEvent.VK_9:
				if (isPressed) printLevelData();
				break;
			case KeyEvent.VK_8:
				if (isPressed) printEnemiesPositions();
				break;
			case KeyEvent.VK_0:
				if (isPressed) printMergedData();
				break;
			case KeyEvent.VK_7:
				if (isPressed) printMarioDetails();
				break;
			case KeyEvent.VK_6:
				if (isPressed) printMarioPos();
				break;
		}
	}

	
	private void printMarioPos() {
//		System.out.println("X: " + marioFloatPos[0] + " Y: " + marioFloatPos[1]);
		System.out.println("X: " + marioPos[0] + " Y: " + marioPos[1]);
	}

	private void printMarioDetails() {
		for (int i = 0; i < marioState.length; i++) {
			System.out.println(marioState[i]);
		}
	}

	private void printEnemiesPositions() {
		for (int i = 0; i < enemiesFloatPos.length; i++)
		{
			System.out.print(enemiesFloatPos[i] + "\t\t");
			if ((i + 1) % 3 == 0) System.out.println();
		}
		System.out.println("-----------------------");
	}

	private void printLevelData()
	{
		for (int x = 0; x < levelScene.length; x++) {
			for (int y = 0; y < levelScene[0].length; y++) {
				System.out.format("%3d ", levelScene[x][y]);
			}
			System.out.println();
		}
		System.out.println("-----------------------");
		for (int x = 0; x < levelScene.length; x++)
		{
			for (int y = 0; y < levelScene[0].length; y++)
			{
				if (levelScene[x][y] != 0)
					System.out.print(x + ":" + y + "\t");
			}
			System.out.println();
		}
		System.out.println("-----------------------");
	}
	
	private void printMergedData()
	{
		for (int i = 0; i < mergedObservation.length; i++) {
			for (int j = 0; j < mergedObservation[0].length; j++) {
				System.out.format("%3d ", mergedObservation[i][j]);
			}
			System.out.println();
		}
		System.out.println("-----------------------");
	}
}
