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
		 MarioAIOptions AiOptions = new MarioAIOptions("-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -ag astar.AstarAgent");
		 BasicTask bt = new BasicTask(AiOptions);
		bt.setOptionsAndReset(AiOptions);
		bt.runSingleEpisode(1);
	    //System.out.println(bt.getEnvironment().getEvaluationInfoAsString());
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
	public void testRightAndSpeed(){

	int playThroughs = 3;	
	int[] times = new int[playThroughs];
	int[] fitnessScores = new int[playThroughs];
	
	for (int i=0;i <playThroughs;i++){
		
		 MarioAIOptions AiOptions = new MarioAIOptions("-vis off -lf on -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -ag astar.AstarAgent");
		 BasicTask bt = new BasicTask(AiOptions);
		
		bt.setOptionsAndReset(AiOptions);
		bt.runSingleEpisode(1);
		
		EvaluationInfo evalInfo = bt.getEnvironment().getEvaluationInfo();
		
		times[i]=evalInfo.timeLeft;
		fitnessScores[i] = evalInfo.computeWeightedFitness();
		
		
	}
	//check time left and print result

	//System.out.print("Time left in each playthrough: ");
	for (int i=0; i < playThroughs; i++){
		//System.out.print(times[i]+", ");
		assertEquals(true, times[0]==times[i]);
	}
	
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
	 * Test method for {@link astar.Node#fitnessEvaluation()}.
	 */
	@Test
	public void testFitnessEval() {
		Node head = new Node(scene, scene.mario, null, null);
		Node.setHead(head);
		Node.setGoal(200);
		
		// Setup test object
		Node node = new Node(scene, scene.mario, null, Node.createAction(false, false, false, false));
		node.mario.x = 100;
		
		node.fitnessEvaluation();
		Assert.assertTrue(node.fitness > 0);
	}

	/**
	 * Test method for {@link astar.Node#searchForPath()}.
	 */
	@Test
	public void testSearchForPath() {
		
		Node node = new Node(scene, scene.mario, null, Node.createAction(false, false, false, false));
		PriorityQueue<Node> queue = new PriorityQueue<Node>();
		Node.setHead(node);
		Node.setGoal(1000);
		
		
		LinkedList<boolean[]> listOfActions = Node.searchForPath(Node.head, queue);
		
		assertTrue( 0 != Node.nodeCount);
		assertNotNull(listOfActions);
	}

	/**
	 * Test method for {@link astar.Node#generateNewNodes()}.
	 */
	@Test
	public void testGenerateNewNodes() {	
		PriorityQueue<Node> queue = new PriorityQueue<Node>();
		

		Node node = new Node(scene, scene.mario, null, Node.createAction(true, true, true, true));
		Node.setHead(node);

		
		
		Node.generateNodes(node, queue);
		Assert.assertEquals(10, queue.size());
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
		Node headNode = new Node(scene, scene.mario, null,  Node.createAction(true, false, false, false));
		Node currentNode = new Node(scene, scene.mario, null,  Node.createAction(false, true, false, false));
		Node middleNode = new Node(scene, scene.mario, null, Node.createAction(false, false, true, false));
		Node goalNode = new Node(scene, scene.mario, null,  Node.createAction(false, false, false, true));
		
		headNode.depth = 0;
		currentNode.depth =1;
		middleNode.depth = 2;
		goalNode.depth = 3;
		
		currentNode.parent = headNode;
		middleNode.parent=currentNode;
		goalNode.parent = middleNode;
	
		LinkedList<boolean[]> listOfActions = Node.getActionPath(goalNode);
		
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

	/**
	 * Test method for {@link astar.Node#testDistanceTraveled()}.
	 */
	@Test
	public void testDistanceTraveled() {
		Node node = new Node(scene, scene.mario, null, null);
		Node.setHead(node);
		
		
		float distTraveled = Node.getDistanceTraveled(node);
		
		Assert.assertEquals(0.0f, distTraveled);
	}

	/**
	 * Test method for {@link astar.Node#testDistanceTraveled()}.
	 */
	@Test
	public void testDistanceTraveledWithDistance() {
		Node node = new Node(scene, scene.mario, null, null);
		Node headNode = new Node(scene, scene.mario, null, null);
		Node.setHead(headNode);
		node.x = 50f;
		headNode.x = 100f;
		
		
		
		float distTraveled = Node.getDistanceTraveled(node);
		
		Assert.assertEquals(-50f, distTraveled);
		}
	/**
	 * 
	 */
	@Test
	public void testAiLoading(){
		    MarioAIOptions AiOptions = new MarioAIOptions("-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -ag astar.AstarAgent");
		    assertNotNull(AiOptions.getAgent());
		    assertEquals(AiOptions.getAgent().getName(), "AstarAgent");
		    assertEquals(AiOptions.getAgentFullLoadName(), "astar.AstarAgent");
		    
		}
	/**
	 * 
	 */
	@Test
	public void testLevelCompletionNoGapsNoEnemies() {
			MarioAIOptions AiOptions = new MarioAIOptions("-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -ag astar.AstarAgent");
			BasicTask bt = new BasicTask(AiOptions);
			bt.setOptionsAndReset(AiOptions);
			bt.runSingleEpisode(1);
		    //System.out.println(bt.getEnvironment().getEvaluationInfoAsString());
			
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
		
		for (int i=0;i <playThroughs;i++){
			
			 MarioAIOptions AiOptions = new MarioAIOptions("-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -ag astar.AstarAgent");
			 BasicTask bt = new BasicTask(AiOptions);
			
			bt.setOptionsAndReset(AiOptions);
			bt.runSingleEpisode(1);
			
			EvaluationInfo evalInfo = bt.getEnvironment().getEvaluationInfo();
			
			times[i]=evalInfo.timeLeft;
			fitnessScores[i] = evalInfo.computeWeightedFitness();
			
			
		}
		//check time left and print result
	
		//System.out.print("Time left in each playthrough: ");
		for (int i=0; i < playThroughs; i++){
			//System.out.print(times[i]+", ");
			assertEquals(times[0], times[i], 10);
			
		//check weighted fitness and print result
			
		}
		//System.out.print("\n Fitness Score for each playthrough: ");
		for (int i=0; i < playThroughs; i++){
			//System.out.print(fitnessScores[i]+", ");
			assertEquals(fitnessScores[0], fitnessScores[i],80);
		}
		
	
		
		
		}
	
	/**
	 * 
	 */
	@Test
	public void testLevelCompletionWithGapWithoutEnemies() {
		 MarioAIOptions AiOptions = new MarioAIOptions("-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -ld 5 -fps 24 -ag astar.AstarAgent");
		 BasicTask bt = new BasicTask(AiOptions);
		bt.setOptionsAndReset(AiOptions);
		bt.runSingleEpisode(1);
	
		EvaluationInfo evalInfo = bt.getEnvironment().getEvaluationInfo();
		
		//check time left
		//System.out.print(evalInfo.marioStatus);
		
	    assertEquals(1, evalInfo.marioStatus);
	    
		}
	
	/**
	 * 
	 */
	@Test
	public void testMarioIsDead() {
		
		Node node = new Node(scene, scene.mario, null,  Node.createAction(true, false, false, false));
	
	
		//scene.mario.carried = new Sprite();
		
		scene.mario.spriteTemplate = new SpriteTemplate(Sprite.KIND_MARIO);
		scene.mario.spriteTemplate.isDead = true;
		
		
		//System.out.print(scene.mario.isDead());
		
		node.fitnessEvaluation();
		
		assertEquals(Float.MAX_VALUE, node.fitness, 0);
	}
	
	/**
	 * 
	 */
	@Test
	public void testMariosYIsToLarge() {
		
		Node node = new Node(scene, scene.mario, null, Node.createAction(true, false, false, false));
	
		scene.mario.y=224f;
		
		node.fitnessEvaluation();
		
		assertEquals(Float.MAX_VALUE, node.fitness, 0);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testCoalitionBetweenXandFitness() {
		
		
	//generate a random graph of nodes.
		
		Node topNode = new Node(scene, scene.mario, null,  Node.createAction(false, false, false, false));
		topNode.depth = 0;
		int graphDepth = 8;
		int numbOfParents=1;
		Random rand = new Random();
		
		List<Node> nodeList = new ArrayList<Node>();
		Node[] parentNodes = new Node[10];
		Node.setGoal(100000f);
		
		for (int i=0;i<graphDepth;i++){
			
			
			int numOfNodes = rand.nextInt(10)+1;
			
			
			Node[] currentNodes = new Node[numOfNodes];
			
			for (int j=0; j<numOfNodes;j++){
				
				Node node = new Node(scene, scene.mario, null,  Node.createAction(false, false, false, false));
				float randomXCoord =  rand.nextFloat() * numOfNodes + 1;
				int randParent = rand.nextInt(numbOfParents);
				
				node.mario.y = 20f;
				node.mario.x = randomXCoord;
				
				//eval fitness
				node.fitnessEvaluation();
				
				
				//setting the parent
				if (i==0){
					node.parent = topNode;
				} else {
					node.parent = parentNodes[randParent];
				}
				
				//adding to array.
				currentNodes[j] = node;
				nodeList.add(node);
				
			}
			
			//Set current nodes to be parent nodes
	
			parentNodes = currentNodes;
			numbOfParents = numOfNodes;
		}
		
		Node goalNode = new Node(scene, scene.mario, null,  Node.createAction(false, false, false, false));
		goalNode.depth = graphDepth;
		
		
	
	
		
		//order nodes after decending fitness score.
		Collections.sort(nodeList);
		
		//assert if the coalition between x and fitness holds
		for (int i=1; i < nodeList.size(); i++){
			Node prevNode = nodeList.get(i-1);
			Node curNode = nodeList.get(i);
			//System.out.println("X-coordinate: " + prevNode.x + " >= " + curNode.x + " Fitness: " + prevNode.fitness + " <= " + curNode.fitness);
			if (curNode.fitness != Float.MAX_VALUE){
			assertTrue(prevNode.x >= curNode.x);
			assertTrue(prevNode.fitness <= curNode.fitness);
			}
		}
			
		
		}
	/**
	 * 
	 */
	@Test
	public void testLargerAccelerationBetterFitness() {
		
		Node walkingNode = new Node(scene, scene.mario, null, Node.createAction(true, false, false, false));
		Node runningNode = new Node(scene, scene.mario, null, Node.createAction(true, false, false, true));
		
		walkingNode.mario.x = 100f;
		walkingNode.mario.xa = 1f;
		walkingNode.mario.y = 20f;
		
		runningNode.mario.x = 100f;
		runningNode.mario.xa = Node.maxSpeed;
		walkingNode.mario.y = 20f;
		
		walkingNode.fitnessEvaluation();
		runningNode.fitnessEvaluation();
		
		assertTrue(walkingNode.fitness > runningNode.fitness);
		}
	
	/**
	 * 
	 */
	@Test
	public void testJumpWhileFalling() {
		Node parentNode = new Node(scene, scene.mario, null, Node.createAction(true, false, false, true));
		Node currentNode = new Node(scene, scene.mario, null, Node.createAction(true, false, false, true));
		
		currentNode.parent = parentNode;
		
		parentNode.mario.x = 200f;
		parentNode.y = 20f;
		
		currentNode.mario.x=203f;
		currentNode.y=21f;
		
		assertFalse(currentNode.canJump());
		
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testJumpWhileJumping() {
		Node parentNode = new Node(scene, scene.mario, null, Node.createAction(true, false, false, true));
		Node currentNode = new Node(scene, scene.mario, null, Node.createAction(true, false, false, true));
		
		currentNode.parent = parentNode;
		
		parentNode.mario.x = 200f;
		parentNode.y = 20f;
		
		currentNode.mario.x=203f;
		currentNode.y=19f;
		
		System.out.println(currentNode.canJump());
		assertTrue(currentNode.canJump());
		
		
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testJumpWhileOnGroundHoldingJump() {
		Node parentNode = new Node(scene, scene.mario, null, Node.createAction(true, false, true, true));
		Node currentNode = new Node(scene, scene.mario, null, Node.createAction(true, false, false, true));
		
		currentNode.parent = parentNode;
		
		parentNode.mario.x = 200f;
		parentNode.y = 20f;
		
		currentNode.mario.x=203f;
		currentNode.y=20f;	
		
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
        
        //check size of grid in simulation
        assertEquals(19, simulationObject.receptiveFieldHeight);
        assertEquals(19, simulationObject.receptiveFieldWidth);
        
        
        //center position of the enviroment, and it should be the middle of the grid in the simulation.
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
		String options = "-vis off -lca off -lco off -lb off -le off -ltb off -ls 22 -fps 24 -miy 200 -tl 1" + " -mix "+ x; 
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
        
        
        for (int i = 0; i < levelSceneFromSim.length; i=i+19){
        	for(int j=0; j <19; j++){
        		assertEquals(levelSceneFromSim[j+i], levelSceneFromGame[j+i]);
        	}
        }

	}
}