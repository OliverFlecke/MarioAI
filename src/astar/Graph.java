package astar;

import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Contains the search functions to search through nodes for solutions.
 * Uses the Node class fitness to search
 */
public class Graph {
	
	private static boolean debug = false;	// True, if the program should output debug data
	
	private static int maxDepth = 100;		// Max depth for the search to take
	private static int timeLimit = 33;		// Any larger, and the game seem to lack 
	public int nodeCount = 0;		// Counter to keep track of the number of generated nodes

	private long startTime;			// Start time for the current start
	public float goal = 0;			// The goal which Mario should reach
	public PriorityQueue<Node> queue;// Queue to store all the nodes that are yet to be explored
	public Node head;

	public Graph() {
		queue = new PriorityQueue<Node>();
	}
	
	/**
	 * Search for a path from a 
	 * @param head The node to search from
	 * @return A list of action, which contains the optimal path through the world
	 */
	public LinkedList<boolean[]> searchForPath(Node head)
	{
		if (debug) System.out.println("Head: X: " + head.x + " Y: " + head.y + " Goal: " + goal);
		
		nodeCount = 1;
		setHead(head);
		head.generateNodes(queue, goal, head.x);

		// Choose to use this, if we find a solution, but want to continue our search
		Node current = queue.poll();
		Node best = current; 
		
		// Set the start time of the search
		setStartTime(System.currentTimeMillis());
		
		while (current != null && !atGoal(current))
		{			
			nodeCount++;
			if (debug) Node.printNodeData(current);
			
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
			current.generateNodes(queue, goal, head.x);
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
		if (debug) Node.printNodeData(node);
		return list;
	}

	/**
	 * Test to see if this node is at the goal
	 * @return True, if this node is at the goal line
	 */
	public boolean atGoal(Node node)
	{
		return node.x >= goal;
	}
	
	/**
	 * Set the goal for the current search
	 * @param goal which Mario should aim for 
	 */
	public void setGoal(float goal)
	{
		this.goal = goal;
	}
	
	/**
	 * Set the start time of the current search
	 * @param currentTimeMillis the time which the search started at 
	 */
	public void setStartTime(long currentTimeMillis) {
		this.startTime  = currentTimeMillis;
	}
	
	/**
	 * Get the start time of the latest search
	 * @return The start time of the latest search
	 */
	public long getStartTime() {
		return this.startTime;
	}

	/**
	 * Set the head/starting point of the graph.
	 * @param head The starting point of the graph
	 */
	public void setHead(Node head) {
		this.head = head;
	}
	
}
