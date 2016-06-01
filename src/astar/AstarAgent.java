package astar;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;

import astar.level.Level;
import astar.level.TileBehavior;
import ch.idsia.agents.Agent;
import astar.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.tools.*;

public class AstarAgent extends KeyAdapter implements Agent {

	private int[] marioPos = new int[2];
	
	// To specify jump height
	private int jumpCounter = 0;
	private int jumpRotation = 0;
	private Environment environment;

	private int zLevelScene=0;

	private int zLevelEnemies=0;

	private boolean[] action;

	private int marioMode;

	private int marioStatus;

	private boolean isMarioOnGround;

	private boolean isMarioAbleToJump;

	private boolean isMarioAbleToShoot;

	private boolean isMarioCarrying;

	private int getKillsTotal;

	private int getKillsByFire;

	private int getKillsByStomp;

	private int getKillsByShell;

	private byte[][] enemies;

	private byte[][] mergedObservation;

	private float[] marioFloatPos;

	private float[] enemiesFloatPos;

	private int[] marioState;

	private int receptiveFieldWidth;

	private int receptiveFieldHeight;

	private int marioEgoCol;

	private int marioEgoRow;
	
	private String name = "AstarAgent";

	private byte[][] observation;
	
	public AstarAgent() 
	{
		try {
			TileBehavior.loadTileBehaviors();
		} catch (IOException e) {
			System.out.println("issue loading tile.dat");
			e.printStackTrace();
		}
	}

	public void reset()
	{
	    action = new boolean[Environment.numberOfKeys];

	}
	
	public boolean[] getAction()
	{
		if (runSimulation)
		{
			runSim();
			action = Node.createAction(true, false, false, true);
		}
		
//		printMario();
		if (runAstar)
		{
			// If we have no more actions left in the list, compute new actions
			if (actionPath.isEmpty() || actionCount <= 0)
			{
				actionCount = MAXCOUNT;
				// Create a new simulation for the AStar 
				// This is what creates a copy of the game world
				levelScene = new LevelScene();
				levelScene.level = new Level(1500, 15);
				levelScene.setup(this.observation, enemiesFloatPos);
				mario = levelScene.mario;
//				mario = new Mario(levelScene);
//				levelScene.mario = mario;
				levelScene.addSprite(mario);	
				
				mario.x = marioFloatPos[0];
				mario.y = marioFloatPos[1];
//				System.out.println("Mario: x: " + mario.x + " y: " + mario.y);
				
				// Calculate the current velocity
				mario.xa = (marioFloatPos[0] - lastX) * 0.89f;
				// As the speed is going towards zero, 
//				if (Math.abs(marioFloatPos[1] - lastY) > 0.1f)
				mario.ya = (marioFloatPos[1] - lastY) * 0.89f;
//				System.out.println("MarioX " + marioFloatPos[0] + " LastX " + lastX + " xa " + mario.xa);
//				System.out.println("MarioY " + marioFloatPos[1] + " LastY " + lastY + " ya " + mario.ya);
				
				mario.mayJump = isMarioAbleToJump;
				mario.canJump = isMarioAbleToJump;
				mario.onGround = isMarioOnGround;
				
				// Create graph
				head = new Node(levelScene, mario, null, currentAction);
				Node.setHead(head);
				Node.setGoal(marioFloatPos[0] + 250f);
				
				// Search for the best path
				actionPath = Node.searchForPath(head, new PriorityQueue<Node>());
			}
			
			// If the action path found by the algorithm is not empty, 
			// it uses the first action in the list
			if (!actionPath.isEmpty())
			{
				action = actionPath.removeFirst();
				actionCount--;
			}		
			else
			{
				action = Node.createAction(true, false, false, true);
			}
		}
		
		// Save the position of mario for next calculation
		lastX = marioFloatPos[0];
		lastY = marioFloatPos[1];
		
		return action;
	}
	
	private final int MAXCOUNT = 5;
	private int actionCount = MAXCOUNT;
	private LinkedList<boolean[]> actionPath = new LinkedList<boolean[]>();
	Node head = null;
	LevelScene levelScene = null;
	Mario mario;
	boolean[] currentAction = new boolean[Environment.numberOfKeys];
	private boolean runSimulation = false;
	private boolean runAstar = true;
	private float lastX = 0, lastY = 0;
	
