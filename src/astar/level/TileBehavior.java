package astar.level;

import java.io.DataInputStream;
import java.io.IOException;



public final class TileBehavior {

	public final static byte[] TILE_BEHAVIORS = new byte[256];
	
	private TileBehavior(){		
		System.out.println("static class");
	}

	public static void loadTileBehaviors() throws IOException {
		DataInputStream dis = new DataInputStream(TileBehavior.class.getResourceAsStream("tiles.dat"));
		dis.readFully(TILE_BEHAVIORS);
	}
	
	public static byte[] getTileBehavior(){
		return TILE_BEHAVIORS;
	}
}