package astar.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import astar.*;
import astar.level.SpriteTemplate;
import astar.sprites.Mario;
import astar.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;
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

		LevelScene scene = new LevelScene();
		MarioAIOptions AiOptions = new MarioAIOptions(
				"-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -ag astar.AstarAgent");
		BasicTask bt = new BasicTask(AiOptions);
		bt.setOptionsAndReset(AiOptions);
		bt.runSingleEpisode(1);
		// System.out.println(bt.getEnvironment().getEvaluationInfoAsString());
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
	 * 
	 */
	@Test
	public void testRightAndSpeed() {

		int playThroughs = 3;
		int[] times = new int[playThroughs];
		int[] fitnessScores = new int[playThroughs];

		for (int i = 0; i < playThroughs; i++) {

			MarioAIOptions AiOptions = new MarioAIOptions(
					"-vis off -lf on -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -ag astar.AstarAgent");
			BasicTask bt = new BasicTask(AiOptions);

			bt.setOptionsAndReset(AiOptions);
			bt.runSingleEpisode(1);

			EvaluationInfo evalInfo = bt.getEnvironment().getEvaluationInfo();

			times[i] = evalInfo.timeLeft;
			fitnessScores[i] = evalInfo.computeWeightedFitness();

		}
		// check time left and print result

		// System.out.print("Time left in each playthrough: ");
		for (int i = 0; i < playThroughs; i++) {
			// System.out.print(times[i]+", ");
			assertEquals(true, times[0] == times[i]);
		}

	}

	/**
	 * Test method for
	 * {@link astar.Node#Node(astar.Node, astar.LevelScene, astar.sprites.Mario, java.util.List, boolean[])}
	 * .
	 */
	@Test
	public void testNode() {
		Node node = new Node(scene, null);
		Assert.assertNotNull(node);
	}

	/**
	 * Test method for {@link astar.Node#fitnessEvaluation()}.
	 */
	@Test
	public void testFitnessEval() {
		Graph graph = new Graph();
		Node head = new Node(scene, null);
		graph.setHead(head);
		graph.setGoal(200);

		// Setup test object
		Node node = new Node(scene, Node.createAction(false, false, false, false));
		node.mario.x = 100;

		node.fitnessEvaluation(graph.goal, graph.head.x);
		Assert.assertTrue(node.fitness > 0);
	}

	/**
	 * Test method for {@link astar.Node#generateNewNodes()}.
	 */
	@Test
	public void testGenerateNewNodes() {
		Graph graph = new Graph();
		PriorityQueue<Node> queue = graph.queue;
		Node node = new Node(scene, Node.createAction(true, true, true, true));
		graph.setHead(node);

		node.generateNodes(queue, 1000, 0);
		Assert.assertEquals(10, queue.size());
	}

	/**
	 * Test method for
	 * {@link astar.Node#createAction(boolean, boolean, boolean, boolean, boolean, boolean)}
	 * .
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
		Node lowest = new Node(scene, null);
		Node highest = new Node(scene, null);

		lowest.fitness = 10;
		highest.fitness = Float.MAX_VALUE;
		Assert.assertTrue(lowest.compareTo(highest) == -1);
	}
	/**

	 * Test method for {@link astar.Node#getActionAsString(boolean[])}.
	 */
	@Test
	public void testPrintAction() {
		boolean[] action = new boolean[Environment.numberOfKeys];
		assertEquals("R: f \tL: f \tJ: f \tS: f\t", Node.getActionAsString(action));

		for (int i = 0; i < action.length; i++) {
			action[i] = true;
		}
		assertEquals("R: t \tL: t \tJ: t \tS: t\t", Node.getActionAsString(action));
	}

	/**
	 * Test method for {@link astar.Node#testDistanceTraveled()}.
	 */
	@Test
	public void testDistanceTraveled() {
		Graph graph = new Graph();
		Node node = new Node(scene, null);
		graph.setHead(node);

		float distTraveled = node.getDistanceTraveled(graph.head.x);

		Assert.assertEquals(0.0f, distTraveled);
	}

	/**
	 * Test method for {@link astar.Node#testDistanceTraveled()}.
	 */
	@Test
	public void testDistanceTraveledWithDistance() {
		Graph graph = new Graph();
		Node node = new Node(scene, null);
		Node headNode = new Node(scene, null);
		graph.setHead(headNode);
		node.x = 50f;
		headNode.x = 100f;

		float distTraveled = node.getDistanceTraveled(graph.head.x);

		Assert.assertEquals(-50f, distTraveled);
	}

	/**
	 * 
	 */
	@Test
	public void testAiLoading() {
		MarioAIOptions AiOptions = new MarioAIOptions(
				"-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -ag astar.AstarAgent");
		assertNotNull(AiOptions.getAgent());
		assertEquals(AiOptions.getAgent().getName(), "AstarAgent");
		assertEquals(AiOptions.getAgentFullLoadName(), "astar.AstarAgent");

	}

	/**
	 * 
	 */
	@Test
	public void testLevelCompletionNoGapsNoEnemies() {
		MarioAIOptions AiOptions = new MarioAIOptions(
				"-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -ag astar.AstarAgent");
		BasicTask bt = new BasicTask(AiOptions);
		bt.setOptionsAndReset(AiOptions);
		bt.runSingleEpisode(1);
		// System.out.println(bt.getEnvironment().getEvaluationInfoAsString());

		assertEquals(1, Mario.STATUS_WIN);

	}

	/**
	 * 
	 */
	@Test
	public void testDeterminism() {

		int playThroughs = 3;
		int[] times = new int[playThroughs];
		int[] fitnessScores = new int[playThroughs];

		for (int i = 0; i < playThroughs; i++) {

			MarioAIOptions AiOptions = new MarioAIOptions(
					"-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -ag astar.AstarAgent");
			BasicTask bt = new BasicTask(AiOptions);

			bt.setOptionsAndReset(AiOptions);
			bt.runSingleEpisode(1);

			EvaluationInfo evalInfo = bt.getEnvironment().getEvaluationInfo();

			times[i] = evalInfo.timeLeft;
			fitnessScores[i] = evalInfo.computeWeightedFitness();

		}
		// check time left and print result

		// System.out.print("Time left in each playthrough: ");
		for (int i = 0; i < playThroughs; i++) {
			// System.out.print(times[i]+", ");
			assertEquals(times[0], times[i], 10);

			// check weighted fitness and print result

		}
		// System.out.print("\n Fitness Score for each playthrough: ");
		for (int i = 0; i < playThroughs; i++) {
			// System.out.print(fitnessScores[i]+", ");
			assertEquals(fitnessScores[0], fitnessScores[i], 80);
		}

	}

	/**
	 * 
	 */
	@Test
	public void testLevelCompletionWithGapWithoutEnemies() {
		MarioAIOptions AiOptions = new MarioAIOptions(
				"-vis off -lca off -lco off -lb off -le off -ltb off -ls 1 -ld 0 -fps 24 -ag astar.AstarAgent");
		BasicTask bt = new BasicTask(AiOptions);
		bt.setOptionsAndReset(AiOptions);
		
		int wins = 0;
		for (int i = 0; i < 5; i++) {
			bt.runSingleEpisode(1);
			
			EvaluationInfo evalInfo = bt.getEnvironment().getEvaluationInfo();
			wins += evalInfo.marioStatus;
		}

		assertTrue(wins > 0);
	}

	/**
	 * 
	 */
	@Test
	public void testMarioIsDead() {
		Node node = new Node(scene, Node.createAction(true, false, false, false));

		// scene.mario.carried = new Sprite();

		scene.mario.spriteTemplate = new SpriteTemplate(Sprite.KIND_MARIO);
		scene.mario.spriteTemplate.isDead = true;

		// System.out.print(scene.mario.isDead());

		node.fitnessEvaluation(0, 0);

		assertEquals(Float.MAX_VALUE, node.fitness, 0);
	}

	/**
	 * 
	 */
	@Test
	public void testMariosYIsToLarge() {
		Node node = new Node(scene, Node.createAction(true, false, false, false));

		scene.mario.y = 224f;

		node.fitnessEvaluation(0, 0);

		assertEquals(Float.MAX_VALUE, node.fitness, 0);
	}

	/**
	 * 
	 */
	@Test
	public void testCoalitionBetweenXandFitness() {

		// generate a random graph of nodes.
		Graph graph = new Graph();
		Node topNode = new Node(scene, Node.createAction(false, false, false, false));
		topNode.depth = 0;
		int graphDepth = 8;
		int numbOfParents = 1;
		Random rand = new Random();

		List<Node> nodeList = new ArrayList<Node>();
		Node[] parentNodes = new Node[10];
		graph.setGoal(100000f);

		for (int i = 0; i < graphDepth; i++) {

			int numOfNodes = rand.nextInt(10) + 1;

			Node[] currentNodes = new Node[numOfNodes];

			for (int j = 0; j < numOfNodes; j++) {

				Node node = new Node(scene, Node.createAction(false, false, false, false));
				float randomXCoord = rand.nextFloat() * numOfNodes + 1;
				int randParent = rand.nextInt(numbOfParents);

				node.mario.y = 20f;
				node.mario.x = randomXCoord;

				// eval fitness
				node.fitnessEvaluation(graph.goal, 0);

				// setting the parent
				if (i == 0) {
					node.parent = topNode;
				} else {
					node.parent = parentNodes[randParent];
				}

				// adding to array.
				currentNodes[j] = node;
				nodeList.add(node);

			}

			// Set current nodes to be parent nodes

			parentNodes = currentNodes;
			numbOfParents = numOfNodes;
		}

		Node goalNode = new Node(scene, Node.createAction(false, false, false, false));
		goalNode.depth = graphDepth;

		// order nodes after decending fitness score.
		Collections.sort(nodeList);

		// assert if the coalition between x and fitness holds
		for (int i = 1; i < nodeList.size(); i++) {
			Node prevNode = nodeList.get(i - 1);
			Node curNode = nodeList.get(i);
			// System.out.println("X-coordinate: " + prevNode.x + " >= " +
			// curNode.x + " Fitness: " + prevNode.fitness + " <= " +
			// curNode.fitness);
			if (curNode.fitness != Float.MAX_VALUE) {
				assertTrue(prevNode.x >= curNode.x);
				assertTrue(prevNode.fitness <= curNode.fitness);
			}
		}

	}

	/**
	 * 
	 */
	@Test
	public void testLargerSpeedBetterFitness() {
		Node walkingNode = new Node((LevelScene) scene.clone(), Node.createAction(true, false, false, false));
		Node runningNode = new Node((LevelScene) scene.clone(), Node.createAction(true, false, false, true));

		walkingNode.mario.x = 500f;
		walkingNode.mario.xa = 1f;
		walkingNode.mario.y = 0f;

		runningNode.mario.x = 500f;
		runningNode.mario.xa = Node.maxSpeed;
		runningNode.mario.y = 0f;

		walkingNode.fitnessEvaluation(1000, 0);
		runningNode.fitnessEvaluation(1000, 0);

		assertFalse(scene.mario.isDead());		
		assertFalse(scene.isInGap(walkingNode));
		assertFalse(scene.isInGap(runningNode));
		
		assertTrue(walkingNode.fitness > runningNode.fitness);
	}

	/**
	 * 
	 */
	@Test
	public void testJumpWhileFalling() {
		Node parentNode = new Node(scene, Node.createAction(true, false, false, true));
		Node currentNode = new Node(scene, Node.createAction(true, false, false, true));

		currentNode.parent = parentNode;

		parentNode.mario.x = 200f;
		parentNode.y = 20f;

		currentNode.mario.x = 203f;
		currentNode.y = 21f;

		assertFalse(currentNode.canJump());

	}

	/**
	 * 
	 */
	@Test
	public void testJumpWhileJumping() {
		Node parentNode = new Node(scene, Node.createAction(true, false, false, true));
		Node currentNode = new Node(scene, Node.createAction(true, false, false, true));

		currentNode.parent = parentNode;

		parentNode.mario.x = 200f;
		parentNode.y = 20f;

		currentNode.mario.x = 203f;
		currentNode.y = 19f;

		System.out.println(currentNode.canJump());
		assertTrue(currentNode.canJump());

	}

	/**
	 * 
	 */
	@Test
	public void testJumpWhileOnGroundHoldingJump() {
		Graph graph = new Graph();
		Node parentNode = new Node(scene, Node.createAction(true, false, true, true));
		Node currentNode = new Node(scene, Node.createAction(true, false, false, true));

		currentNode.parent = parentNode;

		parentNode.mario.x = 200f;
		parentNode.y = 20f;

		currentNode.mario.x = 203f;
		currentNode.y = 20f;

		assertFalse(currentNode.canJump());
	}

	/**
	 * 
	 */
	@Test
	public void testSimulationGridCenterAndSize() {
		//
		MarioAIOptions AiOptions = new MarioAIOptions("-vis on -ls 22 -fps 24 -ag astar.AstarAgent");
		MarioEnvironment environment = MarioEnvironment.getInstance();

		environment.reset(AiOptions);

		AstarAgent simulationObject = new AstarAgent();
		simulationObject.integrateObservation(environment);
		simulationObject.reset();
		simulationObject.getAction();

		// check size of grid in simulation
		assertEquals(19, simulationObject.receptiveFieldHeight);
		assertEquals(19, simulationObject.receptiveFieldWidth);

		// center position of the enviroment, and it should be the middle of the
		// grid in the simulation.
		int[] marioPos = environment.getMarioEgoPos();
		assertEquals(9, marioPos[0], 0);
		assertEquals(9, marioPos[1], 0);
	}

	/**
	 * 
	 */
	@Test
	public void testMarioCoordinateInGameAndSimulation() {

		Random rand = new Random();
		int x = rand.nextInt(1000);
		String options = "-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -miy 200 -tl 1" + " -mix "
				+ x;
		MarioAIOptions AiOptionsForGame = new MarioAIOptions(options);
		BasicTask btForGame = new BasicTask(AiOptionsForGame);

		MarioEnvironment environmentForGame = MarioEnvironment.getInstance();
		MarioEnvironment environmentForSim = MarioEnvironment.getInstance();

		int[] levelSceneFromGame = environmentForGame.getSerializedLevelSceneObservationZ(1);

		scene.reset(AiOptionsForGame);
		AstarAgent simulationObject = new AstarAgent();
		simulationObject.integrateObservation(environmentForGame);
		simulationObject.reset();
		simulationObject.getAction();

		int[] levelSceneFromSim = environmentForSim.getSerializedLevelSceneObservationZ(1);

		for (int i = 0; i < levelSceneFromSim.length; i = i + 19) {
			for (int j = 0; j < 19; j++) {
				assertEquals(levelSceneFromSim[j + i], levelSceneFromGame[j + i]);
			}
		}

	}
}