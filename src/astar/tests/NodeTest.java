package astar.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import astar.*;
import ch.idsia.benchmark.mario.environments.Environment;


/**
 * Contains test for the node class
 */
public class NodeTest {

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
		fail("Not yet implemented");
//		Node node = new Node(null, null, null, null, null);
	}

	/**
	 * Test method for {@link astar.Node#fitnessEval()}.
	 */
	@Test
	public void testFitnessEval() {
		fail("Not yet implemented");
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
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link astar.Node#createAction(boolean, boolean, boolean, boolean, boolean, boolean)}.
	 */
	@Test
	public void testCreateAction() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link astar.Node#compareTo(astar.Node)}.
	 */
	@Test
	public void testCompareTo() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link astar.Node#atGoal()}.
	 */
	@Test
	public void testAtGoal() {
		fail("Not yet implemented");
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
		fail("Not yet implemented");
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
