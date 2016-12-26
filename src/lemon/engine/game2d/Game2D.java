package lemon.engine.game2d;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.entity.Quad;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.evolution.CommonPrograms2D;
import lemon.engine.function.CubicBezierCurve;
import lemon.engine.input.KeyEvent;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector;
import lemon.engine.math.Vector2D;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;

public enum Game2D implements Listener {
	INSTANCE;
	private Box2D quad;
	private Matrix projectionMatrix;
	
	private CubicBezierCurve curve = new CubicBezierCurve(new Vector2D(0f, 0f), 
	//		new Vector2D(0.17f, 0.67f), new Vector2D(0.83f, 0.67f), new Vector2D(1f, 1f));
	//		new Vector2D(0.25f, 0.1f), new Vector2D(0.25f, 1f), new Vector2D(1f, 1f));
			new Vector2D(0.87f, -0.41f), new Vector2D(0.19f, 1.44f), new Vector2D(1f, 1f));
	
	public void start(long window){
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		int window_width = width.get();
		int window_height = height.get();
		projectionMatrix = MathUtil.getOrtho(window_width, window_height, -1f, 1f);
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		GL20.glUseProgram(0);
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		GL20.glUseProgram(0);
		quad = new Box2D(0f, 0, 100f, 100f);
		EventManager.INSTANCE.registerListener(this);
	}
	long time = -500000000;
	@Subscribe
	public void update(UpdateEvent event){
		time+=event.getDelta();
		float timeProgress = (float)(time/1000000000.0);
		if(timeProgress<0f){
			timeProgress = 0f;
		}
		if(timeProgress>1f){
			timeProgress = 1f;
		}
		Vector solved = curve.apply(timeProgress);
		quad.setX(solved.get(1)*500f);
	}
	@Subscribe
	public void render(RenderEvent event){
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX,
				MathUtil.getTranslation(new Vector3D(quad.getX()+500f, quad.getY()+500f, 0f))
				.multiply(MathUtil.getScalar(new Vector3D(50f, 50f, 1f))));
		Quad.COLORED_2D.render();
		GL20.glUseProgram(0);
	}
	@Subscribe
	public void onKey(KeyEvent event){
		if(event.getAction()==GLFW.GLFW_PRESS){
			
		}
		if(event.getAction()==GLFW.GLFW_RELEASE){
			if(event.getKey()==GLFW.GLFW_KEY_R){
				time = -500000000;
			}
		}
	}
}
