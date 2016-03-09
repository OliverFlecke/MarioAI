package astar;

import java.io.IOException;

import astar.level.Level;
import astar.level.TileBehavior;

public class EngineTester {
	
	static LevelScene levelScene = new LevelScene();

	public static void main(String [] args) throws IOException
	{
		
		if(verbose){
		for(byte b : new TileBehavior().getTileBehavior()){
			System.out.println(b+"\n");
		}
		}

		levelScene.level = new Level(1500,15);
		System.out.println("success");
		
		printCreatures();
		
		levelScene.tick();
		
		printCreatures();
	}
	
	static void printCreatures(){
		for(int i=0; i<levelScene.getCreaturesFloatPos().length-1; i++){
			System.out.println("creature coordinate =(" + levelScene.getCreaturesFloatPos()[i] + "," + levelScene.getCreaturesFloatPos()[i+1] + ")"); 
			
		}
	}

}