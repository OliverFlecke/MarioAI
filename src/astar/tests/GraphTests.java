package astar.tests;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import astar.Graph;
import astar.LevelScene;
import astar.Node;
import astar.sprites.Mario;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;
import junit.framework.Assert;

public class GraphTests {

	static LevelScene scene;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		scene = new LevelScene();
		MarioAIOptions AiOptions = new MarioAIOptions(
				"-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -ag astar.AstarAgent");
		BasicTask bt = new BasicTask(AiOptions);
		bt.setOptionsAndReset(AiOptions);
		bt.runSingleEpisode(1);
		// System.out.println(bt.getEnvironment().getEvaluationInfoAsString());
	}
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	

	/**
	 * Test method for {@link astar.Node#searchForPath()}.
	 */
	@Test
	public void testSearchForPath() {
		Graph graph = new Graph();
		Node node = new Node(scene, Node.createAction(false, false, false, false));
		
		graph.setGoal(1000);

		LinkedList<boolean[]> listOfActions = graph.searchForPath(node);

		assertTrue(0 != graph.nodeCount);
		assertNotNull(listOfActions);
	}
	

	/**
	 * Test method for {@link astar.Node#atGoal()}.
	 */
	@Test
	public void testAtGoal() {
		Graph graph = new Graph();
		graph.setGoal(200);
		Node node = new Node(scene, null);
		node.x = 200;

		Assert.assertTrue(graph.atGoal(node));
	}
	
	/**
	 * Test method for {@link astar.Node#setGoal(float)}.
	 */
	@Test
	public void testSetGoal() {
		Graph graph = new Graph();
		float goal = 1000f;
		graph.setGoal(goal);

		assertEquals(goal, graph.goal, 0);
	}


	/**
	 * Test method for {@link astar.Node#setStartTime(long)}.
	 */
	@Test
	public void testSetStartTime() {
		Graph graph = new Graph();
		long time = 10000;
		graph.setStartTime(time);
		assertEquals(time, graph.getStartTime());
	}

	/**
	 * Test method for {@link astar.Node#getStartTime()}
	 */
	@Test
	public void testGetStartTime() {
		Graph graph = new Graph();
		long time = System.currentTimeMillis();
		graph.setStartTime(time);
		assertEquals(time, graph.getStartTime());
	}

	/**
	 * Test method for {@link astar.Node#setHead(astar.Node)}.
	 */
	@Test
	public void testSetHead() {
		Graph graph = new Graph();
		Node head = new Node(scene, null);
		graph.setHead(head);

		Assert.assertNotNull(graph.head);
	}

	/**
	 * Test method for {@link astar.Node#getActionPath(astar.Node)}.
	 */
	@Test
	public void testGetActionPath() {
		Node headNode = new Node(scene, Node.createAction(true, false, false, false));
		Node currentNode = new Node(scene, Node.createAction(false, true, false, false));
		Node middleNode = new Node(scene, Node.createAction(false, false, true, false));
		Node goalNode = new Node(scene, Node.createAction(false, false, false, true));

		headNode.depth = 0;
		currentNode.depth = 1;
		middleNode.depth = 2;
		goalNode.depth = 3;

		currentNode.parent = headNode;
		middleNode.parent = currentNode;
		goalNode.parent = middleNode;

		LinkedList<boolean[]> listOfActions = Graph.getActionPath(goalNode);

		assertEquals(3, listOfActions.size());

		boolean[] firstAction = listOfActions.remove();
		assertEquals(firstAction[Mario.KEY_RIGHT], false);
		assertEquals(firstAction[Mario.KEY_LEFT], true);
		assertEquals(firstAction[Mario.KEY_JUMP], false);
		assertEquals(firstAction[Mario.KEY_SPEED], false);

		boolean[] secondAction = listOfActions.remove();
		assertEquals(secondAction[Mario.KEY_RIGHT], false);
		assertEquals(secondAction[Mario.KEY_LEFT], false);
		assertEquals(secondAction[Mario.KEY_JUMP], true);
		assertEquals(secondAction[Mario.KEY_SPEED], false);

		boolean[] thirdAction = listOfActions.remove();
		assertEquals(thirdAction[Mario.KEY_RIGHT], false);
		assertEquals(thirdAction[Mario.KEY_LEFT], false);
		assertEquals(thirdAction[Mario.KEY_JUMP], false);
		assertEquals(thirdAction[Mario.KEY_SPEED], true);

	}

}