	/**
	 * This function is used to test a simulation of Mario running 
	 * along with the real game. 
	 */
	private void runSim() 
	{
		if (head == null) 
		{
			currentAction[Mario.KEY_JUMP] = true;
			currentAction[Mario.KEY_SPEED] = true;
			levelScene = new LevelScene();
			levelScene.level = new Level(1500, 15);
			levelScene.setup(this.observation, enemiesFloatPos);
			mario = levelScene.mario;	
			levelScene.addSprite(mario);			
			mario.x = marioFloatPos[0];
			mario.y = marioFloatPos[1];
			head = new Node(null, levelScene, mario, null, currentAction);
		}
		else
		{
			levelScene.level.map = this.observation;
		}
		
//		mario.x = marioFloatPos[0];
//		mario.y = marioFloatPos[1];
//		mario.xa = (marioFloatPos[0] - lastX) * 0.89f;
//		if (Math.abs(mario.y - marioFloatPos[1]) > 0.1f)
//			mario.ya = (marioFloatPos[1] - lastY) * 0.89f;
		head.levelScene.tick();
//		System.out.println("Jump time: " + mario.yaa);
		printMario();
//		printLevelGrid();

//		System.out.println("tick\n" );
//		printCreatures(head.levelScene);
	}
	
	/**
	 * Print all the position of the creatures to the console
	 * @param simLevelScene with the creatures to output
	 */
	void printCreatures(LevelScene simLevelScene){
		for(int i = 0; i < simLevelScene.getCreaturesFloatPos().length-1; i += 2){
			System.out.println("creature coordinate =(" + simLevelScene.getCreaturesFloatPos()[i] + "," + simLevelScene.getCreaturesFloatPos()[i+1] + ")"); 
		}		
	}

	private byte getField(int x, int y)
	{
//		return levelScene[marioEgoCol + y][marioEgoRow + x];
		return mergedObservation[marioEgoCol + y+1][marioEgoRow + x+1];
	}
	
	/**
	 * Get the information from the environment into the local fields
	 */
	public void integrateObservation(Environment environment)
	{
		this.environment = environment;
	    observation = environment.getLevelSceneObservationZ(zLevelScene);
	    enemies = environment.getEnemiesObservationZ(zLevelEnemies);
	    mergedObservation = environment.getMergedObservationZZ(1, 0);

	    this.marioFloatPos = environment.getMarioFloatPos();
	    this.enemiesFloatPos = environment.getEnemiesFloatPos();
	    this.marioState = environment.getMarioState();

	    receptiveFieldWidth = environment.getReceptiveFieldWidth();
	    receptiveFieldHeight = environment.getReceptiveFieldHeight();

	    // It also possible to use direct methods from Environment interface.
	    //
	    marioStatus = marioState[0];
	    marioMode = marioState[1];
	    isMarioOnGround = marioState[2] == 1;
	    isMarioAbleToJump = marioState[3] == 1;
	    isMarioAbleToShoot = marioState[4] == 1;
	    isMarioCarrying = marioState[5] == 1;
	    getKillsTotal = marioState[6];
	    getKillsByFire = marioState[7];
	    getKillsByStomp = marioState[8];
	    getKillsByShell = marioState[9];
	}

	@Override
	public void giveIntermediateReward(float intermediateReward) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObservationDetails(int rfWidth, int rfHeight, int egoRow, int egoCol) {
		// TODO Auto-generated method stub
	}

	/**
	 * Get the name of the agent
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the agent
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}	
	

	public void keyPressed(KeyEvent e)
	{
		toggleKey(e.getKeyCode(), true);
	}

	public void keyReleased(KeyEvent e)
	{
//		toggleKey(e.getKeyCode(), false);
	}


	/**
	 * Set actions by the user, and to define other keys to 
	 * output useful information in runtime
	 * @param keyCode
	 * @param isPressed
	 */
	protected void toggleKey(int keyCode, boolean isPressed)
	{
		switch (keyCode)
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
			action[Mario.KEY_JUMP] = !action[Mario.KEY_JUMP];
			break;
		case KeyEvent.VK_A:
			action[Mario.KEY_SPEED] = isPressed;
			break;
			
		case KeyEvent.VK_V:
			reset();
			break;
			
			
		case KeyEvent.VK_0:
			printLevelGrid();
			break;
		case KeyEvent.VK_9:
			printMario();
			break;
		case KeyEvent.VK_7:
			runSimulation = true;
			break;
		case KeyEvent.VK_6:
			System.out.println("Next goal: " + Node.goal + "\tMario X pos: " + marioFloatPos[0]);
			break;
		}
	}

	/**
	 * Output the level grid currently visible
	 */
	public void printLevelGrid()
	{
		for (int i = 0; i <  head.levelScene.level.map.length; i++) {
			for (int j = 0; j <  head.levelScene.level.map[0].length; j++) {
					System.out.format("%5d ", head.levelScene.level.getBlock(i, j));
			}
			System.out.println();
		}
	}
	
	public void printMario() 
	{
		System.out.println("Nor - X = " + (marioFloatPos[0]) + " \tY = " + marioFloatPos[1]);
//		System.out.println("Sim - X = " + head.mario.x + "  \tY = " + head.mario.y);
	}
}