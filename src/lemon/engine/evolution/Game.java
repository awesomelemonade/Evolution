package lemon.engine.evolution;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.entity.HeightMap;
import lemon.engine.entity.LineGraph;
import lemon.engine.entity.Quad;
import lemon.engine.entity.Skybox;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.game.CollisionHandler;
import lemon.engine.game.Platform;
import lemon.engine.game.Player;
import lemon.engine.game.PlayerControls;
import lemon.engine.game.StandardControls;
import lemon.engine.input.CursorPositionEvent;
import lemon.engine.input.MouseScrollEvent;
import lemon.engine.loader.SkyboxLoader;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.render.UniformVariable;
import lemon.engine.terrain.TerrainGenerator;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.time.BenchmarkEvent;
import lemon.engine.time.Benchmarker;
import lemon.engine.toolbox.Toolbox;

public enum Game implements Listener {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(Game.class.getName());
	
	private ShaderProgram colorProgram;
	private UniformVariable uniform_colorModelMatrix;
	private UniformVariable uniform_colorViewMatrix;
	private UniformVariable uniform_colorProjectionMatrix;
	
	private Player player;
	
	private PlayerControls<Integer, Integer> controls;
	
	private HeightMap terrain;
	
	private static final float TILE_SIZE = 0.2f; //0.2f 1f
	
	private FrameBuffer frameBuffer;
	private Texture colorTexture;
	private Texture depthTexture;
	
	private Texture skyboxTexture;
	
	private ShaderProgram postProcessingProgram;
	private UniformVariable uniform_colorSampler;
	private UniformVariable uniform_depthSampler;
	
	private ShaderProgram textureProgram;
	private UniformVariable uniform_textureModelMatrix;
	private UniformVariable uniform_textureViewMatrix;
	private UniformVariable uniform_textureProjectionMatrix;
	
	private ShaderProgram cubemapProgram;
	private UniformVariable uniform_cubemapViewMatrix;
	private UniformVariable uniform_cubemapProjectionMatrix;
	private UniformVariable uniform_cubemapSampler;
	
	private ShaderProgram lineProgram;
	private UniformVariable uniform_lineColor;
	private UniformVariable uniform_lineSpacing;
	private UniformVariable uniform_lineIndex;
	private UniformVariable uniform_lineTotal;
	private UniformVariable uniform_lineAlpha;
	
	private Benchmarker benchmarker;
	
	private TerrainLoader terrainLoader;
	
	private CollisionHandler collisionHandler;
	
	private List<Platform> platforms;
	
	public TerrainLoader getTerrainLoader(){
		if(terrainLoader==null){
			terrainLoader = new TerrainLoader(new TerrainGenerator(0), Math.max((int) (100f/TILE_SIZE), 2), Math.max((int) (100f/TILE_SIZE), 2));
		}
		return terrainLoader;
	}
	
