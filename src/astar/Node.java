package astar;

import java.util.*;
import astar.sprites.Mario;
import astar.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;

public class Node implements Comparator<Node>, Comparable<Node> {
	
	private static int timeLimit = 40;
	public static int nodeCount = 0;
	private static float alpha = 1f;
	public static float goal = 0;
	public static PriorityQueue<Node> queue;
	private static long startTime;
	
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
		if (this.parent == null)
		{
			this.depth = 0;
			Mario.xSimHead = mario.x;
			Mario.ySimHead = mario.y;
		}
		else
		{			
			this.depth = this.parent.depth + 1;
		}
		if (head == null) this.head = this;
		else this.head = head;
		
		// Copy these elements, don't just save the pointers 
		this.mario = (Mario) mario.clone(); 
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
	 *  The function to evaluate the current frame
	 */
	public void fitnessEval()
	{
		// Evalf the simulation
		this.tick();
		
		float g = Math.abs(this.x - this.head.x);
		float toGoal = Math.abs(alpha * (goal - this.x));
		float h = 0;
		if (mario.isDead()) 
		{
			h = Integer.MAX_VALUE;
		}
		else 
		{
			h = toGoal;
//			h = toGoal + this.y;
		}
		this.fitness = (int) (g + h);
		
		System.out.println();
		System.out.println("X: " + this.x + " \tY: " + this.y);
		System.out.println("F: " + fitness + " g " + g + " h: " + h);
//		printAction(action);
	}
	
	/**
	 * 
	 */
	private void tick() {
//		this.levelScene.tick();
		this.mario.tick();
//		System.out.println("Mario ya: " + mario.yAcc);
//		System.out.println("Mario jump: " + (parent.mario.y - mario.y));
//
//		if (mario.isOnGround())
//			System.out.println("Mario is on the ground");
//		if (mario.mayJump())
//			System.out.println("Can jump!");
		if (mario.mayJump() || (!mario.isOnGround() && action[Mario.KEY_JUMP]))
			marioCanJump = true;
		else marioCanJump = false;
//		else if (mario.ya < parent.mario.ya)
//			marioCanJump = true;
//		else if (mario.ya >= -2 && mario.ya < 0)
//			marioCanJump = false;
	}
	
	private boolean marioCanJump = false;
	public int jumpTime = 0;

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
	 * 
	 * @return
	 */
	public LinkedList<boolean[]> searchForPath()
	{
		queue = new PriorityQueue<Node>();
		generateNewNodes();
		// Chose to use this, if we find a solution, but want to continue our search
		Node current = queue.remove();
		Node best = current; 
		
		while ((!current.atGoal() && current.depth < 40))
		{
			if ((System.currentTimeMillis() - startTime) > timeLimit)
			{
				break;
			}
			current.generateNewNodes();
			current = queue.remove();
			
//			System.out.println(current.fitness);
			
			// Update the best node
			if (best.fitness >= current.fitness)
				best = current;
		}
		
//		System.out.println("Depth: " + best.depth);
		
//		System.out.println(Node.nodeCount);
		Node.nodeCount = 0;
		return best.getActionPath();
	}
	
	/**
	 * Generate all the new nodes which Mario can move to from this
	 * 
	 * Should maybe take a queue and insert the new nodes 
	 */
	public void generateNewNodes()
	{
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
//		node = createNode(Node.createAction(false, false, false, false, false, false), newEnemies);
//		node.fitnessEval();
//		queue.add(node);
		
		// Action: Move right
		node = createNode(Node.createAction(true, false, false, false, false, false), newEnemies);
		node.fitnessEval();
		queue.add(node);
		// Action: Move right with speed
		node = createNode(Node.createAction(true, false, false, true, false, false), newEnemies);
		node.fitnessEval();
		queue.add(node);
	
//		// Action: Move left
//		node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, true, false, false, false, false));
//		node.fitnessEval();
//		queue.add(node);
//		// Action: Move left with speed
//		node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, true, false, true, false, false));
//		node.fitnessEval();
//		queue.add(node);
	
		// Check if pressing the jump key makes a differers, and generate nodes if it does
		if (this.marioCanJump)
		{
			// Action: Jump
//			node = createNode(Node.createAction(false, false, true, false, false, false), newEnemies);
//			node.fitnessEval();
//			queue.add(node);
			// Action: Jump and move right
			node = createNode(Node.createAction(true, false, true, false, false, false), newEnemies);
			node.fitnessEval();
			queue.add(node);
			// Action: Jump, right and speed
			node = createNode(Node.createAction(true, false, true, true, false, false), newEnemies);
			node.fitnessEval();
			queue.add(node);
			
//			// Action: Jump and left
//			node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, true, true, false, false, false));
//			node.fitnessEval();
//			queue.add(node);
//			// Action: Jump, left and speed
//			node = new Node(this, this.head, newScene, newMario, newEnemies, Node.createAction(false, true, true, true, false, false));
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
	
	private Node createNode(boolean[] actions, List<Sprite> enemies)
	{
		return new Node(this, head, levelScene, mario, enemies, actions);
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
	
	@Override
	public int compareTo(Node other) {
		return (this.fitness - other.fitness);
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
	
	public static void printAction(boolean[] action)
	{
		System.out.println("Right: " + action[Mario.KEY_RIGHT]);
		System.out.println("Left:  " + action[Mario.KEY_LEFT]);
		System.out.println("Jump:  " + action[Mario.KEY_JUMP]);
		System.out.println("Speed: " + action[Mario.KEY_SPEED]);
	}

	/**
	 * Set the start time of the current search
	 * @param currentTimeMillis
	 */
	public static void setStartTime(long currentTimeMillis) {
		Node.startTime  = currentTimeMillis;
	}

}