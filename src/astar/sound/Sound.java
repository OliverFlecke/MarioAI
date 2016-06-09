package astar.sound;


	import java.applet.Applet;
	import java.applet.AudioClip;

	public class Sound {

		public static final AudioClip EAT = Applet.newAudioClip(Sound.class.getResource("eat.wav"));
		public static final AudioClip GAMEOVER = Applet.newAudioClip(Sound.class.getResource("gameover.wav"));
		public static final AudioClip MUSIC = Applet.newAudioClip(Sound.class.getResource("music.wav"));
	}