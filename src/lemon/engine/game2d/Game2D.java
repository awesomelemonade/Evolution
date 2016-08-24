package lemon.engine.game2d;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.render.UniformVariable;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Toolbox;

public enum Game2D implements Listener {
	INSTANCE;
	private ShaderProgram shaderProgram;
	private UniformVariable	uniform_projectionMatrix;
	private UniformVariable uniform_transformationMatrix;
	private Quad2D player;
	private Matrix projectionMatrix;
	
	public void start(long window){
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		int window_width = width.get();
		int window_height = height.get();
		projectionMatrix = MathUtil.getOrtho(window_width, window_height, -1f, 1f);
		shaderProgram = new ShaderProgram(
				new int[]{0, 1},
				new String[]{"position", "color"},
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/colorVertexShader")),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/colorFragmentShader"))
		);
		uniform_projectionMatrix = shaderProgram.getUniformVariable("projectionMatrix");
		uniform_transformationMatrix = shaderProgram.getUniformVariable("transformationMatrix");
		GL20.glUseProgram(shaderProgram.getId());
		uniform_projectionMatrix.loadMatrix(projectionMatrix);
		uniform_transformationMatrix.loadMatrix(Matrix.IDENTITY_4);
		GL20.glUseProgram(0);
		player = new Quad2D(new Box2D(0f, 0, 20f, 50f), new Color(1f, 0f, 0f));
		EventManager.INSTANCE.registerListener(this);
	}
	@Subscribe
	public void update(UpdateEvent event){
		
	}
	@Subscribe
	public void render(RenderEvent event){
		GL20.glUseProgram(shaderProgram.getId());
		player.render();
		GL20.glUseProgram(0);
	}
}
