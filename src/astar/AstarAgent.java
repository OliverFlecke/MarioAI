package astar;

<<<<<<< HEAD
import astar.level.Level;
=======
import java.util.*;

>>>>>>> origin/master
import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class AstarAgent extends BasicMarioAIAgent implements Agent {

	private int[] marioPos = new int[2];
	
<<<<<<< HEAD
	// To specify jump height
	int jumpCounter = 0;
	int jumpRotation = 0;
	
	
	public AstarAgent() 
	{
		super("Agent01");
	}

	public void reset()
	{
	    action = new boolean[Environment.numberOfKeys];
=======
	private final int numberOfSteps = 2;
	private int count = 0;
	private Stack<boolean[]> actions;
	
	@Override
	public boolean[] getAction() {
		// Only run this after numberOfSteps or if the actions stack is empty
		if (!(count++ < this.numberOfSteps) || actions.isEmpty())
		{
			// Run the A-star to get a list of steps
			count = 0;
		}
		return actions.pop();
	}

	@Override
	public void integrateObservation(Environment environment) {

	}

	@Override
	public void giveIntermediateReward(float intermediateReward) {
>>>>>>> origin/master

	}
	
	public boolean[] getAction()
	{
		action[Mario.KEY_RIGHT] = true;

<<<<<<< HEAD
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
=======
	@Override
	public void reset() {
		this.action = new boolean[Environment.numberOfKeys];
>>>>>>> origin/master
	}
	
	private void runSim() {
		LevelScene levelScene = new LevelScene();
		
		levelScene.level = new Level(1500,15);	
		
		printCreatures(levelScene);
		
		levelScene.tick();
		
		printCreatures(levelScene);
		
		levelScene.setup(environment.getLevelSceneObservationZ(1),environment.getEnemiesObservationZ(0));
	}
	void printCreatures(LevelScene levelScene){
		for(int i=0; i<levelScene.getCreaturesFloatPos().length-1; i++){
			System.out.println("creature coordinate =(" + levelScene.getCreaturesFloatPos()[i] + "," + levelScene.getCreaturesFloatPos()[i+1] + ")"); 
			
		}		
	}

	private byte getField(int x, int y)
	{
//		return levelScene[marioEgoCol + y][marioEgoRow + x];
		return mergedObservation[marioEgoCol + y][marioEgoRow + x];
	}
	
	
}