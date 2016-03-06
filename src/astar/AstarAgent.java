package astar;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.environments.Environment;

public class AstarAgent implements Agent {

	private String name = "Astar Agent";
	private boolean[] action = new boolean[Environment.numberOfKeys];
	
	@Override
	public boolean[] getAction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void integrateObservation(Environment environment) {
		// TODO Auto-generated method stub

	}

	@Override
	public void giveIntermediateReward(float intermediateReward) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
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

}
