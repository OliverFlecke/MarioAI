package smith;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class Agent02 extends BasicMarioAIAgent implements Agent {
	
	int jumpCounter = 0;
	int jumpRotation = 0;
	int jumpHeight = 6; //values from 0-6 affect height 
	
//  if ((isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP])) && jumpRotation == 0)
//  {
//      action[Mario.KEY_JUMP] = true;
//      jumpCounter++;
//      
//  } else if ((isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP])) && jumpRotation == 1){
//	  action[Mario.KEY_JUMP] = true;
//	  jumpRotation = 2;
//	  
//  }	else if (jumpRotation == 2){
//	  action[Mario.KEY_JUMP] = false;
//	  jumpCounter = 0;
//	  //jumpRotation = 1;
//	  
//	  jumpRotation = ((int )(Math.random() * 50 + 1)) % 2;
//	  System.out.println(jumpRotation);
//	  
//  }
//  
//  if (jumpCounter > 10){
//	  System.out.println("ok");
//	  jumpCounter = 0;
//	  jumpRotation = 2; 
//  }

//	action[Mario.KEY_RIGHT] = true;
//	action[Mario.KEY_SPEED] = true;
	
	/*
	jump steps
	0 = 1 height
	1 = 2 height
	2 = 3 height
	3 = 3 height
	4 = 3 height
	5 = 3 height
	6 = 4 height
	*/
	
	public Agent02() {
		super("Agent02");
		// TODO Auto-generated constructor stub
	}

	public void reset()
	{
	    action = new boolean[Environment.numberOfKeys];
	}
	
	public void jump(int height) {
		int jumpHeight = height;
		if ((isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP])) && jumpRotation == 0)
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
}
	
	public void walkLeft(boolean isRunning) {
		
		if (isRunning) {
			fire();
		}
		action[Mario.KEY_LEFT] = true;
	}
	
	public void walkRight(boolean isRunning) {
		
		if (isRunning){
			fire();
		}
		action[Mario.KEY_RIGHT] = true;
	}
	
	public void duck() {
		action[Mario.KEY_DOWN] = true;
	}
	
	public void fire() {
		action[Mario.KEY_SPEED] = true;
	}
	
	public void lookUp () {
		action[Mario.KEY_UP] = true;
	}
	
	public boolean[] getAction()
	{
		reset();


		return action;
	}
}
