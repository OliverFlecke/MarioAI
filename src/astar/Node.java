package astar;

import java.util.*;
import astar.sprites.Mario;
import astar.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;

public class Node implements Comparable<Node> {
	private static boolean debug = false;
	
	private static int timeLimit = 36;
	public static int nodeCount = 0;

	private static long startTime;
	public static float goal = 0;
	public static PriorityQueue<Node> queue;
	
	// Coordinates of the node
	private float x, y;
	
	private boolean marioCanJump = true;
	public int jumpTime = 0;
	
	public float fitness = 0f;				// Overall rating of this option 
	public int depth;					// Depth of the current node
	private boolean[] action;	// Action that are done in this node
	
	// Game elements 
	public Mario mario;			// The Mario object 
	int damageTaken;			// How much damage that Mario will take in this step 
	float maxSpeed = 10.9090909f;
	
	// Enemies 
	public List<Sprite> enemies;
	
	public LevelScene levelScene;
	
	// Graph pointers 
	public Node parent;
	public static Node head;
	public List<Node> children = new ArrayList<Node>();
	private static int maxDepth = 40;
	
	
	
	/**
	 * Create a new node, which should have everything needed to compute next frame
	 * @param parent of the current node
	 * @param levelScene
	 * @param mario
	 * @param enemies
	 * @param action
	 */
	public Node(Node parent, LevelScene levelScene, Mario mario, List<Sprite> enemies, boolean[] action) 
	{
		this.parent = parent;
		if (this.parent == null) this.depth = 0;
		else this.depth = this.parent.depth + 1;
		
		// Copy these elements, don't just save the pointers 
		this.mario = mario; 
		this.levelScene = levelScene;
		this.levelScene.mario = this.mario;
		this.mario.levelScene = this.levelScene;
		this.enemies = levelScene.sprites;
		
		this.action = action;
		
		// Update Mario
		this.mario.keys = this.action;
		this.x = mario.x;
		this.y = mario.y;
		
		Node.nodeCount++;
	}

	public Node(LevelScene levelScene, Mario mario, List<Sprite> enemies, boolean[] action)
	{
		this(null, levelScene, mario, enemies, action);
	}
	
	/**
	 * 
	 */
	private void tick() {
		this.mario.tick();
		if (mario.mayJump() || (!mario.isOnGround() && action[Mario.KEY_JUMP]))
			marioCanJump = true;
	}
	
	
	private static float alpha = 0.5f;
	private static float scalar = 1f;
	
	public float g, h;
	/**
	 *  The function to evaluate the current frame
	 */
	public void fitnessEval()
	{
		// Evalf the simulation
		this.tick();
		
		this.x = mario.x;
		this.y = mario.y;
		
		g =  (this.x - head.x);
		float toGoal = scalar * Math.abs(alpha * (goal - this.x));
		h = 0;
		if (mario.isDead() || this.y > 223f) 
		{
			this.fitness = Integer.MAX_VALUE;
		}
		else 
		{
//			h = toGoal - (this.y * 0.1f);
			h = toGoal;
			this.fitness = h + (20 - mario.xa);
		}

		if (debug)
		{			
			printData(this);
		}
	}

	private static void printData(Node node) {
		System.out.printf("X: %.2f\t", node.x);
		System.out.printf("Y: %.2f\t", node.y);
		System.out.printf("Ya: %.2f\t", node.mario.ya);
		System.out.print(printAction(node.action));
		System.out.println(" Depth: " + node.depth + " F: " + node.fitness + " g " + node.g + " h: " + node.h);
	}
	
	/**
	 * Search for a path from a 
	 * @param head The node to search from
	 * @return A list of action, which contains the optimal path through the world
	 */
	public static LinkedList<boolean[]> searchForPath(Node head, PriorityQueue<Node> queue)
	{
		if (debug) System.out.println("Head: X: " + head.x + " Y: " + head.y + " Goal: " + goal);
		
		Node.nodeCount = 0;
		generateNodes(head, queue);
		
		// Choose to use this, if we find a solution, but want to continue our search
		Node current = queue.poll();
		Node best = current; 
		
		// Set the start time of the search
		setStartTime(System.currentTimeMillis());
		
		while (!current.atGoal())
		{			
//			printData(current);
			// Used when testing. Insuring that the graph does not search to far
			if (current.depth > maxDepth)
			{
				current = queue.poll();
				continue;
			}
						
			if ((System.currentTimeMillis() - getStartTime()) > timeLimit)
			{
				if (debug) System.out.println("Out of time!");
				break;
			}
			generateNodes(current, queue);
			current = queue.poll();
						
			// Update the best node
			if (best.fitness >= current.fitness)
				best = current;
		}
		
		if (debug)
		{			
			System.out.println("Depth: " + best.depth + " Fitness: " + best.fitness);
			System.out.println(Node.nodeCount);
		}
		return getActionPath(best);
	}
	
