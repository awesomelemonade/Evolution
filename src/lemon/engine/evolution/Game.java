package lemon.engine.evolution;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import lemon.engine.control.InitEvent;
import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.frameBuffer.FrameBuffer;
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
import lemon.engine.terrain.TerrainGenerator;
import lemon.engine.texture.Texture;
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
	private static final int SIZE = 9;
	private static final int ARRAY_SIZE = SIZE*SIZE+1;
	private static final float TILE_SIZE = 1f; //0.2f
	
	private FrameBuffer frameBuffer;
	private Texture colorTexture;
	private Texture depthTexture;
	
	private ShaderProgram textureProgram;
	private UniformVariable uniform_textureModelMatrix;
	private UniformVariable uniform_textureViewMatrix;
	private UniformVariable uniform_textureProjectionMatrix;
	
	private RawModel quad;
	private Texture texture;
	
	private TerrainGenerator terrainGenerator;
	
	@Subscribe
	public void init(InitEvent event){
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(event.getWindow(), width, height);
		int window_width = width.get();
		int window_height = height.get();
		
		GL11.glViewport(0, 0, window_width, window_height);
		
		terrainGenerator = new TerrainGenerator(TILE_SIZE);
		
		heights = new float[ARRAY_SIZE][ARRAY_SIZE];
		
		for(int i=0;i<heights.length;++i){
			for(int j=0;j<heights[0].length;++j){
				//heights[i][j] = (float) (((float)i)/((float)heights.length)*8f*Math.random());
				heights[i][j] = terrainGenerator.generate(i, j);
			}
		}
		
		ModelDataBuffer data = new ModelDataBuffer(6*(ARRAY_SIZE-1)*(ARRAY_SIZE-1));
		
		//Defined Counter-Clock-Wise
		for(int i=0;i<ARRAY_SIZE-1;++i){
			for(int j=0;j<ARRAY_SIZE-1;++j){
				if((i+j)%2==0){
					data.addIndices(j*ARRAY_SIZE+i+1, j*ARRAY_SIZE+i, (j+1)*ARRAY_SIZE+i);
					data.addIndices((j+1)*ARRAY_SIZE+i+1, j*ARRAY_SIZE+i+1, (j+1)*ARRAY_SIZE+i);
				}else{
					data.addIndices((j+1)*ARRAY_SIZE+i, (j+1)*ARRAY_SIZE+i+1, j*ARRAY_SIZE+i);
					data.addIndices((j+1)*ARRAY_SIZE+i+1, j*ARRAY_SIZE+i+1, j*ARRAY_SIZE+i);
				}
			}
		}
		
		DataArray array = new DataArray(ARRAY_SIZE*ARRAY_SIZE*7, GL15.GL_STATIC_DRAW,
				new AttributePointer(0, 3, 7*4, 0),
				new AttributePointer(1, 4, 7*4, 3*4)
		);
		for(int i=0;i<heights.length;++i){
			for(int j=0;j<heights[0].length;++j){
				array.addData((i*TILE_SIZE)-((TILE_SIZE*heights.length)/2), heights[i][j], (j*TILE_SIZE)-((TILE_SIZE*heights[0].length)/5f), ((float)i)/((float)heights.length), ((float)j)/((float)heights[0].length), 1f-(heights[i][j])/0.8f, 1f);
				//array.addData((i*TILE_SIZE)-((TILE_SIZE*heights.length)/2), heights[i][j], (j*TILE_SIZE)-((TILE_SIZE*heights[0].length)/2), (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
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
		
		ModelDataBuffer quadData = new ModelDataBuffer(6);
		quadData.addIndices(0, 1, 2, 1, 2, 3);
		DataArray quadDataArray = new DataArray(4*5, GL15.GL_STATIC_DRAW,
				new AttributePointer(0, 3, 5*4, 0),
				new AttributePointer(1, 2, 5*4, 3*4)
		); //4 vertices; 5 floats per vertex
		quadDataArray.addData(
				-0.5f, 0.5f, -1f, 0f, 1f,
				-0.5f, -0.5f, -1f, 0f, 0f,
				0.5f, 0.5f, -1f, 1f, 1f,
				0.5f, -0.5f, -1f, 1f, 0f
		);
		/*quadDataArray.addData(
				-1f, 0f, -1f, 0f, 1f,
				-1f, 0f, 1f, 0f, 0f,
				1f, 0f, -1f, 1f, 1f,
				1f, 0f, 1f, 1f, 0f
		);*/
		quadData.addDataArray(quadDataArray);
		quadData.flip();
		quad = new RawModel(quadData);
		
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
		translation = new Vector(0f, 0f, 0f);
		rotation = new Vector(0f, 0f, 0f);
		uniform_projectionMatrix.loadMatrix(MathUtil.getPerspective(60f, MathUtil.getAspectRatio(event.getWindow()), 0.01f, 100f));
		GL20.glUseProgram(0);
		updateViewMatrix(program, uniform_viewMatrix);
		
		textureProgram = new ShaderProgram(
			new int[]{0, 1},
			new String[]{"position", "textureCoords"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/textureVertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/textureFragmentShader"))
		);
		uniform_textureModelMatrix = textureProgram.getUniformVariable("modelMatrix");
		uniform_textureViewMatrix = textureProgram.getUniformVariable("viewMatrix");
		uniform_textureProjectionMatrix = textureProgram.getUniformVariable("projectionMatrix");
		GL20.glUseProgram(textureProgram.getId());
		uniform_textureModelMatrix.loadMatrix(MathUtil.getTranslation(new Vector(0f, 10f, 0f)).multiply(MathUtil.getScalar(new Vector(12f, 12f, 12f))));
		uniform_textureProjectionMatrix.loadMatrix(MathUtil.getPerspective(60f, MathUtil.getAspectRatio(event.getWindow()), 0.01f, 100f));
		GL20.glUseProgram(0);
		updateViewMatrix(textureProgram, uniform_textureViewMatrix);
		
		texture = new Texture();
		try {
			BufferedImage image = ImageIO.read(new File("res/FTL.jpg"));
			texture.loadByteBuffer(image.getWidth(), image.getHeight(), Toolbox.toByteBuffer(image));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		frameBuffer = new FrameBuffer();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer.getId());
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		colorTexture = new Texture();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.getId());
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, window_width, window_height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.getId(), 0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		depthTexture = new Texture();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.getId());
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, window_width, window_height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer)null);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthTexture.getId(), 0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		controls = new PlayerControls<Integer, Integer>();
		controls.bindKey(GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_MOUSE_BUTTON_LEFT);
		controls.bindKey(GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_A);
		controls.bindKey(GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_D);
		controls.bindKey(GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_W);
		controls.bindKey(GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_S);
		controls.bindKey(GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_SPACE);
		controls.bindKey(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT);
	}
	private static final float PLAYER_SPEED = 0.15f;
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
		}
		updateViewMatrix(program, uniform_viewMatrix);
		updateViewMatrix(textureProgram, uniform_textureViewMatrix);
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
		updateViewMatrix(program, uniform_viewMatrix);
		updateViewMatrix(textureProgram, uniform_textureViewMatrix);
	}
	public void updateViewMatrix(ShaderProgram program, UniformVariable variable){
		GL20.glUseProgram(program.getId());
		Vector translation = this.translation.invert();
		Vector rotation = this.rotation.invert();
		
		Matrix translationMatrix = MathUtil.getTranslation(translation);
		Matrix rotationMatrix = MathUtil.getRotationX(rotation.getX()).multiply(MathUtil.getRotationY(rotation.getY()).multiply(MathUtil.getRotationZ(rotation.getZ())));
		variable.loadMatrix(rotationMatrix.multiply(translationMatrix));
		GL20.glUseProgram(0);
	}
	@Subscribe
	public void render(RenderEvent event){
		
		renderModel();
		renderTexture();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer.getId());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		renderModel();
		renderTexture();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
	}
	public void renderTexture(){
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.getId());
		
		GL20.glUseProgram(textureProgram.getId());
		quad.render();
		GL20.glUseProgram(0);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	public void renderModel(){
		//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		//GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL13.glActiveTexture(GL13.GL_TEXTURE0);
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
		
		GL20.glUseProgram(program.getId());
		
		model.render();
		
		GL20.glUseProgram(0);
		
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
