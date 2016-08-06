package lemon.engine.game2d;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.control.WindowInitEvent;

import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.game.PlayerControls;
import lemon.engine.game.StandardControls;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.render.UniformVariable;
import lemon.engine.toolbox.Toolbox;

public enum Game2D implements Listener {
	INSTANCE;
	private ShaderProgram shaderProgram;
	private UniformVariable	uniform_projectionMatrix;
	private UniformVariable uniform_transformationMatrix;
	private Matrix projectionMatrix;
	
	private static final float PLAYER_SPEED = 5f;
	private static final float PLAYER_FRICTION = 0.5f;
	private static final float PLAYER_DELTA_MODIFIER = 0.00000001f;
	private Player2D player;
	private PlayerControls<Integer, Integer> controls;
	
	@Subscribe
	public void load(WindowInitEvent event){
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(event.getWindow(), width, height);
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
		uniform_transformationMatrix.loadMatrix(Matrix.getIdentity(4));
		GL20.glUseProgram(0);
		
		player = new Player2D();
		controls = new StandardControls();
		controls.bindKey(GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_A);
		controls.bindKey(GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_D);
		controls.bindKey(GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_W);
		controls.bindKey(GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_S);
	}
	@Subscribe
	public void update(UpdateEvent event){
		if(controls.hasStates()){
			if(controls.getState(GLFW.GLFW_KEY_W)){
				player.getVelocity().setY(player.getVelocity().getY()+PLAYER_SPEED*event.getDelta()*PLAYER_DELTA_MODIFIER);
			}
			if(controls.getState(GLFW.GLFW_KEY_A)){
				player.getVelocity().setX(player.getVelocity().getX()-PLAYER_SPEED*event.getDelta()*PLAYER_DELTA_MODIFIER);
			}
			if(controls.getState(GLFW.GLFW_KEY_S)){
				player.getVelocity().setY(player.getVelocity().getY()-PLAYER_SPEED*event.getDelta()*PLAYER_DELTA_MODIFIER);
			}
			if(controls.getState(GLFW.GLFW_KEY_D)){
				player.getVelocity().setX(player.getVelocity().getX()+PLAYER_SPEED*event.getDelta()*PLAYER_DELTA_MODIFIER);
			}
		}
		player.getVelocity().setX((float) (player.getVelocity().getX()*Math.pow(PLAYER_FRICTION, event.getDelta()*PLAYER_DELTA_MODIFIER)));
		player.getVelocity().setY((float) (player.getVelocity().getY()*Math.pow(PLAYER_FRICTION, event.getDelta()*PLAYER_DELTA_MODIFIER)));
		player.update(event);
	}
	@Subscribe
	public void render(RenderEvent event){
		GL20.glUseProgram(shaderProgram.getId());
		uniform_transformationMatrix.loadMatrix(MathUtil.getTranslation(player.getPosition()));
		player.render();
		GL20.glUseProgram(0);
	}
}
