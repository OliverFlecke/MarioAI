package astar;

import java.util.*;
import astar.sprites.Mario;
import astar.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;

/**
 * The node class represent a frame or state in the Mario world, in which the simulation
 * has executed the action stored in this given node. 
 */
public class Node implements Comparable<Node> {

	private static boolean debug = false;	// True, if the program should output debug data
	
	private static int timeLimit = 33;		// Any larger, and the game seem to lack 
	public static int nodeCount = 0;		// Counter to keep track of the number of generated nodes

	private static long startTime;			// Start time for the current start
	public static float goal = 0;			// The goal which Mario should reach
	public static PriorityQueue<Node> queue;// Queue to store all the nodes that are yet to be explored
	
	// Coordinates of the node
	public float x, y;	
	
	private static float alpha = 0.5f;		// Factor to modify the heuristic 
	public float fitness = 0f;				// Overall rating of this option 
	public int depth;						// Depth of the current node
	private boolean[] action;				// Action that are done in this node
	
	// Game elements 
	public Mario mario;						// The Mario object 
	public final static float maxSpeed = 10.9090909f;	// The max speed that Mario can reach
	
	// The level scene used for this simulation
	public LevelScene levelScene;
	
	// Graph pointers 
	public Node parent;
	public static Node head;
	public List<Node> children = new ArrayList<Node>();
	private static int maxDepth = 1000;
	
	/**
	 * Create a new node, which should have everything needed to compute next frame
	 * @param parent of the current node
	 * @param levelScene A copy of the level sceneS
	 * @param action which Mario should take in this simulation
	 */
	public Node(Node parent, LevelScene levelScene, boolean[] action) 
	{
		// Increment the node count in order to keep track of the number nodes generated
		Node.nodeCount++;

		// Set the parent and the depth of the node
		this.parent = parent;
		if (this.parent == null) depth = 0;
		else depth = parent.depth + 1;
		
		// The elements should have been copied before they were passed
		this.levelScene = levelScene;
		this.mario = levelScene.mario; 
			
		this.action = action;
		// Update Mario
		this.mario.keys = action;
		this.x = mario.x;
		this.y = mario.y;
	}

	/**
	 * Create a node without a parent. Everything else is as in a normal node.
	 * @param levelScene
	 * @param mario
	 * @param enemies
	 * @param action
	 */
	public Node(LevelScene levelScene, Mario mario, List<Sprite> enemies, boolean[] action)
	{
		this(null, levelScene, action);
	}
	
	/**
	 * Creates a new node with clones of the level scene and Mario,
	 * as well as the passed enemies and action array. The current
	 * node is passed as the parent node
	 * @param actions the new node should simulate
	 * @param enemies which should be passed into the level scene
	 * @return A node with cloned objects
	 */
	private static Node createNode(Node parent, boolean[] actions, List<Sprite> enemies)
	{
		return new Node(parent, (LevelScene) parent.levelScene.clone(), actions);
	}
	
	/**
	 * Ticked in every frame. This will get Mario and every enemy to move
	 */
	private void tick() 
	{
		this.levelScene.tick();
		this.mario.tick();
	}

	/**
	 *  The function to evaluate the nodes fitness.
	 */
	public void fitnessEvaluation()
	{
		// Evaluate the simulation
		this.tick();
		
		// Error handling for unknown error were Mario get a constant x coordinate
		if (mario.x == 816f)
		{
			mario.x = (this.x + mario.xa * 1.12f);
			mario.xa = mario.x - this.x;
		}
		
		// Update the nodes coordinates
		this.x = mario.x;
		this.y = mario.y;
		
		// If Mario is dead or to low in the level, the path is dead
		if (mario.isDead() || this.y > 223f) 
		{
			this.fitness = Float.MAX_VALUE;
		}
		else if (checkCollision())
		{
			this.fitness = Float.MAX_VALUE;
		}
		else if (levelScene.isInGap(this))
			this.fitness = Float.MAX_VALUE;
		else 
		{
			this.fitness = getHeuristic(this);
		}
	}

	/**
	 * Get the heuristic for a given node
	 * @param node to get the heuristic value from
	 * @return A float, representing 
	 */
	public static float getHeuristic(Node node)
	{
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
			System.out.println("Depth: " + best.depth + " Fitness: " + best.fitness);
		
		return getActionPath(best);
	}
	
