package astar.sprites;

import astar.LevelScene;
import astar.level.Level;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.tools.MarioAIOptions;



public final class Mario extends Sprite implements Cloneable
{	
	public static final String[] MODES = new String[]{"small", "Large", "FIRE"};

	//        fire = (mode == MODE.MODE_FIRE);
	public static final int KEY_LEFT = 0;
	public static final int KEY_RIGHT = 1;
	public static final int KEY_DOWN = 2;
	public static final int KEY_JUMP = 3;
	public static final int KEY_SPEED = 4;
	public static final int KEY_UP = 5;

	public static final int STATUS_RUNNING = 2;
	public static final int STATUS_WIN = 1;
	public static final int STATUS_DEAD = 0;

	private static float marioGravity;

	public static boolean large = false;
	public static boolean fire = false;
	public static int coins = 0;
	public static int hiddenBlocksFound = 0;
	public static int collisionsWithCreatures = 0;
	public static int mushroomsDevoured = 0;
	public static int greenMushroomsDevoured = 0;
	public static int flowersDevoured = 0;

	private static boolean isTrace;

	private static boolean isMarioInvulnerable;

	private int status = STATUS_RUNNING;
	// for racoon when carrying the shell

	private boolean isRacoon;
	public float yaa = 1;

	private static float windCoeff = 0f;
	private static float iceCoeff = 0f;
	private static float jumpPower;
	private boolean inLadderZone;
	private boolean onLadder;
	private boolean onTopOfLadder = false;
	private static float GROUND_INERTIA = 0.89f;
	private static float AIR_INERTIA = 0.89f;
	
	public boolean[] keys = new boolean[Environment.numberOfKeys];
	public boolean[] cheatKeys;
	public float runTime;
	public boolean wasOnGround = false;
	public boolean onGround = false;
	public boolean mayJump = false;
	public boolean ducking = false;
	public boolean sliding = false;
	public int jumpTime = 0;
	public float xJumpSpeed;
	public float yJumpSpeed;
	
	public boolean ableToShoot = false;
	
	int width = 4;
	int height = 24;
	
	public LevelScene levelScene;
	public int facing;
	
	public int xDeathPos, yDeathPos;
	
	public int deathTime = 0;
	public int winTime = 0;
	private int invulnerableTime = 0;
	
	public Sprite carried = null;
	public static Mario instance;
	
	public float jT;
	private boolean lastLarge;
	private boolean lastFire;
	private boolean newLarge;
	private boolean newFire;
	
	public boolean canJump;

	private boolean loadFromEngine = false;
	
	/**
	 * Mario constructor
	 * @param levelScene
	 */
	public Mario(LevelScene levelScene)
	{
		kind = KIND_MARIO;
		this.levelScene = levelScene;
		x = levelScene.getMarioInitialPos().x;
		y = levelScene.getMarioInitialPos().y;
		mapX = (int) (x / 16);
		mapY = (int) (y / 16);
		
		facing = 1;
		setMode(Mario.large, Mario.fire);
		
		// Get gravity
		MarioAIOptions options = MarioAIOptions.getDefaultOptions();
		marioGravity = options.getMarioGravity();
		yaa = marioGravity * 3;
		jT = jumpPower / (marioGravity);
	}
	
	/**
	 * The levelScene is to be set manually afterwards
	 */
	@Override 
	public Object clone() 
	{
		// Maybe there is some objects that should be copied as well?
		Mario clone;
		try {
			clone = (Mario) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			clone = new Mario(levelScene);
		}
		// Important to copy keys, else we will operate on the same array object
		clone.keys = new boolean[Environment.numberOfKeys];
		for (int i = 0; i < this.keys.length; i++) {
			clone.keys[i] = this.keys[i];
		}
		clone.canJump = this.canJump;
		clone.mayJump = this.mayJump;
		clone.onGround = this.onGround;
		
		clone.xa = this.xa;
		clone.ya = this.ya;
		clone.yaa = this.yaa;
		clone.x = this.x;
		clone.y = this.y;

		return clone;
	}

	public static void resetStatic(MarioAIOptions marioAIOptions)
	{
		large = marioAIOptions.getMarioMode() > 0;
		fire = marioAIOptions.getMarioMode() == 2;
		coins = 0;
		hiddenBlocksFound = 0;
		mushroomsDevoured = 0;
		flowersDevoured = 0;
		collisionsWithCreatures = 0;

		isMarioInvulnerable = marioAIOptions.isMarioInvulnerable();
		marioGravity = marioAIOptions.getMarioGravity();
		jumpPower = marioAIOptions.getJumpPower();

		isTrace = marioAIOptions.isTrace();

		iceCoeff = marioAIOptions.getIce();
		windCoeff = marioAIOptions.getWind();
	}

