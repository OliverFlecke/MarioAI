package smith;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class Agent01 extends BasicMarioAIAgent implements Agent {

	private int[] marioPos = new int[2];
	
	// To specify jump height
	int jumpCounter = 0;
	int jumpRotation = 0;
	
	
	public Agent01() 
	{
		super("Agent01");
	}

	public void reset()
	{
	    action = new boolean[Environment.numberOfKeys];

	}
	
	public boolean[] getAction()
	{
//		action[Mario.KEY_RIGHT] = true;

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
		if (isMarioAbleToJump || (!isMarioOnGround ))
			action[Mario.KEY_JUMP] = true;
		else 
			action[Mario.KEY_JUMP] = false;
		return action;
	}
	
	private byte getField(int x, int y)
	{
//		return levelScene[marioEgoCol + y][marioEgoRow + x];
		return mergedObservation[marioEgoCol + y][marioEgoRow + x];
	}
	
	
}
