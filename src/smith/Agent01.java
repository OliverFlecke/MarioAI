package smith;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class Agent01 extends BasicMarioAIAgent implements Agent {

	public Agent01() {
		super("Agent01");
		// TODO Auto-generated constructor stub
	}

	public void reset()
	{
	    action = new boolean[Environment.numberOfKeys];

	}
	
	public boolean[] getAction()
	{
		action[Mario.KEY_RIGHT] = true;
//		action[Mario.KEY_SPEED] = true;
		
		System.out.println(marioEgoCol + " " + marioEgoRow);
		
		if (isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP]))
			action[Mario.KEY_JUMP] = true;
		else 
			action[Mario.KEY_JUMP] = false;
		return action;
	}
}
