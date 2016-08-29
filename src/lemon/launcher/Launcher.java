package lemon.launcher;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

public class Launcher {
	private static LauncherGui launcher;
	private static String javaHome = System.getProperty("java.home");
	private static String javaBin = javaHome+File.separator+"bin"+File.separator+"java";
	private static String natives = "-Djava.library.path=/home/awesomelemonade/Data/lwjgl/native";
	private static String classpaths = "/home/awesomelemonade/Data/lwjgl/jar/lwjgl.jar:bin";
	private static String main = "lemon.engine.evolution.Evolution";
	public static void main(String[] args){
		launcher = new LauncherGui("Launcher", JFrame.EXIT_ON_CLOSE, new ProcessLauncher(){
			@Override
			public void launchProcess() {
				ProcessBuilder builder = new ProcessBuilder(javaBin, natives, "-cp", classpaths, main).inheritIO();
				try {
					launcher.startProcess(builder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		//process.waitFor();
		//process.exitValue();
		//process.isAlive();
		//process.destroy();
		//process.destroyForcibly();
	}
}
