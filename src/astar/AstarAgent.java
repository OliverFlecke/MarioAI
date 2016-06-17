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

	public boolean outputMemeoryData = false;

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

	public int receptiveFieldWidth;

	public int receptiveFieldHeight;

	private int marioEgoCol;

	private int marioEgoRow;
	
	private String name = "AstarAgent";

	private byte[][] observation;

	private Graph graph;
	
	// Variables for the search
	private final int MAXCOUNT = 4;		// Number of actions to follow at most
	private int actionCount = MAXCOUNT;
	
	// The found path 
	private LinkedList<boolean[]> actionPath = new LinkedList<boolean[]>(); 

	// The elements to simulate the game 
	LevelScene levelScene = null;
	Mario mario;
	// The last position of Mario 
	private float lastX = 0, lastY = 0;
	
	// The action to return to the game 
	boolean[] currentAction = new boolean[Environment.numberOfKeys];
	
	// Flags to run the A* or just a simulation
	private boolean runSimulation = false;
	private boolean runAstar = true;
	
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
				graph = new Graph();
				actionCount = MAXCOUNT;
				// Create a new simulation for the AStar 
				// This is what creates a copy of the game world
				levelScene = new LevelScene();
				
				mario = levelScene.mario;
				mario.x = marioFloatPos[0];
				mario.y = marioFloatPos[1];

				mario.xa = (marioFloatPos[0] - lastX) * 0.89f;
				mario.ya = (marioFloatPos[1] - lastY) * 0.89f;
				// If the speeds is too high, set to max speed
				if (mario.xa > Node.maxSpeed) mario.xa = Node.maxSpeed;
				if (mario.ya > Node.maxSpeed) mario.ya = Node.maxSpeed;
				
				LevelScene.level = new Level(1500, 15);
				levelScene.setup(this.observation, environment.getEnemiesFloatPos());
				levelScene.addSprite(mario);	
				
				// Calculate the current velocity
				// Set the variables with the data from the environment 
				mario.mayJump = isMarioAbleToJump || action[Mario.KEY_JUMP];
				mario.canJump = isMarioAbleToJump || action[Mario.KEY_JUMP];
				mario.onGround = isMarioOnGround;
				
				// Create graph starting point and set goal
				graph.setGoal(marioFloatPos[0] + 144f);
				
				// Search for the best path
				actionPath = graph.searchForPath(new Node(levelScene, currentAction));
			}
			
			// If the action path found by the algorithm is not empty, 
			// it uses the first action in the list
			if (!actionPath.isEmpty())
			{
				if (outputMemeoryData)
				{					
					System.out.printf("Number of nodes: %8d\t", graph.nodeCount);
					System.out.printf("Size: %3d\t", actionPath.size());
					System.out.printf("Ratio: %.2f\n", actionPath.size() /((float) graph.nodeCount));
				}
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
	
	/**
	 * This function is used to test a simulation of Mario running 
	 * along with the real game. 
	 */
	private void runSim() 
	{
//		if (head == null) 
//		{
//			currentAction[Mario.KEY_JUMP] = true;
//			currentAction[Mario.KEY_SPEED] = true;
//			levelScene = new LevelScene();
//			LevelScene.level = new Level(1500, 15);
//			levelScene.setup(this.observation, enemiesFloatPos);
//			mario = levelScene.mario;	
//			levelScene.addSprite(mario);			
//			mario.x = marioFloatPos[0];
//			mario.y = marioFloatPos[1];
//			head = new Node(null, levelScene, currentAction);
//		}
//		else
//		{
//			LevelScene.level.map = this.observation;
//		}
//		
////		mario.x = marioFloatPos[0];
////		mario.y = marioFloatPos[1];
////		mario.xa = (marioFloatPos[0] - lastX) * 0.89f;
////		if (Math.abs(mario.y - marioFloatPos[1]) > 0.1f)
////			mario.ya = (marioFloatPos[1] - lastY) * 0.89f;
//		head.levelScene.tick();
////		System.out.println("Jump time: " + mario.yaa);
//		printMario();
////		printLevelGrid();
//
////		System.out.println("tick\n" );
////		printCreatures(head.levelScene);
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

	/**
	 * Get a given field value from the merged observations from the 
	 * environment interface
	 * @param x coordinate of the given field
	 * @param y coordinate of the given field
	 * @return A value from the observation grid
	 */
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
			runSimulation = true;		// Start a simulation when 7 is pressed
			break;
		case KeyEvent.VK_6:
			System.out.println("Next goal: " + graph.goal + "\tMario X pos: " + marioFloatPos[0]);
			break;
		}
	}

	/**
	 * Output the level grid currently visible
	 */
	public void printLevelGrid()
	{
		for (int i = 0; i <  LevelScene.level.map.length; i++) {
			for (int j = 0; j <  LevelScene.level.map[0].length; j++) {
					System.out.format("%5d ", LevelScene.level.getBlock(i, j));
			}
			System.out.println();
		}
	}
	
	/**
	 * Print the coordinates and data of Mario 
	 */
	public void printMario() 
	{
		System.out.println("Nor - X = " + (marioFloatPos[0]) + " \tY = " + marioFloatPos[1]);
//		System.out.println("Sim - X = " + head.mario.x + "  \tY = " + head.mario.y);
	}
}