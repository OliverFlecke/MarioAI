package astar.sprites;

import astar.LevelScene;

/**
 * Created by IntelliJ IDEA.
 * User: Nikolay Sohryakov, nikolay.sohyrakov@gmail.com
 * Date: Nov 28, 2010
 * Time: 12:21:34 AM
 * Package: ch.idsia.benchmark.mario.engine.sprites
 */
public class WaveGoomba extends Enemy
{
private LevelScene world;
private float amplitude = 10f;
private float lastSin;
private int sideWayCounter = 0;

public WaveGoomba(LevelScene world, int x, int y, int dir, int mapX, int mapY)
{
    super(world, x, y, dir, Sprite.KIND_WAVE_GOOMBA, true, mapX, mapY);
    noFireballDeath = false;
    this.world = world;
    lastSin = (float) Math.sin(x);
}

public void move()
{
    wingTime++;
    if (deadTime > 0)
    {
        deadTime--;

        if (deadTime == 0)
        {
            deadTime = 1;
            spriteContext.removeSprite(this);
        }

        if (flyDeath)
        {
            x += xa;
            y += ya;
            ya *= 0.95;
            ya += 1;
        }
        return;
    }


    float sideWaysSpeed = onGround ? 1.75f : 0.55f;

    if (xa > 2)
    {
        facing = 1;
    }
    if (xa < -2)
    {
        facing = -1;
    }

    xa = facing * sideWaysSpeed;

    runTime += (Math.abs(xa)) + 5;

    int runFrame = ((int) (runTime / 20)) % 2;

    if (!onGround)
    {
        runFrame = 1;
    }

    if (!move(xa, 0)) facing = -facing;
    onGround = false;
    if (winged)
    {
        float curSin = (float) Math.sin(x /10);
        ya = (curSin - lastSin) * amplitude;
        lastSin = curSin;
        sideWayCounter++;
    }
    move(0, ya);

    if (sideWayCounter >= 100)
    {
        sideWayCounter = 0;
        facing *= -1;
    }

    ya *= winged ? 0.95 : 0.85f;
    if (onGround)
    {
        xa *= (GROUND_INERTIA + windScale(windCoeff, facing) + iceScale(iceCoeff));
    } else
    {
        xa *= (AIR_INERTIA + windScale(windCoeff, facing) + iceScale(iceCoeff));
    }

    if (!onGround && !winged)
    {
        ya += yaa;
    }

    if (winged) runFrame = wingTime / 4 % 2;

}
}
