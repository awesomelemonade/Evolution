package lemon.engine.toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

public class Toolbox {
	private Toolbox(){}
	
	public static StringBuilder getFile(String path){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
			StringBuilder builder = new StringBuilder();
			String line;
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
			reader.close();
			return builder;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static float getAspectRatio(long window){
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		return ((float)width.get())/((float)height.get());
	}
}
