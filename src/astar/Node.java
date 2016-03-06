package astar;

import java.util.List;

import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;

public class Node {

	int fitness;		// Overall rating of this option 
	int depth;			// Depth of the current node
	
	// Game elements 
	public Mario mario;
	int damageTaken;
	
	// Enemies 
	public List<Sprite> enemies;
	
	byte[][] levelScene;
	
	// Graph pointers 
	public Node parent;
	public List<Node> children;
	
	// Should have everything needed to compute next frame
	public Node(byte[][] levelScene) 
	{
		this.levelScene = levelScene;
	}
	
	// The function to evaluate the current frame
	public void fitnessEval()
	{
		
	}
	
	// The main search function to find the optimal path
	// Should return the best option found 
	public boolean[] searchChildren()
	{
		return null;
	}
}