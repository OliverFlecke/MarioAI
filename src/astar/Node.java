package astar;

import java.util.List;

import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;

public class Node {
	// Coordinates of the node
	private float x, y;
	
	int fitness;				// Overall rating of this option 
	int depth;					// Depth of the current node
	private boolean[] action;	// Action that are done in this node
	
	// Game elements 
	public Mario mario;			// The Mario object 
	int damageTaken;			// How much damage that Mario will take in this step 
	
	// Enemies 
	public List<Sprite> enemies;
	
	byte[][] levelScene;
	
	// Graph pointers 
	public Node parent;
	public List<Node> children;
	
	// Should have everything needed to compute next frame
	public Node(byte[][] levelScene, Mario mario, List<Sprite> enemies, boolean[] action) 
	{
		this.levelScene = levelScene;
		this.mario = mario; 
		this.enemies = enemies;
		this.action = action;
	}
	
	// The function to evaluate the current frame
	public void fitnessEval()
	{
		
	}
	
	// The main search function to find the optimal path
	// Should return the best option found 
	public boolean[] searchChildren()
	{
		// Base case: If no children, return this node's actions 
		if (children == null) return this.action;
		
		int max = Integer.MIN_VALUE;
		boolean[] childAction;
		for (Node node : children) 
		{
			if (node.fitness > max)
			{
				max = node.fitness;
				childAction = node.searchChildren();
			}
		}
		
		return this.action;
	}
}