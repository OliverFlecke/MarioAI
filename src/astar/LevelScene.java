package astar;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.Cloneable;

import com.sun.xml.internal.ws.api.pipe.Engine;

import astar.level.Level;
import astar.level.SpriteTemplate;
import astar.sprites.BulletBill;
import astar.sprites.Enemy;
import astar.sprites.FireFlower;
import astar.sprites.Fireball;
import astar.sprites.Mario;
import astar.sprites.Mushroom;
import astar.sprites.Shell;
import astar.sprites.Sprite;
import astar.sprites.SpriteContext;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.tools.MarioAIOptions;

/**
 * The level scene class. Contains the given level, which is stored in the Level object, 
 * as well as the Mario and enemies object.
 */
public final class LevelScene implements SpriteContext, Cloneable
{
	//debug flags
	int debugSetBlocks = 0; // 1 prints 19:19 environment and set data. 2 also prints whole level
	boolean debugSetEnemies = false;

	public static final int cellSize = 16;

	public List<Sprite> sprites = new ArrayList<Sprite>();
	private List<Sprite> spritesToAdd = new ArrayList<Sprite>();
	private List<Sprite> spritesToRemove = new ArrayList<Sprite>();

	public static Level level;
	public Mario mario;
	public float xCam, yCam, xCamO, yCamO;

	public int tickCount;

	public int startTime = 0;
	private int timeLeft;
	private int width;
	private int height;

	private static boolean onLadder = false;

	private Random randomGen = new Random(0);

	private List<Float> enemiesFloatsList = new ArrayList<Float>();
	private float[] marioFloatPos = new float[2];
	private int[] marioState = new int[11];
	private int numberOfHiddenCoinsGained = 0;

	private int greenMushroomMode = 0;

	public String memo = "";
	private Point marioInitialPos = new Point(0,32);
	private int bonusPoints = -1;

	//    public int getTimeLimit() {  return timeLimit; }

	public void setTimeLimit(int timeLimit)
	{ 
		this.timeLimit = timeLimit; 
	}

	private int timeLimit = 200;

	private long levelSeed;
	private int levelType;
	private int levelDifficulty;
	private int levelLength;
	private int levelHeight;
	public static int killedCreaturesTotal;
	public static int killedCreaturesByFireBall;
	public static int killedCreaturesByStomp;
	public static int killedCreaturesByShell;



	public LevelScene()
	{
		try
		{
			Level.loadBehaviors();
		} catch (IOException e)
		{
			System.err.println("Error finding tiles.dat, check the TileBehavior Class");
			e.printStackTrace();
			System.exit(0);
		}
		Sprite.spriteContext = this;
		sprites.clear();

		// This (last) Mario should be a clone for all the node instances
		this.mario = new Mario(this);
		this.mario.levelScene = this;
		//		this.addSprite(mario);

		startTime = 1;
		timeLeft = 3000;
		tickCount = 1;		
	}

	@Override
	public Object clone() 
	{
		LevelScene clone = null;
		try 
		{
			clone = (LevelScene) super.clone();
			
			// Copy Mario and other objects
			clone.mario = (Mario) this.mario.clone();
			clone.mario.levelScene = clone;
	
			// Clone the list of sprites into the new object
			clone.sprites = new ArrayList<Sprite>();
			for (Sprite sprite : this.sprites)
			{
				clone.sprites.add((Sprite)sprite.clone());
			}
		} 
		catch (CloneNotSupportedException e) 
		{
			e.printStackTrace();
		}
		return clone;
	}

	public float[] getEnemiesFloatPos()
	{
		enemiesFloatsList.clear();
		for (Sprite sprite : sprites)
		{
			// TODO:[M]: add unit tests for getEnemiesFloatPos involving all kinds of creatures
			if (sprite.isDead()) continue;
			switch (sprite.kind)
			{
			case Sprite.KIND_GOOMBA:
			case Sprite.KIND_BULLET_BILL:
			case Sprite.KIND_ENEMY_FLOWER:
			case Sprite.KIND_GOOMBA_WINGED:
			case Sprite.KIND_GREEN_KOOPA:
			case Sprite.KIND_GREEN_KOOPA_WINGED:
			case Sprite.KIND_RED_KOOPA:
			case Sprite.KIND_RED_KOOPA_WINGED:
			case Sprite.KIND_SPIKY:
			case Sprite.KIND_SPIKY_WINGED:
			case Sprite.KIND_SHELL:
			{
				enemiesFloatsList.add((float) sprite.kind);
				enemiesFloatsList.add(sprite.x - mario.x);
				enemiesFloatsList.add(sprite.y - mario.y);
			}
			}
		}

		float[] enemiesFloatsPosArray = new float[enemiesFloatsList.size()];

		int i = 0;
		for (Float F : enemiesFloatsList)
			enemiesFloatsPosArray[i++] = F;

		return enemiesFloatsPosArray;
	}