	/**
	 * Generate all the new nodes which Mario can move to from this.
	 * This method should take into account what options Mario have at a given moment. 
	 * That means that jump nodes are not generated when Mario do not have the option of jumping
	 */
	public static void generateNodes(Node current, PriorityQueue<Node> queue)
	{
		// Compute all the new positions for the enemies
		List<Sprite> newEnemies = new ArrayList<Sprite>();
		
		// Create the different action options and place them in this list
		List<boolean[]> options = new ArrayList<boolean[]>();
		
		// Only created if it is possible to go right, or if Mario is in the air
		if (current.parent == null || (Math.abs(current.x - current.parent.x) > 0.7) 
				|| (current.y != current.parent.y))
		{
			options.add(createAction(false, false, false, false));	// Do nothing

			// Right movement
			options.add(createAction(true, false, false, false));	// Move right
			options.add(createAction(true, false, false, true));	// Move right and speed

			// Left movement
			options.add(createAction(false, true, false, false));	// Move left 
			options.add(createAction(false, true, false, true));	// Move left and speed
		}

		// Check if pressing the jump key makes a differers, and generate nodes if it does
		if (current.canJump())
		{
			options.add(createAction(false, false, true, false));	// Jump
			options.add(createAction(true, false, true, false));	// Jump and move right
			options.add(createAction(true, false, true, true));		// Jump, move right, and speed
			
			// Left, jump movement
			options.add(createAction(false, true, true, false));	// Jump, move left
			options.add(createAction(false, true, true, true));		// Jump, move left, and speed
		}
		
		// Create a node from all the options
		for (boolean[] action : options)
		{
			Node node = createNode(current, action, newEnemies);
			node.fitnessEvaluation();
			queue.add(node);
		}
	}
	
	/**
	 * Computes if Mario is able to jump.
	 * He is able to jump, if Mario is already jumping, that is going upwards.
	 * If he is on the ground, Mario can only jump if he did not hold the down
	 * the jump button in the parent node. If the parent node was holding down
	 * the button while on the ground, he is not able to jump in the current 
	 * state. If Mario is falling, he is not able to jump either. 
	 * @return True, if Mario is able to jump
	 */
	public boolean canJump() 
	{
		if (parent != null)
			if (this.y == parent.y)	// If Mario is on the ground
				if (parent.getAction()[Mario.KEY_JUMP])
					return false;
				else 
					return true;
			else if (this.y < parent.y) // Mario is going upwards
				return true;
			else if (this.y > parent.y) // Mario is going downwards
				return false;
		return true;
	}

	/**
	 * Create an action array with the given actions, which should
	 * get Mario to moved in the given direction
	 * @param right True, if Mario should move to the right
	 * @param left True, if  Mario should move to the left 
	 * @param jump True, if Mario should jump
	 * @param speed True, if Mario should run or fire a fireball
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
	
	/**
	 * Get a path of actions to the node from the head. This is done recursively
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
		list.add(node.getAction());
		
		// Output debug information
		if (debug) printNodeData(node);
		return list;
	}
	
	/**
	 * Get the action from the current node, e.g. the action that Mario is taken in the current
	 * step. This is stored in Mario's keys field.
	 * @return The action of this node
	 */
	public boolean[] getAction() 
	{
//		return this.mario.keys;
		return this.action;
	}

	@Override
	/**
     * Compare this node to another. Uses the fitness as primary compare tool, then
     * the x coordinate and lastly the y coordinate.
     * @return -1, if the other node's fitness is larger than this nodes fitness. 
     * 0 is returned if the have the same fitness, and 1 is returned if this node has 
     * the larger fitness.
	 */
	public int compareTo(Node other) {
		if (other.fitness - this.fitness > 0)
			return -1;
		else if (other.fitness - this.fitness < 0)
			return 1;
		else 
		{
			// Look at the x coordinate
			if ((other.x - this.x) > 0)
				return 1;
			else if ((other.x - this.x) < 0)
				return -1;
			else 
			{
				// Look at the y coordinate
				if ((other.y - this.y) > 0)
					return -1;
				else if ((other.y - this.y) < 0)
					return 1;
			}
		}
		return 0;
	}

	/**
	 * Check the collision between Mario and the enemies in the level scene
	 * @return True, if Mario has collided with any enemy
	 */
	public boolean checkCollision() 
	{
//		for (Sprite sprite : levelScene.sprites)
//		{
//			if (sprite.kind != Sprite.KIND_MARIO)
//			{
//				return Math.abs(mario.x - sprite.x) < 32 && Math.abs(mario.y - sprite.y) < 16;
//			}
//		}
//		return false;
//		for (int i = 0; i < levelScene.enemies.length; i += 3)
//		{
//			x = levelScene.enemies[i + 1];
//			y = levelScene.enemies[i + 2];
//			
//			if (Math.abs(x - getX()) < 64 && Math.abs(y - getY()) < 64)
//				return true;
//		}
		return false;
	}

	/**
	 * Get the x coordinate of this node
	 * @return The x coordinate of this node
	 */
	public float getX() {
		return mario.x;
	}
	
	/**
	 * Get the y coordinate of this node
	 * @return The x coordinate of this node
	 */
	public float getY() {
		return mario.y;
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
	 * Set the goal for the current search
	 * @param goal which Mario should aim for 
	 */
	public static void setGoal(float goal)
	{
		Node.goal = goal;
	}
	
	/**
	 * Set the start time of the current search
	 * @param currentTimeMillis the time which the search started at 
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
	 * @param head The starting point of the graph
	 */
	public static void setHead(Node head) {
		Node.head = head;
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
		System.out.print(getActionAsString(node.getAction()));
		System.out.printf("Depth: %3d ", node.depth);
		System.out.printf("F: %.3f\t", node.fitness);
		System.out.printf("g: %.3f\t", Node.getDistanceTraveled(node));
		System.out.printf("h: %.3f\t", Node.getHeuristic(node));
		System.out.println();
	}
}
