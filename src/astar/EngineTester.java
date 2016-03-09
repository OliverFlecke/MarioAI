package astar;

import java.io.IOException;

import astar.level.TileBehavior;

public class EngineTester {
	
	public static void main(String [] args) throws IOException
	{
		
		for(byte b : new TileBehavior().getTileBehavior()){
			System.out.println(b+"\n");
		}
	}

}