	public int fireballsOnScreen = 0;

	List<Shell> shellsToCheck = new ArrayList<Shell>();

	public void checkShellCollide(Shell shell)
	{
		shellsToCheck.add(shell);
	}

	List<Fireball> fireballsToCheck = new ArrayList<Fireball>();

	public void checkFireballCollide(Fireball fireball)
	{
		fireballsToCheck.add(fireball);
	}

	public void tick()
	{
		timeLeft--;
		if (timeLeft == 0)
			mario.die("Time out!");
		
		if (startTime > 0)
			startTime++;
	
		fireballsOnScreen = 0;

		tickCount++;

		for (Sprite sprite : sprites)
		{			
			if (!(sprite instanceof Mario))
			{
				sprite.tick();
			}
			sprite.collideCheck();
		}

		for (Shell shell : shellsToCheck)
		{
			for (Sprite sprite : sprites)
			{
				if (sprite != shell && !shell.dead)
				{
					if (sprite.shellCollideCheck(shell))
					{
						if (mario.carried == shell && !shell.dead)
						{
							mario.carried = null;
							mario.setRacoon(false);
							shell.die();
							++this.killedCreaturesTotal;
						}
					}
				}
			}
		}
		shellsToCheck.clear();

		for (Fireball fireball : fireballsToCheck)
			for (Sprite sprite : sprites)
				if (sprite != fireball && !fireball.dead)
					if (sprite.fireballCollideCheck(fireball))
						fireball.die();
		fireballsToCheck.clear();


		sprites.addAll(0, spritesToAdd);
		sprites.removeAll(spritesToRemove);
		spritesToAdd.clear();
		spritesToRemove.clear();
	}

	public void addSprite(Sprite sprite)
	{
		spritesToAdd.add(sprite);
		sprite.tick();
	}

	public void removeSprite(Sprite sprite)
	{
		spritesToRemove.add(sprite);
	}

