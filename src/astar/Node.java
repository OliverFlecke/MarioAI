package astar;

import java.util.*;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;

public class Node implements Comparator<Node> {
	
	private static float alpha = 1;
	
	// Coordinates of the node
	private float x, y;
	
	public int fitness = 0;				// Overall rating of this option 
	public int depth;					// Depth of the current node
	private boolean[] action;	// Action that are done in this node
	
	// Game elements 
	public Mario mario;			// The Mario object 
	int damageTaken;			// How much damage that Mario will take in this step 
	
	// Enemies 
	public List<Sprite> enemies;
	
	LevelScene levelScene;
	
	// Graph pointers 
	public Node parent, head;
	public List<Node> children = new ArrayList<Node>();
	
	// Should have everything needed to compute next frame
	public Node(Node parent, Node head, LevelScene levelScene, Mario mario, List<Sprite> enemies, boolean[] action) 
	{
		this.parent = parent;
		this.depth = this.parent.depth + 1;
		this.head = head;
		
		// Copy these elements, don't just save the pointers 
		this.levelScene = levelScene;
		this.mario = mario; 
		this.enemies = enemies;
		
		this.action = action;
		
		// Update Mario
		this.mario.keys = this.action;
		this.x = mario.x;
		this.y = mario.y;
	}
	
	// The function to evaluate the current frame
	public void fitnessEval()
	{
		float g = this.head.x;
		float toGoal = alpha * ((head.x + 150f) - this.x);
		float h = 0;
		if (mario.isDead()) 
		{
			this.fitness = Integer.MAX_VALUE;
		}
		else 
		{
			h = toGoal;
			this.fitness = (int) (g + h);
		}
	}
	
	// The main search function to find the optimal path
	// Should return the best option found 
	public Node searchChildren()
	{
		// Base case: If no children, return this node's actions 
		if (children == null) return this;
		
		int min = Integer.MAX_VALUE;
		Node bestNode = null;
		for (Node node : children) 
		{
			if (node.fitness < min)
			{
				min = node.fitness;
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
		Node node;
		
		// Create new nodes with actions 
		// TODO Should every node have a clone of the levelScene, Mario, and enemies?
		// Action: Nothing
		node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, false, false, false, false, false));
		node.fitnessEval();
		queue.add(node);
		
		// Action: Move right
		node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(true, false, false, false, false, false));
		node.fitnessEval();
		queue.add(node);
		// Action: Move right with speed
		node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(true, false, false, true, false, false));
		node.fitnessEval();
		queue.add(node);
	
		// Action: Move left
		node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, true, false, false, false, false));
		node.fitnessEval();
		queue.add(node);
		// Action: Move left with speed
		node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, true, false, true, false, false));
		node.fitnessEval();
		queue.add(node);
	
		// Check if pressing the jump key makes a differers, and generate nodes if it does
		if (mario.jumpTime >= 0)
		{
			// Action: Jump
			node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, false, true, false, false, false));
			node.fitnessEval();
			queue.add(node);
			// Action: Jump and move right
			node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(true, false, true, false, false, false));
			node.fitnessEval();
			queue.add(node);
			// Action: Jump, right and speed
			node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(true, false, true, true, false, false));
			node.fitnessEval();
			queue.add(node);
			
			// Action: Jump and left
			node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, true, true, false, false, false));
			node.fitnessEval();
			queue.add(node);
			// Action: Jump, left and speed
			node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, true, true, true, false, false));
			node.fitnessEval();
			queue.add(node);
		}
	
		// Is mario able to shoot?
		if (mario.ableToShoot && mario.getMode() == 2 && this.levelScene.fireballsOnScreen < 2)
		{
			node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, false, false, true, false, false));
			node.fitnessEval();
			queue.add(node);
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
		return (a.fitness - b.fitness);
	}
}