package lemon.engine.evolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.control.InitEvent;
import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;

public enum Test implements Listener {
	INSTANCE;
	
	int vao;
	int program;
	
	@Subscribe
	public void init(InitEvent event){
		FloatBuffer data = BufferUtils.createFloatBuffer(5*3);
		IntBuffer indices = BufferUtils.createIntBuffer(3);
		data.put(new float[]{
				0f, -1f, 1f, 0f, 0f,
				-1f, 1f, 0f, 1f, 0f,
				1f, 1f, 0f, 0f, 1f
		});
		indices.put(new int[]{0, 1, 2});
		data.flip();
		indices.flip();
		vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.glGenBuffers());
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);;
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, GL15.glGenBuffers());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 5*4, 0);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 5*4, 2*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(vao);
		
		program = GL20.glCreateProgram();
		int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		GL20.glShaderSource(vertexShader, getFile("shaders/vertexShader"));
		GL20.glShaderSource(fragmentShader, getFile("shaders/fragmentShader"));
		GL20.glCompileShader(vertexShader);
		GL20.glCompileShader(fragmentShader);
		GL20.glAttachShader(program, vertexShader);
		GL20.glAttachShader(program, fragmentShader);
		GL20.glBindAttribLocation(program, 0, "position");
		GL20.glBindAttribLocation(program, 1, "color");
		GL20.glLinkProgram(program);
		GL20.glValidateProgram(program);
		
	}
	@Subscribe
	public void update(UpdateEvent event){
		
	}
	@Subscribe
	public void render(RenderEvent event){
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LESS);
		GL20.glUseProgram(program);
		GL30.glBindVertexArray(vao);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL11.glDrawElements(GL11.GL_TRIANGLES, 3, GL11.GL_UNSIGNED_INT, 0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}
	private static StringBuilder getFile(String path){
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
}
