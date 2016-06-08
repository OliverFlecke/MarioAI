package astar;

import java.util.*;
import astar.sprites.Mario;
import astar.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;

public class Node implements Comparable<Node> {
	private static boolean debug = false;
	
	private static int timeLimit = 33;	// Any larger, and the game seem to lack 
	public static int nodeCount = 0;

	private static long startTime;
	public static float goal = 0;
	public static PriorityQueue<Node> queue;
	
	// Coordinates of the node
	public float x, y;
	
	
	public float fitness = 0f;				// Overall rating of this option 
	public int depth;					// Depth of the current node
	private boolean[] action;	// Action that are done in this node
	
	// Game elements 
	public Mario mario;			// The Mario object 
	int damageTaken;			// How much damage that Mario will take in this step 
	public int jumpTime = 0;
	public final static float maxSpeed = 10.9090909f;
	
	// Enemies 
	public List<Sprite> enemies;
	
	public LevelScene levelScene;
	
	// Graph pointers 
	public Node parent;
	public static Node head;
	public List<Node> children = new ArrayList<Node>();
	private static int maxDepth = 1000;
	
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

	/**
	 * Create a node without a parent
	 * @param levelScene
	 * @param mario
	 * @param enemies
	 * @param action
	 */
	public Node(LevelScene levelScene, Mario mario, List<Sprite> enemies, boolean[] action)
	{
		this(null, levelScene, mario, enemies, action);
	}
	
	/**
	 * Ticked in every frame
	 */
	private void tick() 
	{
		this.mario.tick();
	}
	
	// Helper variables to the fitness evaluations 
	private static float alpha = 0.5f;
	private static float scalar = 1f;
	
	public float g, h;
	
	/**
	 *  The function to evaluate the current frame
	 */
	public void fitnessEval()
	{
		// Evaluate the simulation
		this.tick();
		
		if (mario.x == 816f)
		{
			mario.x = (mario.xOld + mario.xa * 1.12f);
		}
		this.x = mario.x;
		this.y = mario.y;
		
		g = getDistanceTraveled(this);
		
		if (mario.isDead() || this.y > 223f) 
		{
			this.fitness = Integer.MAX_VALUE;
		}
		else 
		{
			h = getHeuristic(this);
			this.fitness = h;
		}
	}
	
	/**
	 * Get the heuristic for a given node
	 * @param node to get the heuristic value from
	 * @return A float, representing 
	 */
	public static float getHeuristic(Node node)
	{
//		return alpha * (goal - node.x);
		return alpha * (goal - node.x) 
				+ (Node.maxSpeed - node.mario.xa); 
	}
	
	/**
	 * Get the distance which the passed node has already traveled
	 * @param node to get the distance from 
	 * @return The distance with the given node is from the starting point
	 */
	public static float getDistanceTraveled(Node node)
	{
		return (node.x - head.x);
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
			if (debug) printNodeData(current);
			
			// Used when testing. Insuring that the graph does not search too far
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
			// Generate the children for this node
			generateNodes(current, queue);
			if (queue.isEmpty()) break;	// If there are no more options, end the search
			
			current = queue.poll();	// Poll the new best options
						
			// Update the best node
			if (best.fitness >= current.fitness)
				best = current;				
		}
		
		if (debug)
		{			
			System.out.println("Depth: " + best.depth + " Fitness: " + best.fitness);
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
		
		// Right movement - Only created if it is possible to go right, or if mario is in the air
		if (current.parent == null || 
				(Math.abs(current.x - current.parent.x) > 0.7) 
				|| (current.y != current.parent.y)
				)
		{
			options.add(createAction(true, false, false, false));
			options.add(createAction(true, false, false, true));

			// Left movement
			options.add(createAction(false, true, false, false));
			options.add(createAction(false, true, false, true));	
		}

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
	
	/**
	 * Computes if Mario is able to jump
	 * @return True, if Mario is able to jump
	 */
	public boolean canJump() 
	{
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
		return new Node(parent, (LevelScene) parent.levelScene.clone(), (Mario) parent.mario.clone(), enemies, actions);
	}
	
	/**
	 * Create an action array with the given actions, which should
	 * get mario to moved in the given direction
	 * @param right True, if mario should move to the right
	 * @param left True, if  mario should move to the left 
	 * @param jump True, if mario should jump
	 * @param speed True, if mario should run or fire a fireball
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
	
	@Override
	/**
     * Compare this node to another. 
     * @return -1, if the other node's fitness is larger than this nodes fitness. 
     * 0 is returned if the have the same fitness, and 1 is returned if this node has 
     * the larger fitness.
	 */
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
		LinkedList<boolean[]> list;
		// We only want the path from the node AFTER the root. The root does not have any actions
		if (node.parent.depth == 0) 
			list = new LinkedList<boolean[]>();
		else 
			list = getActionPath(node.parent);
		list.add(node.action);
		
		// Output debug information
		if (debug) printNodeData(node);
		return list;
	}
	
	/**
	 * Returns a string with displaying the given actions
	 * @param action to display
	 * @return A string displaying the action
	 */
	public static String getActionAsString(boolean[] action)
	{
		return "R: " + ((action[Mario.KEY_RIGHT]) ? "t" : "f") +
				" \tL: " + ((action[Mario.KEY_LEFT]) ? "t" : "f") +
				" \tJ: " + ((action[Mario.KEY_JUMP]) ? "t" : "f") +
				" \tS: " + ((action[Mario.KEY_SPEED]) ? "t" : "f") +
				"\t";
	}

	/**
	 * Print out the data about the node
	 * @param node to output data about
	 */
	private static void printNodeData(Node node) {
		System.out.printf("X: %.2f\t", node.x);
		System.out.printf("Y: %.2f\t", node.y);
		System.out.printf("Vx: %.2f\t", node.mario.xa);
		System.out.printf("Vy: %.2f\t", node.mario.ya);
		System.out.printf("Ay: %.2f\t", node.mario.yaa);
		System.out.print(getActionAsString(node.action));
		System.out.printf("Depth: %3d ", node.depth);
		System.out.printf("F: %.3f\t", node.fitness);
		System.out.printf("g: %.3f\t", Node.getDistanceTraveled(node));
		System.out.printf("h: %.3f\t", Node.getHeuristic(node));
		System.out.println();
	}
}