	public void bump(int x, int y, boolean canBreakBricks)
	{
		byte block = level.getBlock(x, y);

		if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0)
		{
			if (block == 1)
				this.mario.gainHiddenBlock();
			bumpInto(x, y - 1);
			byte blockData = level.getBlockData(x, y);
			if (blockData < 0)
				level.setBlockData(x, y, (byte) (blockData + 1));

			if (blockData == 0)
			{
				level.setBlock(x, y, (byte) 4);
				level.setBlockData(x, y, (byte) 4);
			}

			if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0)
			{
				{
					if (!Mario.large)
					{
						addSprite(new Mushroom(this, x * cellSize + 8, y * cellSize + 8));
						++level.counters.mushrooms;
					} else
					{
						addSprite(new FireFlower(this, x * cellSize + 8, y * cellSize + 8));
						++level.counters.flowers;
					}
				}
			} else
			{
				this.mario.gainCoin();
			}
		}

		if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0)
		{
			bumpInto(x, y - 1);
			if (canBreakBricks)
			{
				level.setBlock(x, y, (byte) 0);

			} else
			{
				level.setBlockData(x, y, (byte) 4);
			}
		}
	}

	public void bumpInto(int x, int y)
	{
		byte block = level.getBlock(x, y);
		if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
		{
			this.mario.gainCoin();
			level.setBlock(x, y, (byte) 0);
		}

		for (Sprite sprite : sprites)
		{
			sprite.bumpCheck(x, y);
		}
	}

	public int getTimeSpent() { return startTime / GlobalOptions.mariosecondMultiplier; }

	public int getTimeLeft() { return timeLeft / GlobalOptions.mariosecondMultiplier; }

	public int getKillsTotal()
	{
		return killedCreaturesTotal;
	}

	public int getKillsByFire()
	{
		return killedCreaturesByFireBall;
	}

	public int getKillsByStomp()
	{
		return killedCreaturesByStomp;
	}

	public int getKillsByShell()
	{
		return killedCreaturesByShell;
	}

	public int[] getMarioState()
	{
		marioState[0] = this.getMarioStatus();
		marioState[1] = this.getMarioMode();
		marioState[2] = this.isMarioOnGround() ? 1 : 0;
		marioState[3] = this.isMarioAbleToJump() ? 1 : 0;
		marioState[4] = this.isMarioAbleToShoot() ? 1 : 0;
		marioState[5] = this.isMarioCarrying() ? 1 : 0;
		marioState[6] = this.getKillsTotal();
		marioState[7] = this.getKillsByFire();
		marioState[8] = this.getKillsByStomp();
		marioState[9] = this.getKillsByShell();
		marioState[10] = this.getTimeLeft();
		return marioState;
	}

	public void performAction(boolean[] action)
	{
		// might look ugly , but arrayCopy is not necessary here:
		this.mario.keys = action;
	}

	public boolean isLevelFinished()
	{
		return (mario.getStatus() != Mario.STATUS_RUNNING);
	}

	public boolean isMarioAbleToShoot()
	{
		return mario.isAbleToShoot();
	}

	public int getMarioStatus()
	{
		return mario.getStatus();
	}

	/**
	 * first and second elements of the array are x and y Mario coordinates correspondingly
	 *
	 * @return an array of size 2*(number of creatures on screen) including mario
	 */
	public float[] getCreaturesFloatPos()
	{
		float[] enemies = this.getEnemiesFloatPos();
		float ret[] = new float[enemies.length + 2];
		System.arraycopy(this.getMarioFloatPos(), 0, ret, 0, 2);
		System.arraycopy(enemies, 0, ret, 2, enemies.length);
		return ret;
	}

	public boolean isMarioOnGround()
	{ return mario.isOnGround(); }

	public boolean isMarioAbleToJump()
	{ return mario.mayJump(); }

	public float[] getMarioFloatPos()
	{
		marioFloatPos[0] = this.mario.x;
		marioFloatPos[1] = this.mario.y;
		return marioFloatPos;
	}

	public int getMarioMode() {
		return mario.getMode();
	}

	public boolean isMarioCarrying() {
		return mario.carried != null;
	}

	public int getLevelDifficulty() {
		return levelDifficulty;
	}

	public long getLevelSeed() {
		return levelSeed;
	}

	public int getLevelLength() {
		return levelLength;
	}

	public int getLevelHeight() {
		return levelHeight;
	}

	public int getLevelType() {
		return levelType;
	}

	public void addMemoMessage(final String memoMessage) {
		memo += memoMessage;
	}

	public Point getMarioInitialPos() {
		return marioInitialPos;
	}

	public int getGreenMushroomMode() {
		return greenMushroomMode;
	}

	public int getBonusPoints() {
		return bonusPoints;
	}

	public void setBonusPoints(final int bonusPoints) {
		this.bonusPoints = bonusPoints;
	}

	public void appendBonusPoints(final int superPunti) {
		bonusPoints += superPunti;
	}

	public void setup(byte[][] levelSceneObservationZ, float[] enemiesFloatPos) {
		//		this.level.map = levelSceneObservationZ;
		this.setLevelScene(levelSceneObservationZ);
		setEnemiesFloatPos(enemiesFloatPos);
	}

	/**
	 * Set levelscene data based on the information from the interface
	 * @param levelSceneObservationZ
	 */
	private void setLevelScene(byte[][] obs) {

		for(int i=0; i<19; i++){
			for(int j=0; j<19; j++){
				byte b = obs[i][j];
				if(b==-60)
					b=-127;
				int x = (int)(mario.x/16+j-9);
				int y = (int)(mario.y/16+i-9);
				level.setBlock(x, y, b);
			}
		} 
		if(debugSetBlocks>0){
			System.out.println();
			System.out.println("Data original/copy");
			System.out.println(mario.x + " : " + mario.y);
			System.out.println();
			boolean printColumnNr = true;
			for(int i=0; i<19; i++){
				for(int j=0; j<19; j++){
					if(printColumnNr){
						String nr = Integer.toString((int)(mario.x/16+j-9));
						System.out.printf("|%7s%-8s",nr,"");
					}
					else{
						int x = (int)(mario.x/16+j-9);
						int y = (int)(mario.y/16+i-9);
						String c = "["+level.getBlock(x, y)+"]";
						String o = "["+ch.idsia.benchmark.mario.engine.level.Level.GetBlock(x, y)+"]";
						System.out.printf("|%7s/%-7s", o,c);
					}
				}
				printColumnNr = false;
				System.out.println("");
			}

			System.out.println();
			System.out.println();
			System.out.println("**********************************************************************************************");
			System.out.println();

			if(debugSetBlocks>1){
				System.out.println();
				System.out.println("Whole level");
				System.out.println();
				printColumnNr = true;
				for(int i=0; i<19; i++){
					for(int j=0; j<1500; j++){
						int x = j;
						int y = i;
						if(printColumnNr){
							String nr = Integer.toString(j);
							System.out.printf("|%7s%-8s",nr,"");
						}
						else{
							String o = "["+ch.idsia.benchmark.mario.engine.level.Level.GetBlock(x, y)+"]";
							System.out.printf("|%7s/%-7s", o,0);
						}
					}
					printColumnNr = false;
					System.out.println("");
				}

				System.out.println();
				System.out.println();
				System.out.println("**********************************************************************************************");
				System.out.println();
			}
		}
	}

	//    public boolean setLevelScene(byte[][] data)
	//    {
	//        int HalfObsWidth = 11;
	//        int HalfObsHeight = 11;
	//        int MarioXInMap = (int)mario.x/16;
	//        int MarioYInMap = (int)mario.y/16;
	//        boolean gapAtLast = true;
	//        boolean gapAtSecondLast = true;
	//        int lastEventX = 0;
	//        int[] heights = new int[22];
	//        for(int i = 0; i < heights.length; i++)
	//        	heights[i] = 0;
	//        
	//        int gapBorderHeight = 0;
	//        int gapBorderMinusOneHeight = 0;
	//        int gapBorderMinusTwoHeight = 0;
	//        
	//        for (int y = MarioYInMap - HalfObsHeight, obsX = 0; y < MarioYInMap + HalfObsHeight; y++, obsX++)
	//        {
	//            for (int x = MarioXInMap - HalfObsWidth, obsY = 0; x < MarioXInMap + HalfObsWidth; x++, obsY++)
	//            {
	//                if (x >=0 && x <= level.xExit && y >= 0 && y < level.height)
	//                {
	//                	byte datum = data[obsX][obsY];
	//                	
	//                 	if (datum != 0 && datum != -10 && datum != 1 && obsY > lastEventX)
	//                	{
	//                 		lastEventX = obsY;
	//                	}
	//                 	if (datum != 0 && datum != 1)
	//                	{
	//                		if (heights[obsY] == 0)
	//                		{
	//                			heights[obsY] = y;
	//                		}
	//                	}
	//                 	
	//                	// cannon detection: if there's a one-block long hill, it's a cannon!
	//                 	// i think this is not required anymore, because we get the cannon data straight from the API.
	//                	if (x == MarioXInMap + HalfObsWidth - 3 &&
	//                			datum != 0 && y > 5)
	//                	{
	//
	//                		if (gapBorderMinusTwoHeight == 0)
	//                			gapBorderMinusTwoHeight = y;
	//                	}
	//                	if (x == MarioXInMap + HalfObsWidth - 2 &&
	//                			datum != 0 && y > 5)
	//                	{
	//                		if (gapBorderMinusOneHeight == 0)
	//                			gapBorderMinusOneHeight = y;
	//                		gapAtSecondLast = false;
	//                	}
	//                	if (x == MarioXInMap + HalfObsWidth - 1 &&
	//                			datum != 0 && y > 5)
	//                	{
	//
	//                		if (gapBorderHeight == 0)
	//                			gapBorderHeight = y;
	//                		gapAtLast = false;
	//                	}
	//                	
	//                    if (datum != 1 && level.getBlock(x, y) != 14) 
	//                    	level.setBlock(x, y, datum);
	//                }
	//            }
	//        }
	//        if (gapBorderHeight == gapBorderMinusTwoHeight && gapBorderMinusOneHeight < gapBorderHeight)
	//        {
	//        	// found a cannon!
	//        	//System.out.println("Got a cannon!");
	//        	level.setBlock(MarioXInMap + HalfObsWidth - 2,gapBorderMinusOneHeight, (byte)14);
	//        }
	//        if (gapAtLast && !gapAtSecondLast)
	//        {
	//        	// found a gap. 
	//        	int holeWidth = 3;
	//
	//    		// make the gap wider before we see the end to allow ample time for the 
	//        	// planner to jump over.
	//        	for(int i = 0; i < holeWidth; i++)
	//        	{
	//            	for(int j = 0; j < 15; j++)
	//            	{
	//            		level.setBlock(MarioXInMap + HalfObsWidth + i, j, (byte) 0);
	//            	}
	//            	level.isGap[MarioXInMap + HalfObsWidth + i] = true;
	//            	level.gapHeight[MarioXInMap + HalfObsWidth + i] = gapBorderMinusOneHeight;
	//        	}
	//        	for(int j = gapBorderMinusOneHeight; j < 16; j++)
	//        	{
	//        		level.setBlock(MarioXInMap + HalfObsWidth + holeWidth, gapBorderMinusOneHeight, (byte) 4);
	//        	}
	//        	return true;
	//        }
	//    	return false;
	//    }

	//adding enemy sprites and fireballs etc
	/**
	 * Create objects for the enemies in the game from the enemies float array. 
	 * @param enemiesFloatPos The array with all the information of the enemies in the game
	 */
	public void setEnemiesFloatPos(float[] enemiesFloatPos)
	{
		for(int i = 0; i < enemiesFloatPos.length; i += 3){

			int type = (int)enemiesFloatPos[i];
			float x = enemiesFloatPos[i + 1];
			float y = enemiesFloatPos[i + 2];

			boolean winged = false;

			switch (type) 
			{
				case (Sprite.KIND_GOOMBA_WINGED):
				case (Sprite.KIND_GREEN_KOOPA_WINGED):
				case (Sprite.KIND_RED_KOOPA_WINGED):
				case (Sprite.KIND_SPIKY_WINGED):
					winged = true;
					break;
				case (Sprite.KIND_BULLET_BILL):
				case (Sprite.KIND_SHELL):
				case (Sprite.KIND_GOOMBA):
				case (Sprite.KIND_ENEMY_FLOWER):
				case (Sprite.KIND_GREEN_KOOPA):
				case (Sprite.KIND_RED_KOOPA):
				case (Sprite.KIND_SPIKY):
				case (Sprite.KIND_WAVE_GOOMBA):
				default:
					winged = false;
			}
//			switch (kind) {
//			case(Sprite.KIND_BULLET_BILL): 
//				type = -2;
//				break;
//			case(Sprite.KIND_SHELL): 
//				type = Enemy.KIND_SHELL;
//				break;
//			case(Sprite.KIND_GOOMBA): 
//				type = Enemy.KIND_GOOMBA;
//				break;
//			case(Sprite.KIND_GOOMBA_WINGED): 
//				type = Enemy.KIND_GOOMBA_WINGED; 
//				winged = true;
//				break;
//			case(Sprite.KIND_GREEN_KOOPA): 
//				type = Enemy.KIND_GREEN_KOOPA;
//				break;
//			case(Sprite.KIND_GREEN_KOOPA_WINGED): 
//				type = Enemy.KIND_GREEN_KOOPA_WINGEDE; 
//				winged = true;
//				break;
//			case(Sprite.KIND_RED_KOOPA): type = Enemy.ENEMY_RED_KOOPA;
//			break;
//			case(Sprite.KIND_RED_KOOPA_WINGED): type = Enemy.ENEMY_RED_KOOPA; winged = true;
//			break;
//			case(Sprite.KIND_SPIKY): type = Enemy.ENEMY_SPIKY;
//			break;
//			case(Sprite.KIND_SPIKY_WINGED): type = Enemy.ENEMY_SPIKY; winged = true;
//			break;
//			default : type=-1;
//			break;
//			}
			Sprite sprite = null;
			if(type == -1)
			{
				System.out.println("oh shit type -1");
			}
			else
			{
				sprite = new Enemy(this, x, y, -1, type, winged, (int) x/16, (int) y/16);
			}

			sprite.spriteTemplate =  new SpriteTemplate(type);
			sprites.add(sprite);

		}

		if (debugSetEnemies){
			for(Sprite s : sprites){
				System.out.println("kind: " + (int) s.kind + ", Coordinate: " + s.x + "," + s.y);			
			}
			System.out.println();
		}
	}


	public void resetDefault()
	{
		// TODO: set values to defaults
		reset(MarioAIOptions.getDefaultOptions());
	}

	public void reset(MarioAIOptions marioAIOptions)
	{
		//        System.out.println("\nLevelScene RESET!");
		//        this.gameViewer = setUpOptions[0] == 1;
		//        System.out.println("this.mario.isMarioInvulnerable = " + this.mario.isMarioInvulnerable);
		//    this.levelDifficulty = marioAIOptions.getLevelDifficulty();
		//        System.out.println("this.levelDifficulty = " + this.levelDifficulty);
		//    this.levelLength = marioAIOptions.getLevelLength();
		//        System.out.println("this.levelLength = " + this.levelLength);
		//    this.levelSeed = marioAIOptions.getLevelRandSeed();
		//        System.out.println("levelSeed = " + levelSeed);
		//    this.levelType = marioAIOptions.getLevelType();
		//        System.out.println("levelType = " + levelType);


		GlobalOptions.FPS = marioAIOptions.getFPS();
		//        System.out.println("GlobalOptions.FPS = " + GlobalOptions.FPS);
		GlobalOptions.isPowerRestoration = marioAIOptions.isPowerRestoration();
		//        System.out.println("GlobalOptions.isPowerRestoration = " + GlobalOptions.isPowerRestoration);
		//    GlobalOptions.isPauseWorld = marioAIOptions.isPauseWorld();
		GlobalOptions.areFrozenCreatures = marioAIOptions.isFrozenCreatures();
		//        System.out.println("GlobalOptions = " + GlobalOptions.isPauseWorld);
		//        GlobalOptions.isTimer = marioAIOptions.isTimer();
		//        System.out.println("GlobalOptions.isTimer = " + GlobalOptions.isTimer);
		//        isToolsConfigurator = setUpOptions[11] == 1;
		this.setTimeLimit(marioAIOptions.getTimeLimit());
		//        System.out.println("this.getTimeLimit() = " + this.getTimeLimit());
		//        this.isViewAlwaysOnTop() ? 1 : 0, setUpOptions[13]
		GlobalOptions.isVisualization = marioAIOptions.isVisualization();
		//        System.out.println("visualization = " + visualization);

		killedCreaturesTotal = 0;
		killedCreaturesByFireBall = 0;
		killedCreaturesByStomp = 0;
		killedCreaturesByShell = 0;

		marioInitialPos = marioAIOptions.getMarioInitialPos();
		greenMushroomMode = marioAIOptions.getGreenMushroomMode();

		//	    if (replayer != null)
		//	    {
		//	        try
		//	        {
		//	//            replayer.openNextReplayFile();
		//	            replayer.openFile("level.lvl");
		//	            level = (Level) replayer.readObject();
		//	            level.counters.resetUncountableCounters();
		//	//            replayer.closeFile();
		//	//            replayer.closeRecorder();
		//	        } catch (IOException e)
		//	        {
		//	            System.err.println("[Mario AI Exception] : level reading failed");
		//	            e.printStackTrace();
		//	        } catch (Exception e)
		//	        {
		//	            e.printStackTrace();
		//	        }
		//	    } else
		//	        level = LevelGenerator.createLevel(marioAIOptions);

		String fileName = marioAIOptions.getLevelFileName();
		if (!fileName.equals(""))
		{
			try
			{
				Level.save(level, new ObjectOutputStream(new FileOutputStream(fileName)));
			} catch (IOException e)
			{
				System.err.println("[Mario AI Exception] : Cannot write to file " + fileName);
				e.printStackTrace();
			}
		}
		this.levelSeed = level.randomSeed;
		this.levelLength = level.length;
		this.levelHeight = level.height;
		this.levelType = level.type;
		this.levelDifficulty = level.difficulty;

		Sprite.spriteContext = this;
		sprites.clear();
		this.width = GlobalOptions.VISUAL_COMPONENT_WIDTH;
		this.height = GlobalOptions.VISUAL_COMPONENT_HEIGHT;

		Sprite.setCreaturesGravity(marioAIOptions.getCreaturesGravity());
		Sprite.setCreaturesWind(marioAIOptions.getWind());
		Sprite.setCreaturesIce(marioAIOptions.getIce());
		Mario.resetStatic(marioAIOptions);

		bonusPoints = -1;

		mario = new Mario(this);
		//System.out.println("mario = " + mario);
		memo = "";

		sprites.add(mario);
		startTime = 1;
		timeLeft = timeLimit * GlobalOptions.mariosecondMultiplier;

		tickCount = 0;
	}

}