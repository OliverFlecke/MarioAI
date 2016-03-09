package astar;

import java.util.*;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;

public class Node implements Comparator<Node> {
	// Coordinates of the node
	private float x, y;
	
	int fitness = 0;				// Overall rating of this option 
	int depth;					// Depth of the current node
	private boolean[] action;	// Action that are done in this node
	
	// Game elements 
	public Mario mario;			// The Mario object 
	int damageTaken;			// How much damage that Mario will take in this step 
	
	// Enemies 
	public List<Sprite> enemies;
	
	LevelScene levelScene;
	
	// Graph pointers 
	public Node parent;
	public List<Node> children = new ArrayList<Node>();
	
	// Should have everything needed to compute next frame
	public Node(Node parent, LevelScene levelScene, Mario mario, List<Sprite> enemies, boolean[] action) 
	{
		this.parent = parent;
		this.levelScene = levelScene;
		this.mario = mario; 
		this.enemies = enemies;
		this.action = action;
		
		// Update mario
		this.mario.keys = this.action;
	}
	
	// The function to evaluate the current frame
	public void fitnessEval()
	{
		
	}
	
	// The main search function to find the optimal path
	// Should return the best option found 
	public Node searchChildren()
	{
		// Base case: If no children, return this node's actions 
		if (children == null) return this;
		
		int max = Integer.MIN_VALUE;
		Node bestNode = null;
		for (Node node : children) 
		{
			if (node.fitness > max)
			{
				max = node.fitness;
				bestNode = node;
			}
		}
		
		return bestNode;
	}
	
	/**
	 * Get a path of actions to the node
	 * @return A LinkedList with a path representing the actions to get to that node/position 
	 */
	public LinkedList<boolean[]> getActionPath()
	{
		// We only want the path from the node AFTER the root. The root does not have any actions
		if (this.parent.depth == 0) 
		{
			LinkedList<boolean[]> list = new LinkedList<boolean[]>();
			list.add(this.action);
			return list;
		}
		LinkedList<boolean[]> list = this.parent.getActionPath();
		list.add(this.action);
		return list;
	}
	
	/**
	 * Generate all the new nodes which Mario can move to from this
	 * 
	 * Should maybe take a queue and insert the new nodes 
	 */
	public void generateNewNodes(PriorityQueue<Node> queue)
	{
		// TODO Clone the objects
		LevelScene newScene = null;
		// Not sure if mario should be cloned here or in the Node constructor 
		Mario newMario = null;		
		
		// Compute all the new positions for the enemies
		List<Sprite> newEnemies = new ArrayList<Sprite>();
		for (Sprite enemy : this.enemies)
		{
			// TODO Clone the current enemy
			Sprite newEnemy = null; 
			newEnemy.tick();
			newEnemies.add(newEnemy);
		}
		
		// Create new nodes with actions 
		// TODO Should every node have a clone of the levelScene, Mario, and enemies?
		// Action: Nothing
		queue.add(new Node(this, newScene, newMario, newEnemies, Node.createAction(false, false, false, false, false, false)));
		
		// Action: Move right
		queue.add(new Node(this, newScene, newMario, newEnemies, Node.createAction(true, false, false, false, false, false)));
		// Action: Move right with speed
		queue.add(new Node(this, newScene, newMario, newEnemies, Node.createAction(true, false, false, true, false, false)));
	
		// Action: Move left
		queue.add(new Node(this, newScene, newMario, newEnemies, Node.createAction(false, true, false, false, false, false)));
		// Action: Move left with speed
		queue.add(new Node(this, newScene, newMario, newEnemies, Node.createAction(false, true, false, true, false, false)));
	
		// Check if pressing the jump key makes a differers, and generate nodes if it does
		if (mario.jumpTime >= 0)
		{
			// Action: Jump
			queue.add(new Node(this, newScene, newMario, newEnemies, Node.createAction(false, false, true, false, false, false)));
			// Action: Jump and move right
			queue.add(new Node(this, newScene, newMario, newEnemies, Node.createAction(true, false, true, false, false, false)));
			// Action: Jump, right and speed
			queue.add(new Node(this, newScene, newMario, newEnemies, Node.createAction(true, false, true, true, false, false)));
			
			// Action: Jump and left
			queue.add(new Node(this, newScene, newMario, newEnemies, Node.createAction(false, true, true, false, false, false)));
			// Action: Jump, left and speed
			queue.add(new Node(this, newScene, newMario, newEnemies, Node.createAction(false, true, true, true, false, false)));
		}
	
		// Is mario able to shoot?
		if (mario.ableToShoot && mario.getMode() == 2 && this.levelScene.fireballsOnScreen < 2)
		{
			queue.add(new Node(this, newScene, newMario, newEnemies, Node.createAction(false, false, false, true, false, false)));
		}
	}
	
	/**
	 * Create an action array
	 * @param right
	 * @param left
	 * @param jump
	 * @param speed
	 * @param up
	 * @param down
	 * @return An action
	 */
	private static boolean[] createAction(boolean right, boolean left, boolean jump, boolean speed, boolean up, boolean down)
	{
		boolean[] action = new boolean[Environment.numberOfKeys];
		action[Mario.KEY_RIGHT] = right;
		action[Mario.KEY_LEFT] = left;
		action[Mario.KEY_JUMP] = jump;
		action[Mario.KEY_SPEED] = speed;
		action[Mario.KEY_DOWN] = down;
		action[Mario.KEY_UP] = up;		
		return action;
	}
	
	/**
	 * Compare the two nodes based on their fitness
	 * @param a
	 * @param b
	 * @return A positive number, if a is larger
	 */
	@Override
	public int compare(Node a, Node b) {
		return a.fitness - b.fitness;
	}
}