	/**
	 * Generate all the new nodes which Mario can move to from this
	 */
	public static void generateNodes(Node current, PriorityQueue<Node> queue)
	{
		// Compute all the new positions for the enemies
		List<Sprite> newEnemies = new ArrayList<Sprite>();
//		for (Sprite enemy : this.enemies)
//		{
//			// TODO Clone the current enemy
//			Sprite newEnemy = null; 
//			newEnemy.tick();
//			newEnemies.add(newEnemy);
//		}
		List<boolean[]> options = new ArrayList<boolean[]>();
		
		// Create the different action options
//		options.add(createAction(false, false, false, false));	// Do nothing
		
		// Right movement
		if (current.parent == null || 
				(current.parent != null && Math.abs(current.x - current.parent.x) != 0))
		{
			options.add(createAction(true, false, false, false));
			options.add(createAction(true, false, false, true));
		}
		
		// Left movement
		options.add(createAction(false, true, false, false));
		options.add(createAction(false, true, false, true));
	
		// Check if pressing the jump key makes a differers, and generate nodes if it does
		if (current.canJump())
		{
			options.add(createAction(false, false, true, false));	// Just jump
			options.add(createAction(true, false, true, false));	// Jump and go right
			options.add(createAction(true, false, true, true));		// Jump, go right, and speed
			
			// Left, jump movement
			options.add(createAction(false, true, true, false));
			options.add(createAction(false, true, true, true));
		}
		
		for (boolean[] action : options)
		{
			Node node = createNode(current, action, newEnemies);
			node.fitnessEval();
			queue.add(node);
		}
	}
	
	/*
	 * Computes when Mario is able to jump
	 */
	public boolean canJump() {
		if (parent != null)
		{
			if (this.y == parent.y)
			{
				if (parent.action[Mario.KEY_JUMP])
					return false;
				else 
					return true;
			}
			else if (this.y < parent.y) // Mario is going upwards
				return true;
			else if (this.y > parent.y) // Mario is going downwards
				return false;	
		}
		return true;
	}

	/**
	 * Creates a new node with clones of the level scene and mario,
	 * as well as the passed enemies and action array. The current
	 * node is passed as the parent node
	 * @param actions the new node should simulate
	 * @param enemies which should be passed into the levelscene
	 * @return A node with cloned objects
	 */
	private static Node createNode(Node parent, boolean[] actions, List<Sprite> enemies)
	{
		return new Node(parent, (LevelScene) parent.levelScene, (Mario) parent.mario.clone(), enemies, actions);
	}
	
	/**
	 * Create an action array
	 * @param right
	 * @param left
	 * @param jump
	 * @param speed
	 * @return An action with the passed values
	 */
	public static boolean[] createAction(boolean right, boolean left, boolean jump, boolean speed)
	{
		boolean[] action = new boolean[Environment.numberOfKeys];
		action[Mario.KEY_RIGHT] = right;
		action[Mario.KEY_LEFT] = left;
		action[Mario.KEY_JUMP] = jump;
		action[Mario.KEY_SPEED] = speed;
		action[Mario.KEY_DOWN] = false;
		action[Mario.KEY_UP] = false;
		return action;
	}
	
	/*
	 * Calculate if the Mario in the current node can jump higher
	 */
    public boolean canJumpHigher(boolean jumpParent)
    {
    	if (this.parent != null && jumpParent && this.parent.canJumpHigher(false))
    			return true;
    	return this.mario.mayJump() || (this.mario.jumpTime > 0);
    }
	
	@Override
	public int compareTo(Node other) {
		if (other.fitness - this.fitness > 0)
			return -1;
		else if (other.fitness - this.fitness < 0)
			return 1;
		else return 0;
	}

	/**
	 * Test to see if this node is at the goal
	 * @return True, if this node is at the goal line
	 */
	public boolean atGoal()
	{
		return this.x >= Node.goal;
	}
	
	/**
	 * Set the goal for the current simulation
	 * @param goal Value of the goal
	 */
	public static void setGoal(float goal)
	{
		Node.goal = goal;
	}
	
	public static String printAction(boolean[] action)
	{
		String output = "R: " + ((action[Mario.KEY_RIGHT]) ? "t" : "f") +
				" \tL: " + ((action[Mario.KEY_LEFT]) ? "t" : "f") +
				" \tJ: " + ((action[Mario.KEY_JUMP]) ? "t" : "f") +
				" \tS: " + ((action[Mario.KEY_SPEED]) ? "t" : "f");
		return output;
	}

	/**
	 * Set the start time of the current search
	 * @param currentTimeMillis
	 */
	public static void setStartTime(long currentTimeMillis) {
		Node.startTime  = currentTimeMillis;
	}
	
	/**
	 * Get the start time of the latest search
	 * @return The start time of the latest search
	 */
	public static long getStartTime() {
		return Node.startTime;
	}

	/**
	 * Set the head/starting point of the graph.
	 * @param head
	 */
	public static void setHead(Node head) {
		Node.head = head;
	}

	/**
	 * Get a path of actions to the node
	 * @return A LinkedList with a path representing the actions to get to that node/position 
	 */
	public static LinkedList<boolean[]> getActionPath(Node node)
	{
		// We only want the path from the node AFTER the root. The root does not have any actions
		if (node.parent.depth == 0) 
		{
			LinkedList<boolean[]> list = new LinkedList<boolean[]>();
			list.add(node.action);
			return list;
		}
		LinkedList<boolean[]> list = getActionPath(node.parent);
		list.add(node.action);
		return list;
	}
	
	/**
	 * Not in use!
	 * The main search function to find the optimal path
	 * @param searchNode node to start searching from
	 * @return Should return the best child option
	 */
	public static Node searchChildren(Node searchNode)
	{
		// Base case: If no children, return this node's actions 
		if (searchNode.children == null) return searchNode;
		
		float min = Float.MAX_VALUE;
		Node bestNode = null;
		for (Node node : searchNode.children) 
		{
			if (node.fitness < min)
			{
				min = node.fitness;
				bestNode = node;
			}
		}
		
		return bestNode;
	}
}
