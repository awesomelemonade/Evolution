package lemon.engine.evolution;

import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.InitEvent;
import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.game.OpenSimplexNoise;
import lemon.engine.game.PlayerControls;
import lemon.engine.input.CursorPositionEvent;
import lemon.engine.input.KeyEvent;
import lemon.engine.input.MouseButtonEvent;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector;
import lemon.engine.render.AttributePointer;
import lemon.engine.render.DataArray;
import lemon.engine.render.ModelDataBuffer;
import lemon.engine.render.RawModel;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.render.UniformVariable;
import lemon.engine.toolbox.Toolbox;

public enum Game implements Listener {
	INSTANCE;
	
	private ShaderProgram program;
	private UniformVariable uniform_modelMatrix;
	private UniformVariable uniform_viewMatrix;
	private UniformVariable uniform_projectionMatrix;
	
	private Vector translation;
	private Vector rotation;
	
	private PlayerControls<Integer, Integer> controls;
	
	private RawModel model;
	
	private float[][] heights;
	private static final int SIZE = 40;
	private static final int ARRAY_SIZE = SIZE*SIZE+1;
	private static final float TILE_SIZE = 0.2f;
	
	public float[][] diamondSquareAlgorithm(float[][] grid, float... points){
		for(int i=0;i<points.length;i+=8){
			//diamond step
			
			//square step
		}
		return grid;
	}
	
	OpenSimplexNoise noise = new OpenSimplexNoise(new Random().nextLong()); //12094
	
