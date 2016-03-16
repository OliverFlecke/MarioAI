package astar;
import astar.level.Level;
import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class AstarAgent implements Agent {

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
		action[Mario.KEY_RIGHT] = true;

		// Jump logic
		boolean jump = false;
//		jump = (getField(1, 0) != 0) || (getField(2, 0) != 0); 
		int jumpHeight = (jump = (getField(1, 0) != 0) && (getField(1, -1) == 0)) ? 1 : 6;
		if (!jump) 
		{
			jump = getField(1, 0) != 0 && getField(1, -1) != 0;
		}
		
		// Check for holes
		if (getField(1, 1) == 0)
		{	
			jump = true;
			for (int i = 1; i < 9; i++)
			{
				jump &= (getField(1, i) == 0);
			}
		}
		
		if (jump && (isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP])) && jumpRotation == 0)
		{
			action[Mario.KEY_JUMP] = true;
			jumpCounter++;
		} else if (jumpRotation == 1){
			action[Mario.KEY_JUMP] = false;
			jumpRotation = 0;
		}
		if (jumpCounter > jumpHeight){
			jumpCounter = 0;
			jumpRotation = 1; 
		}
		
//		if (jump && isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP]))
//			action[Mario.KEY_JUMP] = true;
//		else 
//		{			
//			action[Mario.KEY_JUMP] = false;
//		}
		runSim();
		return action;
	}
	
	private void runSim() {
		
		LevelScene simLevelScene = new LevelScene();
		
		simLevelScene.level = new Level(1500,15);	
		
		printCreatures(simLevelScene);
		
		simLevelScene.setup(observation,enemiesFloatPos);
		
		printCreatures(simLevelScene);
		
		simLevelScene.tick();
		System.out.println("tick\n" );
		
		printCreatures(simLevelScene);
		
		
	}
	
	void printCreatures(LevelScene simLevelScene){
		for(int i=0; i<simLevelScene.getCreaturesFloatPos().length-1; i+=2){
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
	public void setObservationDetails(int rfWidth, int rfHeight, int egoRow,
			int egoCol) {
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
	
}