	public int getMode()
	{
		return ((large) ? 1 : 0) + ((fire) ? 1 : 0);
	}



	private void blink(boolean on)
	{
		Mario.large = on ? newLarge : lastLarge;
		Mario.fire = on ? newFire : lastFire;
	}

	void setMode(boolean large, boolean fire)
	{
		//        System.out.println("large = " + large);
		if (fire) large = true;
		if (!large) fire = false;

		lastLarge = Mario.large;
		lastFire = Mario.fire;

		Mario.large = large;
		Mario.fire = fire;

		newLarge = Mario.large;
		newFire = Mario.fire;

		blink(true);
	}

	public void setRacoon(boolean isRacoon)
	{
		this.isRacoon = isRacoon;
	}

	public void move()
	{
		if (mapY > -1 && isTrace)
			++levelScene.level.marioTrace[this.mapX][this.mapY];

		if (winTime > 0)
		{
			winTime++;

			xa = 0;
			ya = 0;
			return;
		}

		if (deathTime > 0)
		{
			deathTime++;
			if (deathTime < 11)
			{
				xa = 0;
				ya = 0;
			} else if (deathTime == 11)
			{
				ya = -15;
			} else
			{
				ya += 2;
			}
			x += xa;
			y += ya;
			return;
		}

		if (invulnerableTime > 0) invulnerableTime--;
		visible = ((invulnerableTime / 2) & 1) == 0;

		wasOnGround = onGround;
		float sideWaysSpeed = keys[KEY_SPEED] ? 1.2f : 0.6f;

		//        float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		if (onGround)
		{
			ducking = keys[KEY_DOWN] && large;
		}

		if (xa > 2)
		{
			facing = 1;
		}
		if (xa < -2)
		{
			facing = -1;
		}

		//    float Wind = 0.2f;
		//    float windAngle = 180;
		//    xa += Wind * Math.cos(windAngle * Math.PI / 180);

		if (keys[KEY_JUMP] || (jumpTime < 0 && !onGround && !sliding))
		{
			if (jumpTime < 0)
			{
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed;
				jumpTime++;
			} else if (onGround && mayJump)
			{
				xJumpSpeed = 0;
				yJumpSpeed = -1.9f;
				jumpTime = (int) jT;
				ya = jumpTime * yJumpSpeed;
				onGround = false;
				sliding = false;
			} else if (sliding && mayJump)
			{
				xJumpSpeed = -facing * 6.0f;
				yJumpSpeed = -2.0f;
				jumpTime = -6;
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed;
				onGround = false;
				sliding = false;
				facing = -facing;
			} else if (jumpTime > 0)
			{
				xa += xJumpSpeed;
				ya = jumpTime * yJumpSpeed;
				jumpTime--;
			}
		} else
		{
			jumpTime = 0;
		}

		if (keys[KEY_LEFT] && !ducking)
		{
			if (facing == 1) sliding = false;
			xa -= sideWaysSpeed;
			if (jumpTime >= 0) facing = -1;
		}

		if (keys[KEY_RIGHT] && !ducking)
		{
			if (facing == -1) sliding = false;
			xa += sideWaysSpeed;
			if (jumpTime >= 0) facing = 1;
		}

		if ((!keys[KEY_LEFT] && !keys[KEY_RIGHT]) || ducking || ya < 0 || onGround)
		{
			sliding = false;
		}

		if (keys[KEY_SPEED] && ableToShoot && Mario.fire && levelScene.fireballsOnScreen < 2)
		{
			levelScene.addSprite(new Fireball(levelScene, x + facing * 6, y - 20, facing));
		}

		ableToShoot = !keys[KEY_SPEED];

		mayJump = (onGround || sliding) && !keys[KEY_JUMP];

		runTime += (Math.abs(xa)) + 5;
		if (Math.abs(xa) < 0.5f)
		{
			runTime = 0;
			xa = 0;
		}

		if (sliding)
		{
			ya *= 0.5f;
		}

		onGround = false;
		move(xa, 0);
		move(0, ya);

		if (y > levelScene.level.height * LevelScene.cellSize + LevelScene.cellSize)
			die("Gap");

		if (x < 0)
		{
			x = 0;
			xa = 0;
		}

		if (mapX >= levelScene.level.xExit && mapY <= levelScene.level.yExit)
		{
			x = (levelScene.level.xExit + 1) * LevelScene.cellSize;
			win();
		}

		if (x > levelScene.level.length * LevelScene.cellSize)
		{
			x = levelScene.level.length * LevelScene.cellSize;
			xa = 0;
		}

		ya *= 0.85f;
		if (onGround)
		{
			xa *= (GROUND_INERTIA + windScale(windCoeff, facing) + iceScale(iceCoeff));
		} else
		{
			xa *= (AIR_INERTIA + windScale(windCoeff, facing) + iceScale(iceCoeff));
		}

		if (!onGround)
		{
			//        ya += 3;
			ya += yaa;
		}

		if (carried != null)
		{
			carried.x = x + facing * 8; //TODO:|L| move to cellSize_2 = cellSize/2;
			carried.y = y - 2;
			if (!keys[KEY_SPEED])
			{
				carried.release(this);
				carried = null;
				setRacoon(false);
				//                System.out.println("carried = " + carried);
			}
			//            System.out.println("sideWaysSpeed = " + sideWaysSpeed);
		}
	}

	
	private boolean move(float xa, float ya)
	{
		while (xa > 8)
		{
			if (!move(8, 0)) return false;
			xa -= 8;
		}
		while (xa < -8)
		{
			if (!move(-8, 0)) return false;
			xa += 8;
		}
		while (ya > 8)
		{
			if (!move(0, 8)) return false;
			ya -= 8;
		}
		while (ya < -8)
		{
			if (!move(0, -8)) return false;
			ya += 8;
		}

		boolean collide = false;
		if (ya > 0)
		{
			if (isBlocking(x + xa - width, y + ya, xa, 0)) collide = true;
			else if (isBlocking(x + xa + width, y + ya, xa, 0)) collide = true;
			else if (isBlocking(x + xa - width, y + ya + 1, xa, ya)) collide = true;
			else if (isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;
		}
		if (ya < 0)
		{
			if (isBlocking(x + xa, y + ya - height, xa, ya)) collide = true;
			else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
			else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
		}
		if (xa > 0)
		{
			sliding = true;
			if (isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
			else sliding = false;
			if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) collide = true;
			else sliding = false;
			if (isBlocking(x + xa + width, y + ya, xa, ya)) collide = true;
			else sliding = false;
		}
		if (xa < 0)
		{
			sliding = true;
			if (isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
			else sliding = false;
			if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) collide = true;
			else sliding = false;
			if (isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;
			else sliding = false;
		}

		if (collide)
		{
			if (xa < 0)
			{
				x = (int) ((x - width) / 16) * 16 + width;
				this.xa = 0;
			}
			if (xa > 0)
			{
				x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
				this.xa = 0;
			}
			if (ya < 0)
			{
				y = (int) ((y - height) / 16) * 16 + height;
				jumpTime = 0;
				this.ya = 0;
			}
			if (ya > 0)
			{
				y = (int) ((y - 1) / 16 + 1) * 16 - 1;
				onGround = true;
			}
			return false;
		} else
		{
			x += xa;
			y += ya;
			return true;
		}
	}

	private boolean isBlocking(final float _x, final float _y, final float xa, final float ya)
	{
		int x = (int) (_x / 16);
		int y = (int) (_y / 16);
		if (x == (int) (this.x / 16) && y == (int) (this.y / 16)) return false;
		
		boolean blocking;
		byte block;
		//If true, game does not comply with interface rule - only for testing!
		if(loadFromEngine ){
		blocking = ch.idsia.benchmark.mario.engine.level.Level.IsBlocking(x, y, xa, ya);		
		block = ch.idsia.benchmark.mario.engine.level.Level.GetBlock(x, y);
		}
		else{
			blocking = levelScene.level.isBlocking(x, y, xa, ya);
			block = levelScene.level.getBlock(x, y);
		}
		
		
//		System.out.println("Sim: " + x + " - " + y + " Block: " + block);
//		if (blocking)
//			System.out.println("Sim " + x + " - " + y + " Block: " + block);
		
		if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
		{
			this.gainCoin();
			levelScene.level.setBlock(x, y, (byte) 0);	
		}

		if (blocking && ya < 0)
		{
			levelScene.bump(x, y, large);
		}
//		System.out.println(blocking);
		return blocking;
	}

	public void stomp(final Enemy enemy)
	{
		if (deathTime > 0) return;

		float targetY = enemy.y - enemy.height / 2;
		move(0, targetY - y);
		mapY = (int) y / 16;

		xJumpSpeed = 0;
		yJumpSpeed = -1.9f;
		jumpTime = (int) jT + 1;
		ya = jumpTime * yJumpSpeed;
		onGround = false;
		sliding = false;
		invulnerableTime = 1;
		levelScene.appendBonusPoints(MarioEnvironment.IntermediateRewardsSystemOfValues.stomp);
	}

	public void stomp(final Shell shell)
	{
		if (deathTime > 0) return;

		if (keys[KEY_SPEED] && shell.facing == 0)
		{
			carried = shell;
			shell.carried = true;
			setRacoon(true);
		} else
		{
			float targetY = shell.y - shell.height / 2;
			move(0, targetY - y);
			mapY = (int) y / 16;

			xJumpSpeed = 0;
			yJumpSpeed = -1.9f;
			jumpTime = (int) jT + 1;
			ya = jumpTime * yJumpSpeed;
			onGround = false;
			sliding = false;
			invulnerableTime = 1;
		}
		levelScene.appendBonusPoints(MarioEnvironment.IntermediateRewardsSystemOfValues.stomp);
	}

	public void getHurt(final int spriteKind)
	{
		if (deathTime > 0 || isMarioInvulnerable) return;

		if (invulnerableTime > 0) return;

		++collisionsWithCreatures;
		levelScene.appendBonusPoints(-MarioEnvironment.IntermediateRewardsSystemOfValues.kills);
		if (large)
		{
			//        levelScene.paused = true;
			//        powerUpTime = -3 * FractionalPowerUpTime;
			if (fire)
			{
				levelScene.mario.setMode(true, false);
			} else
			{
				levelScene.mario.setMode(false, false);
			}
			invulnerableTime = 32;
		} else
		{
			die("Collision with a creature [" + Sprite.getNameByKind(spriteKind) + "]");
		}
	}

	public void win()
	{
		xDeathPos = (int) x;
		yDeathPos = (int) y;
		winTime = 1;
		status = Mario.STATUS_WIN;
		levelScene.appendBonusPoints(MarioEnvironment.IntermediateRewardsSystemOfValues.win);
	}

	public void die(final String reasonOfDeath)
	{
		xDeathPos = (int) x;
		yDeathPos = (int) y;
		deathTime = 25;
		status = Mario.STATUS_DEAD;
		levelScene.addMemoMessage("Reason of death: " + reasonOfDeath);
		levelScene.appendBonusPoints(-MarioEnvironment.IntermediateRewardsSystemOfValues.win / 2);
	}

	public void devourFlower()
	{
		if (deathTime > 0) return;

		if (!fire)
		{
			levelScene.mario.setMode(true, true);
		} else
		{
			this.gainCoin();
		}
		++flowersDevoured;
		levelScene.appendBonusPoints(MarioEnvironment.IntermediateRewardsSystemOfValues.flowerFire);
	}

	public void devourMushroom()
	{
		if (deathTime > 0) return;

		if (!large)
		{
			levelScene.mario.setMode(true, false);
		} else
		{
			this.gainCoin();
		}
		++mushroomsDevoured;
		levelScene.appendBonusPoints(MarioEnvironment.IntermediateRewardsSystemOfValues.mushroom);
	}

	public void kick(final Shell shell)
	{
		//        if (deathTime > 0 || levelScene.paused) return;

		if (keys[KEY_SPEED])
		{
			carried = shell;
			shell.carried = true;
			setRacoon(true);
			//        System.out.println("shell = " + shell);
		} else
		{
			invulnerableTime = 1;
		}
	}

	public void stomp(final BulletBill bill)
	{
		if (deathTime > 0)
			return;

		float targetY = bill.y - bill.height / 2;
		move(0, targetY - y);
		mapY = (int) y / 16;

		xJumpSpeed = 0;
		yJumpSpeed = -1.9f;
		jumpTime = (int) jT + 1;
		ya = jumpTime * yJumpSpeed;
		onGround = false;
		sliding = false;
		invulnerableTime = 1;
		levelScene.appendBonusPoints(MarioEnvironment.IntermediateRewardsSystemOfValues.stomp);
	}

	public void gainCoin()
	{
		coins++;
		levelScene.appendBonusPoints(MarioEnvironment.IntermediateRewardsSystemOfValues.coins);
		//        if (coins % 100 == 0)
		//            get1Up();
	}

	public void gainHiddenBlock()
	{
		++hiddenBlocksFound;
		levelScene.appendBonusPoints(MarioEnvironment.IntermediateRewardsSystemOfValues.hiddenBlock);
	}

	public int getStatus()
	{
		return status;
	}

	public boolean isOnGround()
	{
		return onGround;
	}

	public boolean mayJump()
	{
		return mayJump;
	}

	public boolean isAbleToShoot()
	{
		return ableToShoot;
	}

	public void setInLadderZone(final boolean inLadderZone)
	{
		this.inLadderZone = inLadderZone;
		if (!inLadderZone)
		{
			onLadder = false;
			onTopOfLadder = false;
		}
	}

	public boolean isInLadderZone()
	{
		return this.inLadderZone;
	}

	public void setOnTopOfLadder(final boolean onTop)
	{
		this.onTopOfLadder = onTop;
	}

	public boolean isOnTopOfLadder()
	{
		return this.onTopOfLadder;
	}
}