	public void init(long window){
		logger.log(Level.FINE, "Initializing");
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		int window_width = width.get();
		int window_height = height.get();
		
		GL11.glViewport(0, 0, window_width, window_height);
		
		Skybox.INSTANCE.init();
		Quad.TEXTURED.init();
		Quad.COLORED.init();
		terrain = new HeightMap(terrainLoader.getTerrain(), TILE_SIZE);
		
		benchmarker = new Benchmarker();
		benchmarker.put("updateData", new LineGraph(1000, 100000000));
		benchmarker.put("renderData", new LineGraph(1000, 100000000));
		benchmarker.put("fpsData", new LineGraph(1000, 100));
		
		player = new Player(new Projection(60f, ((float)window_width)/((float)window_height), 0.01f, 1000f));
		Matrix projectionMatrix = player.getCamera().getProjectionMatrix();
		
		colorProgram = new ShaderProgram(
			new int[]{0, 1},
			new String[]{"position", "color"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/colorVertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/colorFragmentShader"))
		);
		uniform_colorModelMatrix = colorProgram.getUniformVariable("modelMatrix");
		uniform_colorViewMatrix = colorProgram.getUniformVariable("viewMatrix");
		uniform_colorProjectionMatrix = colorProgram.getUniformVariable("projectionMatrix");
		GL20.glUseProgram(colorProgram.getId());
		uniform_colorModelMatrix.loadMatrix(Matrix.IDENTITY_4);
		uniform_colorProjectionMatrix.loadMatrix(projectionMatrix);
		GL20.glUseProgram(0);
		updateViewMatrix(colorProgram, uniform_colorViewMatrix);
		
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
		uniform_textureModelMatrix.loadMatrix(MathUtil.getTranslation(new Vector(0f, 0f, 0f)).multiply(MathUtil.getScalar(new Vector(1f, 1f, 1f))));
		uniform_textureProjectionMatrix.loadMatrix(projectionMatrix);
		GL20.glUseProgram(0);
		updateViewMatrix(textureProgram, uniform_textureViewMatrix);
		
		cubemapProgram = new ShaderProgram(
			new int[]{0},
			new String[]{"position"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/cubemapVertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/cubemapFragmentShader"))
		);
		uniform_cubemapViewMatrix = cubemapProgram.getUniformVariable("viewMatrix");
		uniform_cubemapProjectionMatrix = cubemapProgram.getUniformVariable("projectionMatrix");
		uniform_cubemapSampler = cubemapProgram.getUniformVariable("cubemapSampler");
		GL20.glUseProgram(cubemapProgram.getId());
		uniform_cubemapProjectionMatrix.loadMatrix(projectionMatrix);
		uniform_cubemapSampler.loadInt(TextureBank.SKYBOX.getId());
		GL20.glUseProgram(0);
		updateViewMatrix(cubemapProgram, uniform_cubemapViewMatrix);
		
		lineProgram = new ShaderProgram(
				new int[]{0, 1},
				new String[]{"id", "value"},
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/lineVertexShader")),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/lineFragmentShader"))
		);
		uniform_lineColor = lineProgram.getUniformVariable("color");
		uniform_lineSpacing = lineProgram.getUniformVariable("spacing");
		uniform_lineIndex = lineProgram.getUniformVariable("index");
		uniform_lineTotal = lineProgram.getUniformVariable("total");
		uniform_lineAlpha = lineProgram.getUniformVariable("alpha");
		GL20.glUseProgram(lineProgram.getId());
		uniform_lineIndex.loadInt(0);
		uniform_lineTotal.loadInt(0);
		uniform_lineAlpha.loadFloat(1f);
		GL20.glUseProgram(0);
		
		postProcessingProgram = new ShaderProgram(
				new int[]{0, 1},
				new String[]{"position", "textureCoords"},
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/postVertexShader")),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/postFragmentShader"))
		);
		uniform_colorSampler = postProcessingProgram.getUniformVariable("colorSampler");
		uniform_depthSampler = postProcessingProgram.getUniformVariable("depthSampler");
		GL20.glUseProgram(postProcessingProgram.getId());
		uniform_colorSampler.loadInt(TextureBank.COLOR.getId());
		uniform_depthSampler.loadInt(TextureBank.DEPTH.getId());
		GL20.glUseProgram(0);
		
		frameBuffer = new FrameBuffer();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer.getId());
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		colorTexture = new Texture();
		GL13.glActiveTexture(TextureBank.COLOR.getBind());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.getId());
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, window_width, window_height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.getId(), 0);
		depthTexture = new Texture();
		GL13.glActiveTexture(TextureBank.DEPTH.getBind());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.getId());
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, window_width, window_height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer)null);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthTexture.getId(), 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		skyboxTexture = new Texture();
		GL13.glActiveTexture(TextureBank.SKYBOX.getBind());
		skyboxTexture.load(new SkyboxLoader(new File("res/darkskies/"), new File("res/darkskies/darkskies.cfg")).load());
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, skyboxTexture.getId());
		GL13.glActiveTexture(TextureBank.REUSE.getBind());
		
		controls = new StandardControls();
		controls.bindKey(GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_MOUSE_BUTTON_LEFT);
		controls.bindKey(GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_A);
		controls.bindKey(GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_D);
		controls.bindKey(GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_W);
		controls.bindKey(GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_S);
		controls.bindKey(GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_SPACE);
		controls.bindKey(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT);
		controls.bindKey(GLFW.GLFW_KEY_T, GLFW.GLFW_KEY_T);
		
		collisionHandler = new CollisionHandler();
		collisionHandler.addCollidable(player);
		platforms = new ArrayList<Platform>();
		platforms.add(new Platform(new Vector(10f, 0f, 10f)));
		for(Platform platform: platforms){
			collisionHandler.addCollidable(platform);
		}
		
		EventManager.INSTANCE.registerListener(this);
	}
	private static float friction = 0.98f;
	private static float maxSpeed = 0.03f;
	private static float playerSpeed = maxSpeed-maxSpeed*friction;
	@Subscribe
	public void update(UpdateEvent event){
		if(controls.hasStates()){
			float angle = (player.getCamera().getRotation().getY()+90)*(((float)Math.PI)/180f);
			float sin = (float)Math.sin(angle);
			float cos = (float)Math.cos(angle);
			if(controls.getState(GLFW.GLFW_KEY_A)){
				player.getVelocity().setX(player.getVelocity().getX()-((float)(playerSpeed))*sin);
				player.getVelocity().setZ(player.getVelocity().getZ()-((float)(playerSpeed))*cos);
			}
			if(controls.getState(GLFW.GLFW_KEY_D)){
				player.getVelocity().setX(player.getVelocity().getX()+((float)(playerSpeed))*sin);
				player.getVelocity().setZ(player.getVelocity().getZ()+((float)(playerSpeed))*cos);
			}
			angle = player.getCamera().getRotation().getY()*(((float)Math.PI)/180f);
			sin = (float)Math.sin(angle);
			cos = (float)Math.cos(angle);
			if(controls.getState(GLFW.GLFW_KEY_W)){
				player.getVelocity().setX(player.getVelocity().getX()-((float)(playerSpeed))*sin);
				player.getVelocity().setZ(player.getVelocity().getZ()-((float)(playerSpeed))*cos);
			}
			if(controls.getState(GLFW.GLFW_KEY_S)){
				player.getVelocity().setX(player.getVelocity().getX()+((float)(playerSpeed))*sin);
				player.getVelocity().setZ(player.getVelocity().getZ()+((float)(playerSpeed))*cos);
			}
			if(controls.getState(GLFW.GLFW_KEY_SPACE)){
				player.getVelocity().setY(player.getVelocity().getY()+((float)(playerSpeed)));
			}
			if(controls.getState(GLFW.GLFW_KEY_LEFT_SHIFT)){
				player.getVelocity().setY(player.getVelocity().getY()-((float)(playerSpeed)));
			}
		}
		player.getVelocity().setX(player.getVelocity().getX()*friction);
		player.getVelocity().setY(player.getVelocity().getY()*friction);
		player.getVelocity().setZ(player.getVelocity().getZ()*friction);
		
		collisionHandler.update(event);
		
		player.update(event);
		updateViewMatrix(colorProgram, uniform_colorViewMatrix);
		updateViewMatrix(textureProgram, uniform_textureViewMatrix);
		updateCubeMapMatrix(cubemapProgram, uniform_cubemapViewMatrix);
	}
	@Subscribe
	public void onMouseScroll(MouseScrollEvent event){
		playerSpeed+=(float)(event.getYOffset()/100f);
		if(playerSpeed<0){
			playerSpeed = 0;
		}
		player.getCamera().getProjection().setFov(player.getCamera().getProjection().getFov()+((float)(event.getYOffset()/100f)));
		updateProjectionMatrix(colorProgram, uniform_colorProjectionMatrix);
		updateProjectionMatrix(textureProgram, uniform_textureProjectionMatrix);
		updateProjectionMatrix(cubemapProgram, uniform_cubemapProjectionMatrix);
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
			player.getCamera().getRotation().setY((float) (player.getCamera().getRotation().getY()-(mouseX-lastMouseX)*MOUSE_SENSITIVITY));
			player.getCamera().getRotation().setX((float) (player.getCamera().getRotation().getX()-(mouseY-lastMouseY)*MOUSE_SENSITIVITY));
			if(player.getCamera().getRotation().getX()<-90){
				player.getCamera().getRotation().setX(-90);
			}
			if(player.getCamera().getRotation().getX()>90){
				player.getCamera().getRotation().setX(90);
			}
		}
		updateViewMatrix(colorProgram, uniform_colorViewMatrix);
		updateViewMatrix(textureProgram, uniform_textureViewMatrix);
		updateCubeMapMatrix(cubemapProgram, uniform_cubemapViewMatrix);
	}
	public void updateViewMatrix(ShaderProgram program, UniformVariable variable){
		GL20.glUseProgram(program.getId());
		variable.loadMatrix(player.getCamera().getInvertedRotationMatrix().multiply(player.getCamera().getInvertedTranslationMatrix()));
		GL20.glUseProgram(0);
	}
	public void updateCubeMapMatrix(ShaderProgram program, UniformVariable variable){
		GL20.glUseProgram(program.getId());
		variable.loadMatrix(player.getCamera().getInvertedRotationMatrix());
		GL20.glUseProgram(0);
	}
	public void updateProjectionMatrix(ShaderProgram program, UniformVariable variable){
		GL20.glUseProgram(program.getId());
		variable.loadMatrix(player.getCamera().getProjectionMatrix());
		GL20.glUseProgram(0);
	}
	@Subscribe
	public void render(RenderEvent event){
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer.getId());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glDepthMask(false);
		renderSkybox();
		GL11.glDepthMask(true);
		renderHeightMap();
		renderPlatforms();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL20.glUseProgram(postProcessingProgram.getId());
		Quad.TEXTURED.render();
		GL20.glUseProgram(0);
		renderFPS();
	}
	public void renderHeightMap(){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL20.glUseProgram(colorProgram.getId());
		uniform_colorModelMatrix.loadMatrix(Matrix.IDENTITY_4);
		terrain.render();
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	public void renderPlatforms(){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		for(Platform platform: platforms){
			GL20.glUseProgram(colorProgram.getId());
			uniform_colorModelMatrix.loadMatrix(MathUtil.getTranslation(platform.getPosition()).multiply(MathUtil.getRotationX(90f)));
			Quad.COLORED.render();
			GL20.glUseProgram(0);
		}
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	public void renderSkybox(){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL20.glUseProgram(cubemapProgram.getId());
		Skybox.INSTANCE.render();
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_BLEND);
	}
	public void renderFPS(){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL20.glUseProgram(lineProgram.getId());
		byte color = 1; //Not Black
		for(String benchmarker: this.benchmarker.getNames()){
			uniform_lineColor.loadVector(new Vector((((color&0x01)!=0)?1f:0f), (((color&0x02)!=0)?1f:0f), (((color&0x04)!=0)?1f:0f)));
			uniform_lineSpacing.loadFloat(2f/(this.benchmarker.getLineGraph(benchmarker).getSize()-1));
			this.benchmarker.getLineGraph(benchmarker).render();
			color++;
		}
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_BLEND);
	}
	@Subscribe
	public void onBenchmark(BenchmarkEvent event){
		benchmarker.benchmark(event.getBenchmark());
	}
}