	@Subscribe
	public void init(InitEvent event){
		System.out.println(noise.getSeed());
		heights = new float[ARRAY_SIZE][ARRAY_SIZE];
		
		heights[0][0] = (float) (Math.random()*10);
		heights[0][ARRAY_SIZE-1] = (float) (Math.random()*10);
		heights[ARRAY_SIZE-1][0] = (float) (Math.random()*10);
		heights[ARRAY_SIZE-1][ARRAY_SIZE-1] = (float) (Math.random()*10);
		
		heights = diamondSquareAlgorithm(heights, (ARRAY_SIZE+1)/2);
		
		for(int i=0;i<heights.length;++i){
			for(int j=0;j<heights[0].length;++j){
				heights[i][j] = (float) noise.eval(((double)i)/25.0, ((double)j)/25.0)*5f;
				//heights[i][j] = (float) (((float)i)/((float)heights.length)*8f*Math.random());
			}
		}
		
		ModelDataBuffer data = new ModelDataBuffer(6*(ARRAY_SIZE-1)*(ARRAY_SIZE-1));
		
		for(int i=0;i<ARRAY_SIZE-1;++i){
			for(int j=0;j<ARRAY_SIZE-1;++j){
				if((i+j)%2==0){
					data.addIndices(j*ARRAY_SIZE+i, j*ARRAY_SIZE+i+1, (j+1)*ARRAY_SIZE+i);
					data.addIndices(j*ARRAY_SIZE+i+1, (j+1)*ARRAY_SIZE+i, (j+1)*ARRAY_SIZE+i+1);
				}else{
					data.addIndices(j*ARRAY_SIZE+i, (j+1)*ARRAY_SIZE+i+1, (j+1)*ARRAY_SIZE+i);
					data.addIndices(j*ARRAY_SIZE+i, (j+1)*ARRAY_SIZE+i+1, j*ARRAY_SIZE+i+1);
				}
			}
		}
		
		DataArray array = new DataArray(ARRAY_SIZE*ARRAY_SIZE*7,
				new AttributePointer(0, 3, 7*4, 0),
				new AttributePointer(1, 4, 7*4, 3*4)
		);
		for(int i=0;i<heights.length;++i){
			for(int j=0;j<heights[0].length;++j){
				array.addData((i*TILE_SIZE)-((TILE_SIZE*heights.length)/2), heights[i][j], (j*TILE_SIZE)-((TILE_SIZE*heights[0].length)/2), (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
				//array.addData(((float)Math.random())-0.5f, ((float)Math.random())-0.5f, -1f, (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
			}
		}
		//array.addData(-0.5f, -0.5f, -1f, (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
		//array.addData(0.5f, -0.5f, -1f, (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
		//array.addData(0.5f, 0.5f, -1f, (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
		//array.addData(-0.5f, 0.5f, -1f, (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
		data.addDataArray(array);
		data.flip();
		model = new RawModel(data);
		
		
		program = new ShaderProgram(
			new int[]{0, 1},
			new String[]{"position", "color"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/vertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/fragmentShader"))
		);
		uniform_modelMatrix = program.getUniformVariable("modelMatrix");
		uniform_viewMatrix = program.getUniformVariable("viewMatrix");
		uniform_projectionMatrix = program.getUniformVariable("projectionMatrix");
		GL20.glUseProgram(program.getId());
		uniform_modelMatrix.loadMatrix(Matrix.getIdentity(4));
		translation = new Vector(0f, 10f, 0f);
		rotation = new Vector(0f, 0f, 0f);
		uniform_viewMatrix.loadMatrix(MathUtil.getRotationX(rotation.getX()).multiply(MathUtil.getRotationY(rotation.getY()).multiply(MathUtil.getRotationZ(rotation.getZ()))));
		uniform_projectionMatrix.loadMatrix(MathUtil.getPerspective(60f, MathUtil.getAspectRatio(event.getWindow()), 0.01f, 100f));
		GL20.glUseProgram(0);
		updateViewMatrix();
		
		controls = new PlayerControls<Integer, Integer>();
		controls.bindKey(GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_MOUSE_BUTTON_LEFT);
		controls.bindKey(GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_A);
		controls.bindKey(GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_D);
		controls.bindKey(GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_W);
		controls.bindKey(GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_S);
		controls.bindKey(GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_SPACE);
		controls.bindKey(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT);
	}
	private static final float PLAYER_SPEED = 0.05f;
	@Subscribe
	public void update(UpdateEvent event){
		if(controls.hasStates()){
			float angle = (rotation.getY()+90)*(((float)Math.PI)/180f);
			float sin = (float)Math.sin(angle);
			float cos = (float)Math.cos(angle);
			if(controls.getState(GLFW.GLFW_KEY_A)){
				translation.setX(translation.getX()-((float)(PLAYER_SPEED))*sin);
				translation.setZ(translation.getZ()-((float)(PLAYER_SPEED))*cos);
			}
			if(controls.getState(GLFW.GLFW_KEY_D)){
				translation.setX(translation.getX()+((float)(PLAYER_SPEED))*sin);
				translation.setZ(translation.getZ()+((float)(PLAYER_SPEED))*cos);
			}
			angle = rotation.getY()*(((float)Math.PI)/180f);
			sin = (float)Math.sin(angle);
			cos = (float)Math.cos(angle);
			if(controls.getState(GLFW.GLFW_KEY_W)){
				translation.setX(translation.getX()-((float)(PLAYER_SPEED))*sin);
				translation.setZ(translation.getZ()-((float)(PLAYER_SPEED))*cos);
			}
			if(controls.getState(GLFW.GLFW_KEY_S)){
				translation.setX(translation.getX()+((float)(PLAYER_SPEED))*sin);
				translation.setZ(translation.getZ()+((float)(PLAYER_SPEED))*cos);
			}
			if(controls.getState(GLFW.GLFW_KEY_SPACE)){
				translation.setY(translation.getY()+((float)(PLAYER_SPEED)));
			}
			if(controls.getState(GLFW.GLFW_KEY_LEFT_SHIFT)){
				translation.setY(translation.getY()-((float)(PLAYER_SPEED)));
			}
			updateViewMatrix();
		}
	}
	@Subscribe
	public void onKey(KeyEvent event){
		if(event.getAction()==GLFW.GLFW_PRESS){
			controls.setKeyState(event.getKey(), true);
		}
		if(event.getAction()==GLFW.GLFW_RELEASE){
			controls.setKeyState(event.getKey(), false);
		}
	}
	@Subscribe
	public void onMouse(MouseButtonEvent event){
		if(event.getAction()==GLFW.GLFW_PRESS){
			controls.setKeyState(event.getButton(), true);
		}
		if(event.getAction()==GLFW.GLFW_RELEASE){
			controls.setKeyState(event.getButton(), false);
		}
	}
	private double lastMouseX;
	private double lastMouseY;
	private double mouseX;
	private double mouseY;
	private static final float MOUSE_SENSITIVITY = 0.1f;
	@Subscribe
	public void onMousePosition(CursorPositionEvent event){
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		mouseX = event.getX();
		mouseY = event.getY();
		if(controls.getState(GLFW.GLFW_MOUSE_BUTTON_1)){
			rotation.setY((float) (rotation.getY()-(mouseX-lastMouseX)*MOUSE_SENSITIVITY));
			rotation.setX((float) (rotation.getX()-(mouseY-lastMouseY)*MOUSE_SENSITIVITY));
			if(rotation.getX()<-90){
				rotation.setX(-90);
			}
			if(rotation.getX()>90){
				rotation.setX(90);
			}
		}
		updateViewMatrix();
	}
	public void updateViewMatrix(){
		GL20.glUseProgram(program.getId());
		Vector translation = this.translation.invert();
		Vector rotation = this.rotation.invert();
		
		Matrix translationMatrix = MathUtil.getTranslation(translation);
		Matrix rotationMatrix = MathUtil.getRotationX(rotation.getX()).multiply(MathUtil.getRotationY(rotation.getY()).multiply(MathUtil.getRotationZ(rotation.getZ())));
		uniform_viewMatrix.loadMatrix(rotationMatrix.multiply(translationMatrix));
		GL20.glUseProgram(0);
	}
	@Subscribe
	public void render(RenderEvent event){
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		//GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
		GL20.glUseProgram(program.getId());
		
		model.render();
		
		GL20.glUseProgram(0);
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
