package lemon.engine.toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import lemon.engine.math.Matrix;

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
	public static Matrix getPerspective(float fov, float aspect, float zNear, float zFar){
		Matrix matrix = new Matrix(4);
		float yScale = (float)(1/Math.tan(Math.toRadians(fov/2)));
		float xScale = yScale/aspect;
		matrix.set(0, 0, xScale);
		matrix.set(1, 1, yScale);
		matrix.set(2, 2, -(zNear+zFar)/(zFar-zNear));
		matrix.set(2, 3, (-2*zNear*zFar)/(zFar-zNear));
		matrix.set(3, 2, -1);
		matrix.set(3, 3, 0);
		return matrix;
	}
}
