/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package astar.level;

import astar.LevelScene;
import astar.sprites.*;

import java.io.Serializable;
import java.lang.Cloneable;

public class SpriteTemplate implements Serializable, Cloneable
{
	public int lastVisibleTick = -1;
	public Sprite sprite;
	public boolean isDead = false;
	private boolean winged;

	private static final long serialVersionUID = -6585112454240065011L;

	public int getType()
	{
		return type;
	}

	private int type;

	public SpriteTemplate(int type)
	{
		this.type = type;
		switch (type)
		{
		case Sprite.KIND_GOOMBA:
			this.winged = false;
			break;
		case Sprite.KIND_GREEN_KOOPA:
			this.winged = false;
			break;
		case Sprite.KIND_RED_KOOPA:
			this.winged = false;
			break;
		case Sprite.KIND_SPIKY:
			this.winged = false;
			break;
		case Sprite.KIND_GOOMBA_WINGED:
			this.winged = true;
			break;
		case Sprite.KIND_GREEN_KOOPA_WINGED:
			this.winged = true;
			break;
		case Sprite.KIND_RED_KOOPA_WINGED:
			this.winged = true;
			break;
		case Sprite.KIND_SPIKY_WINGED:
			this.winged = true;
			break;
		case Sprite.KIND_ENEMY_FLOWER:
			this.winged = false;
			break;
		case Sprite.KIND_BULLET_BILL:
			this.winged = false;
			break;
		case Sprite.KIND_PRINCESS:
			this.winged = false;
			break;
		case Sprite.KIND_WAVE_GOOMBA:
			this.winged = true;
			break;
		}
	}

	public void spawn(LevelScene levelScene, int x, int y, int dir)
	{
		if (isDead) return;

		else if (type == Sprite.KIND_WAVE_GOOMBA)
		{
			//            sprite = new Enemy(levelScene, x*16+8, y*16+15, dir, type, winged);
			sprite = new Enemy(levelScene, x * 16 + 8, y * 16 + 15, dir, type, winged, x, y);
		}
		sprite.spriteTemplate = this;
		levelScene.addSprite(sprite);
	}
	
	@Override 
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}