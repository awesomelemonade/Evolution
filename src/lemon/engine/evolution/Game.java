package lemon.engine.evolution;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

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
import lemon.engine.entity.Segment;
import lemon.engine.entity.Skybox;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.game.Player;
import lemon.engine.game.PlayerControls;
import lemon.engine.input.CursorPositionEvent;
import lemon.engine.input.KeyEvent;
import lemon.engine.input.MouseButtonEvent;
import lemon.engine.input.MouseScrollEvent;
import lemon.engine.loader.SkyboxLoader;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector;
import lemon.engine.render.Renderable;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.render.UniformVariable;
import lemon.engine.terrain.TerrainGenerator;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.texture.TextureData;
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
	
	private Texture texture;
	
	private TerrainLoader terrainLoader;
	
	private List<Vector> entities;
	
	private List<Renderable> segments;
	
	public TerrainLoader getTerrainLoader(){
		if(terrainLoader==null){
			terrainLoader = new TerrainLoader(new TerrainGenerator(0), Math.max((int) (500f/TILE_SIZE), 2), Math.max((int) (500f/TILE_SIZE), 2));
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
		Quad.INSTANCE.init();
		terrain = new HeightMap(terrainLoader.getTerrain(), TILE_SIZE);
		segments = new ArrayList<Renderable>();
		
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
		uniform_colorModelMatrix.loadMatrix(Matrix.getIdentity(4));
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
		
		texture = new Texture();
		try {
			BufferedImage image = ImageIO.read(new File("res/FTL.jpg"));
			texture.load(new TextureData(image));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
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
		
		controls = new PlayerControls<Integer, Integer>();
		controls.bindKey(GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_MOUSE_BUTTON_LEFT);
		controls.bindKey(GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_A);
		controls.bindKey(GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_D);
		controls.bindKey(GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_W);
		controls.bindKey(GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_S);
		controls.bindKey(GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_SPACE);
		controls.bindKey(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT);
		controls.bindKey(GLFW.GLFW_KEY_T, GLFW.GLFW_KEY_T);
		
		entities = new ArrayList<Vector>();
		for(int x=-4;x<=4;x+=1){
			for(int z=-4;z<=4;z+=1){
				entities.add(new Vector(x, 0, z));
			}
		}
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
			if(controls.getState(GLFW.GLFW_KEY_T)){
				/*int x = (int)(Math.random()*terrain.getWidth());
				int y = (int)(Math.random()*terrain.getHeight());*/
				float x = toArrayCoord(player.getCamera().getPosition().getX(), terrain.getWidth());
				float z = toArrayCoord(player.getCamera().getPosition().getZ(), terrain.getHeight());
				int intX = (int)x;
				int intZ = (int)z;
				if(intX>0&&intZ>0&&intX<terrain.getWidth()&&intZ<terrain.getHeight()){
					Vector vector = new Vector(player.getCamera().getPosition());
					vector.setY(getHeight(x, z));
					entities.add(vector);
					//terrain.set(x, y, terrain.get(x, y)+((float)Math.random()));
					//terrain.update();
				}
				segments.add(new Segment(new Vector(0, 0, 0), player.getCamera().getPosition()));
			}
		}
		player.getVelocity().setX(player.getVelocity().getX()*friction);
		player.getVelocity().setY(player.getVelocity().getY()*friction);
		player.getVelocity().setZ(player.getVelocity().getZ()*friction);
		player.update(event);
		updateViewMatrix(colorProgram, uniform_colorViewMatrix);
		updateViewMatrix(textureProgram, uniform_textureViewMatrix);
		updateCubeMapMatrix(cubemapProgram, uniform_cubemapViewMatrix);
		for(Vector vector: entities){
			vector.setX(vector.getX()+((float)Math.random()-0.5f));
			vector.setZ(vector.getZ()+((float)Math.random()-0.5f));
			if(vector.getX()<-49.5){
				vector.setX(-49.5f);
			}
			if(vector.getX()>49.5){
				vector.setX(49.5f);
			}
			if(vector.getZ()<-49.5){
				vector.setZ(-49.5f);
			}
			if(vector.getZ()>49.5){
				vector.setZ(49.5f);
			}
			vector.setY(getHeight(toArrayCoord(vector.getX(), terrain.getWidth()), toArrayCoord(vector.getZ(), terrain.getHeight())));
		}
		if(Math.random()<0.022){
			List<Vector> born = new ArrayList<Vector>();
			for(Vector vector: entities){
				if(Math.random()<0.2){
					born.add(new Vector(vector));
				}
			}
			for(Vector vector: born){
				entities.add(vector);
			}
		}
		List<Vector> dead = new ArrayList<Vector>();
		for(Vector vector: entities){
			if(Math.random()*20<vector.getY()){
				dead.add(vector);
			}
		}
		for(Vector vector: entities){
			if(Math.random()>(1f/((float)(near(vector, entities, 2f)))+0.64f)){
				dead.add(vector);
			}
		}
		for(Vector vector: dead){
			entities.remove(vector);
		}
	}
	public float near(Vector vector, List<Vector> entities, float distance){
		float count = 0;
		for(Vector v: entities){
			count+=Math.max(distance-vector.getDistance(v), 0);
		}
		return count;
	}
	public float toArrayCoord(float coord, float width){
		return coord/TILE_SIZE+width/2f-TILE_SIZE/2f;
	}
	public float getHeight(float x, float z){
		int intX = (int)x;
		int intZ = (int)z;
		float fracX = x-intX;
		float fracZ = z-intZ;
		if(intX>=0&&intZ>=0&&intX+1<terrain.getWidth()&&intZ+1<terrain.getHeight()){
			return lerp(lerp(terrain.get(intX, intZ), terrain.get(intX+1, intZ), fracX), lerp(terrain.get(intX, intZ+1), terrain.get(intX+1, intZ+1), fracX), fracZ);
		}else if(intX>=0&&intZ>=0&&intX<terrain.getWidth()&&intZ+1<terrain.getHeight()){
			return lerp(terrain.get(intX, intZ), terrain.get(intX, intZ+1), fracZ);
		}else if(intX>=0&&intZ>=0&&intX+1<terrain.getWidth()&&intZ<terrain.getHeight()){
			return lerp(terrain.get(intX, intZ), terrain.get(intX+1, intZ), fracX);
		}else if(intX>=0&&intZ>=0&&intX<terrain.getWidth()&&intZ<terrain.getHeight()){
			return terrain.get(intX, intZ);
		}else{
			return 0;
		}
	}
	public float lerp(float a, float b, float x){
		return a*(1-x)+b*x;
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
		for(Vector vector: entities){
			renderQuad(vector);
		}
		for(Renderable renderable: segments){
			renderSegment(renderable);
		}
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL20.glUseProgram(postProcessingProgram.getId());
		Quad.INSTANCE.render();
		GL20.glUseProgram(0);
		renderFPS();
		num+=1f;
	}
	float num = 0;
	public void renderQuad(Vector vector){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL13.glActiveTexture(TextureBank.REUSE.getBind());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.getId());
		GL20.glUseProgram(textureProgram.getId());
		uniform_textureModelMatrix.loadMatrix(MathUtil.getTranslation(vector).multiply(MathUtil.getRotationY(num)).multiply(MathUtil.getScalar(new Vector(1f, 1f, 1f))));
		Quad.INSTANCE.render();
		GL20.glUseProgram(0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	public void renderSegment(Renderable renderable){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL20.glUseProgram(colorProgram.getId());
		renderable.render();
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	public void renderHeightMap(){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL20.glUseProgram(colorProgram.getId());
		terrain.render();
		GL20.glUseProgram(0);
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
		uniform_lineColor.loadVector(new Vector(1f, 0f, 0f));
		uniform_lineSpacing.loadFloat(2f/(benchmarker.getLineGraph("updateData").getSize()-1));
		benchmarker.getLineGraph("updateData").render();
		uniform_lineColor.loadVector(new Vector(1f, 1f, 0f));
		uniform_lineSpacing.loadFloat(2f/(benchmarker.getLineGraph("renderData").getSize()-1));
		benchmarker.getLineGraph("renderData").render();
		uniform_lineColor.loadVector(new Vector(1f, 0f, 1f));
		uniform_lineSpacing.loadFloat(2f/(benchmarker.getLineGraph("fpsData").getSize()-1));
		benchmarker.getLineGraph("fpsData").render();
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_BLEND);
	}
	@Subscribe
	public void onBenchmark(BenchmarkEvent event){
		benchmarker.benchmark(event.getBenchmark());
	}
}
