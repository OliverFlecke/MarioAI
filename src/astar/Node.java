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
	
	public int fitness = 0;				// Overall rating of this option 
	public int depth;					// Depth of the current node
	private boolean[] action;	// Action that are done in this node
	
	// Game elements 
	public Mario mario;			// The Mario object 
	int damageTaken;			// How much damage that Mario will take in this step 
	
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
//		this.levelScene.tick();
		this.mario.tick();
		
//		System.out.println("Depth: " + depth + " \tX: " + x + " \tY: " + y);
		
//		System.out.println("Mario ya: " + mario.yAcc);
//		System.out.println("Mario jump: " + (parent.mario.y - mario.y));
//
//		if (mario.isOnGround())
//			System.out.println("Mario is on the ground");
//		if (mario.mayJump())
//			System.out.println("Can jump!");
		if (mario.mayJump() || (!mario.isOnGround() && action[Mario.KEY_JUMP]))
			marioCanJump = true;
//		else marioCanJump = false;
//		else if (mario.ya < parent.mario.ya)
//			marioCanJump = true;
//		else if (mario.ya >= -2 && mario.ya < 0)
//			marioCanJump = false;
	}
	
	
	private static float alpha = 0.8f;
	private static float scalar = 1f;
	/**
	 *  The function to evaluate the current frame
	 */
	public void fitnessEval()
	{
		// Evalf the simulation
		this.tick();
		
		this.x = mario.x;
		this.y = mario.y;
		
		float g = scalar * (this.x - head.x);
		float toGoal = scalar * Math.abs(alpha * (goal - this.x));
		float h = 0;
		if (mario.isDead() || this.y > 223f || mario.ya > 11f) 
		{
			this.fitness = Integer.MAX_VALUE;
		}
		else 
		{
//			h = toGoal - (this.y * 0.1f);
			h = toGoal;
			this.fitness = Math.round(g + h);
		}

		if (debug)
		{			
			System.out.print("X: " + this.x + " \tY: " + this.y + " \tYa: " + mario.ya + "\t");
			System.out.print(printAction(action));
			System.out.println(" Depth: " + this.depth + " F: " + fitness + " g " + g + " h: " + h);
		}
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
		Node current = queue.remove();
		Node best = current; 
		
		// Set the start time of the search
		setStartTime(System.currentTimeMillis());
		
		while (!current.atGoal())
		{
			// Used when testing. Insuring that the graph does not search to far
			if (current.depth > maxDepth)
			{
				current = queue.remove();
				continue;
			}
						
			if ((System.currentTimeMillis() - getStartTime()) > timeLimit)
			{
				if (debug) System.out.println("Out of time!");
				break;
			}
			generateNodes(current, queue);
			current = queue.remove();
						
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
		Node node;
		
		// Create new nodes with actions 
		// Action: Nothing
//		node = createNode(current, Node.createAction(false, false, false, false, false, false), newEnemies);
//		node.fitnessEval();
//		queue.add(node);
		
		// Action: Move right
		node = createNode(current, Node.createAction(true, false, false, false, false, false), newEnemies);
		node.fitnessEval();
		queue.add(node);
		// Action: Move right with speed
		node = createNode(current, Node.createAction(true, false, false, true, false, false), newEnemies);
		node.fitnessEval();
		queue.add(node);

		// Action: Move left
//		node = createNode(current, Node.createAction(false, true, false, false, false, false), newEnemies);
//		node.fitnessEval();
//		queue.add(node);
//		// Action: Move left with speed
//		node = createNode(current, Node.createAction(false, true, false, true, false, false), newEnemies);
//		node.fitnessEval();
//		queue.add(node);
	
		// Check if pressing the jump key makes a differers, and generate nodes if it does
		if (current.marioCanJump)
		{
			// Action: Jump
			node = createNode(current, Node.createAction(false, false, true, false, false, false), newEnemies);
			node.fitnessEval();
			queue.add(node);
			// Action: Jump and move right
			node = createNode(current, Node.createAction(true, false, true, false, false, false), newEnemies);
			node.fitnessEval();
			queue.add(node);
			// Action: Jump, right and speed
			node = createNode(current, Node.createAction(true, false, true, true, false, false), newEnemies);
			node.fitnessEval();
			queue.add(node);
			
			// Action: Jump and left
//			node = createNode(current, Node.createAction(false, true, true, false, false, false), newEnemies);
//			node.fitnessEval();
//			queue.add(node);
//			// Action: Jump, left and speed
//			node = createNode(current, Node.createAction(false, true, true, true, false, false), newEnemies);
//			node.fitnessEval();
//			queue.add(node);
		}
	
//		// Is mario able to shoot?
//		if (mario.ableToShoot && mario.getMode() == 2 && this.levelScene.fireballsOnScreen < 2)
//		{
//			node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, false, false, true, false, false));
//			node.fitnessEval();
//			queue.add(node);
//		}
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
	 * @param up
	 * @param down
	 * @return An action with the passed values
	 */
	public static boolean[] createAction(boolean right, boolean left, boolean jump, boolean speed, boolean up, boolean down)
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
	
	@Override
	public int compareTo(Node other) {
		return (other.fitness - this.fitness);
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
		
		int min = Integer.MAX_VALUE;
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
