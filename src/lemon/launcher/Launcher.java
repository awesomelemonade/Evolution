package lemon.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		launcher = new LauncherGui("Launcher", JFrame.EXIT_ON_CLOSE, x->launchProcess(x));
	}
	public static void launchProcess(String... args){
		List<String> builderArgs = new ArrayList<String>();
		builderArgs.add(settings.getValue("javaBin"));
		builderArgs.add(settings.getValue("natives"));
		builderArgs.add("-cp");
		builderArgs.add(settings.getValue("classpaths"));
		builderArgs.add(settings.getValue("main"));
		if(args!=null){
			for(String arg: args){
				builderArgs.add(arg);
			}
		}
		ProcessBuilder builder = new ProcessBuilder(builderArgs);
		try {
			launcher.startProcess(builder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
