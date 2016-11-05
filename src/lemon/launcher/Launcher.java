package lemon.launcher;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

public class Launcher {
	private static LauncherGui launcher;
	private static Settings settings;
	public static void main(String[] args){
		settings = new Settings();
		settings.setDefaultValue("javaHome", System.getProperty("java.home"));
		settings.setDefaultValue("javaBin", settings.getValue("javaHome")+File.separator+"bin"+File.separator+"java");
		settings.setDefaultValue("natives", "-Djava.library.path=/home/awesomelemonade/Data/lwjgl/native");
		settings.setDefaultValue("classpaths", "/home/awesomelemonade/Data/lwjgl/jar/lwjgl.jar:bin");
		settings.setDefaultValue("main", "lemon.engine.evolution.Evolution");
		launcher = new LauncherGui("Launcher", JFrame.EXIT_ON_CLOSE, ()->launchProcess());
	}
	public static void launchProcess(){
		ProcessBuilder builder = new ProcessBuilder(settings.getValue("javaBin"), settings.getValue("natives"),
				"-cp", settings.getValue("classpaths"), settings.getValue("main"));
		try {
			launcher.startProcess(builder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
