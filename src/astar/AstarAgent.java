package astar;

import java.util.*;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.environments.Environment;

public class AstarAgent implements Agent {

	private String name = "Astar Agent";
	private boolean[] action = new boolean[Environment.numberOfKeys];
	
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

	}

	@Override
	public void reset() {
		this.action = new boolean[Environment.numberOfKeys];
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

}
