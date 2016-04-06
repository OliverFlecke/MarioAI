package astar;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.PriorityQueue;

import astar.level.Level;
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
	}

	public void reset()
	{
	    action = new boolean[Environment.numberOfKeys];

	}
	
	public boolean[] getAction()
	{
		//this.environment = environment;
//		action[Mario.KEY_RIGHT] = true;
//		action[Mario.KEY_SPEED] = true;

//		// Jump logic
//		boolean jump = false;
////		jump = (getField(1, 0) != 0) || (getField(2, 0) != 0); 
//		int jumpHeight = (jump = (getField(1, 0) != 0) && (getField(1, -1) == 0)) ? 1 : 6;
//		if (!jump) 
//		{
//			jump = getField(1, 0) != 0 && getField(1, -1) != 0;
//		}
//		
//		// Check for holes
//		if (getField(1, 1) == 0)
//		{	
//			jump = true;
//			for (int i = 1; i < 9; i++)
//			{
//				jump &= (getField(1, i) == 0);
//			}
//		}
//		
//		if (jump && (isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP])) && jumpRotation == 0)
//		{
//			action[Mario.KEY_JUMP] = true;
//			jumpCounter++;
//		} else if (jumpRotation == 1){
//			action[Mario.KEY_JUMP] = false;
//			jumpRotation = 0;
//		}
//		if (jumpCounter > jumpHeight){
//			jumpCounter = 0;
//			jumpRotation = 1; 
//		}
//		
//		if (jump && isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP]))
//			action[Mario.KEY_JUMP] = true;
//		else 
//		{			
//			action[Mario.KEY_JUMP] = false;
//		}
		if (runSimulation)
			runSim();
		
		if (runAstar)
		{
			// If we have no more actions left in the list, compute new actions
			if (actionPath.isEmpty() || actionCount <= 0)
			{
				actionCount = MAXCOUNT;
				// Create a new simulation for the AStar 
				Node.queue = new PriorityQueue<Node>();
				
				levelScene = new LevelScene();
				levelScene.level = new Level(19, 19);
				levelScene.setup(this.observation, enemiesFloatPos);
				mario = new Mario(levelScene);
				levelScene.mario = mario;
				levelScene.addSprite(mario);	
				
				mario.x = marioFloatPos[0];
				mario.y = marioFloatPos[1];
				mario.xa = (marioFloatPos[0] - lastX) * 0.89f;
				if (Math.abs(mario.y - marioFloatPos[1]) > 0.1f)
					mario.ya = (marioFloatPos[1] - lastY) * 0.89f;
				
				mario.mayJump = isMarioAbleToJump;
				mario.onGround = isMarioOnGround;
				
				head = new Node(null, null, levelScene, mario, null, currentAction);	
				Node.setGoal(marioFloatPos[0] + 250f);
				Node.setStartTime(System.currentTimeMillis());
				
				// Search for the best path
				actionPath = head.searchForPath();
			}
			else
			{
				action = actionPath.removeFirst();
				actionCount--;
			}			
		}
		
		lastX = marioFloatPos[0];
		lastX = marioFloatPos[1];
//		printMario();
//		System.out.println(actionPath.size());
		return action;
	}
	
	private final int MAXCOUNT = 2;
	private int actionCount = MAXCOUNT;
	private LinkedList<boolean[]> actionPath = new LinkedList<boolean[]>();
	Node head = null;
	LevelScene levelScene = null;
	Mario mario;
	boolean[] currentAction = new boolean[Environment.numberOfKeys];
	boolean runSimulation = false;
	private boolean runAstar = true;
	private float lastX = 0, lastY = 0;
	
	private void runSim() 
	{
		if (head == null) 
		{
			currentAction[Mario.KEY_JUMP] = true;
			levelScene = new LevelScene();
			levelScene.level = new Level(19, 19);
			levelScene.setup(this.observation, enemiesFloatPos);
			mario = new Mario(levelScene);
			levelScene.mario = mario;
			levelScene.addSprite(mario);
			
			head = new Node(null, null, levelScene, mario, null, currentAction);
		}
		else
		{
			levelScene.level.map = this.observation;
//			for (int i = 0; i < observation.length; i++) {
//				sim.level.map[10][i] = -60;
//			}
		}
		
		mario.x = marioFloatPos[0];
		mario.y = marioFloatPos[1];
		mario.xa = (marioFloatPos[0] - lastX) * 0.89f;
		if (Math.abs(mario.y - marioFloatPos[1]) > 0.1f)
			mario.ya = (marioFloatPos[1] - lastY) * 0.89f;
		
		head.levelScene.tick();
		System.out.println("Jump time: " + mario.yaa);
//		printMario();
//		printLevelGrid();

//		System.out.println("tick\n" );
//		printCreatures(head.levelScene);
	}
	
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

	@Override
	public String getName() {
		return name;
	}

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

	public void printLevelGrid()
	{
//		for (int i = 0; i < observation.length; i++) {
//			for (int j = 0; j < observation[0].length; j++) {
//				if (observation[i][j] != 0)
//					System.out.format("%5d ", observation[i][j]);
//				else 
//				{					
//					System.out.format("%2d-", i);
//					System.out.format("%2d ", j);
//				}
//			}
//			System.out.println();
//		}
//		System.out.println("------------------------------------");
		for (int i = 0; i < observation.length; i++) {
			for (int j = 0; j < observation[0].length; j++) {
//				if (head.levelScene.level.getBlock(i, j) != 0)
				if (i == 9 && j == 9)
					System.out.format("%5d ", 99);
				else
					System.out.format("%5d ", head.levelScene.level.getBlock(i, j));
//				else 
//				{					
//					System.out.format("%2d-", i);
//					System.out.format("%2d ", j);
//				}
			}
			System.out.println();
		}
		System.out.println("------------------------------------");
	}
	
	public void printMario() 
	{
		System.out.println("Nor - X = " + (marioFloatPos[0] + 250) + " \tY = " + marioFloatPos[1]);
//		System.out.println("Sim - X = " + head.mario.x + "  \tY = " + head.mario.y);
	}
}