package lemon.launcher;

import java.io.File;
import java.io.IOException;

public class Launcher {
	public static void main(String[] args){
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome+File.separator+"bin"+File.separator+"java";
		String natives = "-Djava.library.path=/home/awesomelemonade/Data/lwjgl/native";
		String classpaths = "/home/awesomelemonade/Data/lwjgl/jar/lwjgl.jar:bin";
		String main = "lemon.engine.evolution.Evolution";
		ProcessBuilder builder = new ProcessBuilder(javaBin, natives, "-cp", classpaths, main).inheritIO();
		try {
			Process process = builder.start();
			System.out.println(process.waitFor());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
