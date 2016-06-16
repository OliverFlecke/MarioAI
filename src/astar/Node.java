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
	// Coordinates of the node
	public float x, y;	
	// Graph pointers 
	public Node parent;
	private Graph graph;
	
	private static float alpha = 0.5f;		// Factor to modify the heuristic 
	public float fitness = 0f;				// Overall rating of this option 
	public int depth;						// Depth of the current node
	private boolean[] action;				// Action that are done in this node
	
	// Game elements 
	public Mario mario;						// The Mario object 
	public final static float maxSpeed = 10.9090909f;	// The max speed that Mario can reach
	
	// The level scene used for this simulation
	public LevelScene levelScene;
	
	
	/**
	 * Create a new node, which should have everything needed to compute next frame
	 * @param parent of the current node
	 * @param levelScene A copy of the level sceneS
	 * @param action which Mario should take in this simulation
	 */
	public Node(Graph graph, Node parent, LevelScene levelScene, boolean[] action) 
	{
		this.graph = graph;
		graph.nodeCount++;
		
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
	 * Create a node without a parent. Everything else is in a normal node
	 * @param levelScene used for the simulation
	 * @param mario which is used in the simulation
	 * @param enemies in the simulation
	 * @param action which is simulated in this node
	 */
	public Node(Graph graph, LevelScene levelScene, boolean[] action)
	{
		this(graph, null, levelScene, action);
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
		return new Node(parent.graph, parent, (LevelScene) parent.levelScene.clone(), actions);
	}
	
	/**
	 * Ticked in every frame. This will get Mario and every enemy to move
	 */
	private void tick() 
	{
//		this.levelScene.tick();
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
		
//		// If Mario is dead or to low in the level, the path is dead
		if (mario.isDead() || this.y > 223f) 
		{
			this.fitness = Float.MAX_VALUE;
		}
		else if (checkCollision())
		{
			this.fitness = Float.MAX_VALUE;

		}
		else if (levelScene.isInGap(this))
		{
			this.fitness = Float.MAX_VALUE;
		}
		else 
		{
			this.fitness = getHeuristic();
		}
	}
	
	
	/**
	 * Get the heuristic for a given node
	 * @param node to get the heuristic value from
	 * @return A float, representing 
	 */
	public float getHeuristic()
	{
		return alpha * (graph.goal - this.x) 
				+ (Node.maxSpeed - this.mario.xa); 
	}
	
	/**
	 * Get the distance which the passed node has already traveled
	 * @param node to get the distance from 
	 * @return The distance with the given node is from the starting point
	 */
	public float getDistanceTraveled()
	{
		return (this.x - graph.head.x);
	}
	
	
	/**
	 * Generate all the new nodes which Mario can move to from this.
	 * This method should take into account what options Mario have at a given moment. 
	 * That means that jump nodes are not generated when Mario do not have the option of jumping
	 * @param current The node which there should be generated new (child) nodes from 
	 * @param queue The queue to add the nodes to
	 */
	public void generateNodes(PriorityQueue<Node> queue)
	{
		// Compute all the new positions for the enemies
		List<Sprite> newEnemies = new ArrayList<Sprite>();
		
		// Create the different action options and place them in this list
		List<boolean[]> options = new ArrayList<boolean[]>();
		
		// Only created if it is possible to go right, or if Mario is in the air
		if (this.parent == null || (Math.abs(this.x - this.parent.x) > 0.7) 
				|| (this.y != this.parent.y))
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
		if (this.canJump())
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
			Node node = createNode(this, action, newEnemies);
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
	public static void printNodeData(Node node) {
		System.out.printf("X: %.2f\t", node.x);
		System.out.printf("Y: %.2f\t", node.y);
		System.out.printf("Vx: %.2f\t", node.mario.xa);
		System.out.printf("Vy: %.2f\t", node.mario.ya);
		System.out.printf("Ay: %.2f\t", node.mario.yaa);
		System.out.print(getActionAsString(node.getAction()));
		System.out.printf("Depth: %3d ", node.depth);
		System.out.printf("F: %.3f\t", node.fitness);
		System.out.printf("g: %.3f\t", node.getDistanceTraveled());
		System.out.printf("h: %.3f\t", node.getHeuristic());
		System.out.println();
	}
}
