package astar.level;

import java.io.DataInputStream;
import java.io.IOException;



public class TileBehavior {

	public final static byte[] TILE_BEHAVIORS = new byte[256];
	
	public TileBehavior() throws IOException{
		
		loadBehaviors(new DataInputStream(TileBehavior.class.getResourceAsStream("/tiles.dat")));

	}

	static void loadBehaviors(DataInputStream dis) throws IOException
	{
		dis.readFully(TILE_BEHAVIORS);
	}
	
	public byte[] getTileBehavior(){
		return TILE_BEHAVIORS;
	}
}