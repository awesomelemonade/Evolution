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
import lemon.engine.evolution.CommonPrograms2D;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.Color;

public enum Game2D implements Listener {
	INSTANCE;
	private Quad2D quad;
	private Matrix projectionMatrix;
	
	public void start(long window){
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		int window_width = width.get();
		int window_height = height.get();
		projectionMatrix = MathUtil.getOrtho(window_width, window_height, -1f, 1f);
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.TRANSFORMATION_MATRIX, Matrix.IDENTITY_4);
		GL20.glUseProgram(0);
		quad = new Quad2D(new Box2D(0f, 0, 100f, 100f), new Color(1f, 0f, 0f));
		EventManager.INSTANCE.registerListener(this);
	}
	@Subscribe
	public void update(UpdateEvent event){
		
	}
	@Subscribe
	public void render(RenderEvent event){
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		quad.render();
		GL20.glUseProgram(0);
	}
}
