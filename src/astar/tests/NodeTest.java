package astar.tests;

import static org.junit.Assert.*;

import java.util.PriorityQueue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import astar.*;
import astar.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import junit.framework.Assert;


/**
 * Contains test for the node class
 */
public class NodeTest {
	
	LevelScene scene;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		scene = new LevelScene();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link astar.Node#Node(astar.Node, astar.LevelScene, astar.sprites.Mario, java.util.List, boolean[])}.
	 */
	@Test
	public void testNode() {
		Node node = new Node(scene, scene.mario, null, null);
		Assert.assertNotNull(node);
	}

	/**
	 * Test method for {@link astar.Node#fitnessEval()}.
	 */
	@Test
	public void testFitnessEval() {
		fail("Not yet implemented");
//		Node head = new Node(scene, scene.mario, null, null);
//		Node.setHead(head);
//		Node.setGoal(200);
//		
//		// Setup test object
//		Node node = new Node(scene, scene.mario, null, Node.createAction(false, false, false, false));
//		node.mario.x = 100;
//		
//		node.fitnessEval();
//		Assert.assertTrue(node.fitness > 0);
	}

	/**
	 * Test method for {@link astar.Node#searchForPath()}.
	 */
	@Test
	public void testSearchForPath() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link astar.Node#generateNewNodes()}.
	 */
	@Test
	public void testGenerateNewNodes() {
		Node node = new Node(scene, scene.mario, null, Node.createAction(false, false, false, false));
		PriorityQueue<Node> queue = new PriorityQueue<Node>();
		
		
		Node.generateNodes(node, queue);
		Assert.assertEquals(9, queue.size());
	}

	/**
	 * Test method for {@link astar.Node#createAction(boolean, boolean, boolean, boolean, boolean, boolean)}.
	 */
	@Test
	public void testCreateAction() {
		boolean[] action = Node.createAction(true, false, true, true);
		Assert.assertTrue(action[Mario.KEY_RIGHT]);
		Assert.assertTrue(action[Mario.KEY_JUMP]);
		Assert.assertTrue(action[Mario.KEY_SPEED]);
		Assert.assertFalse(action[Mario.KEY_LEFT]);
	}

	/**
	 * Test method for {@link astar.Node#compareTo(astar.Node)}.
	 */
	@Test
	public void testCompareTo() {
		Node lowest = new Node(scene, scene.mario, null, null);
		Node highest = new Node(scene, scene.mario, null, null);
		
		lowest.fitness = 10;
		highest.fitness = Float.MAX_VALUE;
		Assert.assertTrue(lowest.compareTo(highest) == -1);
	}

	/**
	 * Test method for {@link astar.Node#atGoal()}.
	 */
	@Test
	public void testAtGoal() {
		Node.setGoal(200);
		Node node = new Node(scene, scene.mario, null, null);
		node.x = 200;
		
		Assert.assertTrue(node.atGoal());
	}

	/**
	 * Test method for {@link astar.Node#setGoal(float)}.
	 */
	@Test
	public void testSetGoal() {
		float goal = 1000f;
		Node.setGoal(goal);
		
		assertEquals(goal, Node.goal, 0);
	}

	/**
	 * Test method for {@link astar.Node#printAction(boolean[])}.
	 */
	@Test
	public void testPrintAction() {
		boolean[] action = new boolean[Environment.numberOfKeys];
		assertEquals("R: f \tL: f \tJ: f \tS: f", Node.printAction(action));
	
		for (int i = 0; i < action.length; i++) {
			action[i] = true;
		}
		assertEquals("R: t \tL: t \tJ: t \tS: t", Node.printAction(action));
	}

	/**
	 * Test method for {@link astar.Node#setStartTime(long)}.
	 */
	@Test
	public void testSetStartTime() {
		long time = 10000;
		Node.setStartTime(time);
		assertEquals(time, Node.getStartTime());
	}

	/**
	 * Test method for {@link astar.Node#getStartTime()}
	 */
	@Test
	public void testGetStartTime() {
		long time = System.currentTimeMillis();
		Node.setStartTime(time);
		assertEquals(time, Node.getStartTime());
	}
	
	/**
	 * Test method for {@link astar.Node#setHead(astar.Node)}.
	 */
	@Test
	public void testSetHead() {
		Node head = new Node(scene, scene.mario, null, null);
		Node.setHead(head);
		
		Assert.assertNotNull(Node.head);
	}

	/**
	 * Test method for {@link astar.Node#getActionPath(astar.Node)}.
	 */
	@Test
	public void testGetActionPath() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link astar.Node#searchChildren()}.
	 */
	@Test
	public void testSearchChildren() {
		fail("Not yet implemented");
	}
}
