package lemon.evolution;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import lemon.engine.function.LineLineIntersection;
import lemon.engine.function.MollerTrumbore;
import lemon.engine.function.MurmurHash;
import lemon.engine.function.PerlinNoise;
import lemon.engine.function.RaySphereIntersection;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.Line;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Projection;
import lemon.engine.math.Sphere;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;
import lemon.engine.render.Renderable;
import lemon.evolution.destructible.beta.MarchingCube;
import lemon.evolution.destructible.beta.ScalarField;
import lemon.evolution.physicsbeta.Collision;
import lemon.evolution.physicsbeta.CollisionPacket;
import lemon.evolution.puzzle.PuzzleBall;
import lemon.evolution.puzzle.PuzzleGrid;
import lemon.evolution.util.ShaderProgramHolder;
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
import lemon.engine.game.Player;
import lemon.engine.input.CursorPositionEvent;
import lemon.engine.input.KeyEvent;
import lemon.engine.input.MouseButtonEvent;
import lemon.engine.input.MouseScrollEvent;
import lemon.engine.loader.SkyboxLoader;
import lemon.engine.render.MatrixType;
import lemon.engine.render.ShaderProgram;
import lemon.engine.terrain.TerrainGenerator;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.time.BenchmarkEvent;
import lemon.engine.time.Benchmarker;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.util.CommonPrograms3D;

public enum Game implements Listener {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(Game.class.getName());

	private Player player;

	private HeightMap terrain;

	private static final float TILE_SIZE = 0.5f; // 0.2f 1f

	private FrameBuffer frameBuffer;
	private Texture colorTexture;
	private Texture depthTexture;

	private Texture skyboxTexture;

	private Benchmarker benchmarker;

	private TerrainLoader terrainLoader;

	public TerrainLoader getTerrainLoader() {
		if (terrainLoader == null) {
			terrainLoader = new TerrainLoader(new TerrainGenerator(), Math.max((int) (500f / TILE_SIZE), 2),
					Math.max((int) (500f / TILE_SIZE), 2));
		}
		return terrainLoader;
	}

	@Override
	public void onRegister() {
		logger.log(Level.FINE, "Initializing");
		//GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, height);
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

		player = new Player(new Projection(MathUtil.toRadians(60f),
				((float) window_width) / ((float) window_height), 0.01f, 1000f));

		CommonProgramsSetup.setup2D();
		CommonProgramsSetup.setup3D(player.getCamera().getProjectionMatrix());

		updateViewMatrix(CommonPrograms3D.COLOR);
		updateViewMatrix(CommonPrograms3D.TEXTURE);
		updateViewMatrix(CommonPrograms3D.CUBEMAP);

		frameBuffer = new FrameBuffer();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer.getId());
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		colorTexture = new Texture();
		GL13.glActiveTexture(TextureBank.COLOR.getBind());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.getId());
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, window_width, window_height, 0, GL11.GL_RGB,
				GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.getId(), 0);
		depthTexture = new Texture();
		GL13.glActiveTexture(TextureBank.DEPTH.getBind());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.getId());
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, window_width, window_height, 0,
				GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthTexture.getId(), 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		skyboxTexture = new Texture();
		GL13.glActiveTexture(TextureBank.SKYBOX.getBind());
		skyboxTexture
				.load(new SkyboxLoader(new File("res/darkskies/"), new File("res/darkskies/darkskies.cfg")).load());
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, skyboxTexture.getId());
		GL13.glActiveTexture(TextureBank.REUSE.getBind());

		GameControls.setup();

		rayTriangleIntersection = new MollerTrumbore(true);
		raySphereIntersection = new RaySphereIntersection();

		//puzzleBall = new PuzzleBall(new Vector3D(0, 20f, 0), new Vector3D(Vector3D.ZERO));
		puzzleBalls = new ArrayList<PuzzleBall>();
		for (int i = 20; i <= 500; i += 10) {
			puzzleBalls.add(new PuzzleBall(new Vector3D(0, i, 0), new Vector3D(Vector3D.ZERO)));
		}
		puzzleGrid = new PuzzleGrid();

		Game.triangles = terrain.getTriangles();
		CollisionPacket.consumers.add((position, velocity) -> collision -> {
			Vector3D origin = position;
			float radius = velocity.getAbsoluteValue() + 2f;

			int roundedX = Math.round(origin.getX() / TILE_SIZE);
			int roundedZ = Math.round(origin.getZ() / TILE_SIZE);
			int indexRadius = (int) Math.ceil(radius / TILE_SIZE);
			for (int i = -indexRadius; i <= indexRadius; i++) {
				for (int j = -indexRadius; j <= indexRadius; j++) {
					if (i * i + j * j <= indexRadius * indexRadius) {
						checkTriangle(position, velocity, collision, roundedX + i + triangles.length / 2, roundedZ + j + triangles[0].length / 2);
					}
				}
			}
		});
		System.out.println("Triangles: " + CollisionPacket.triangles.size());

		SzudzikIntPair p = SzudzikIntPair.INSTANCE;
		ToIntFunction<int[]> pairer = (b) -> p.applyAsInt(b[0], p.applyAsInt(b[1], b[2]));
		PerlinNoise noise = new PerlinNoise((s) -> MurmurHash.createWithSeed(s), pairer, x -> 1f, 6);
		float[] stat = {10000f, -10000f};
		MarchingCube marchingCube = new MarchingCube(vector -> noise.apply(vector.divide(800f)),
				new Vector3D(100f, 100f, 100f), 1f, 0f);
		marchingCubeModel = marchingCube.getIndexedModel();

		EventManager.INSTANCE.registerListener(new Listener() {
			@Subscribe
			public void onKeyRelease(KeyEvent event) {
				if(event.getAction() == GLFW.GLFW_RELEASE) {
					if (event.getKey() == GLFW.GLFW_KEY_R) {
						System.out.println("Set Origin: " + player.getPosition());
						line.set(0, new Vector3D(player.getPosition()));
					}
					if (event.getKey() == GLFW.GLFW_KEY_T) {
						line.set(1, new Vector3D(player.getPosition().subtract(line.getOrigin())));
						System.out.println("Set Direction: " + line.getDirection());
					}
					if (event.getKey() == GLFW.GLFW_KEY_G) {
						puzzleBalls.add(new PuzzleBall(new Vector3D(player.getPosition()), new Vector3D(player.getVelocity())));
					}
					if (event.getKey() == GLFW.GLFW_KEY_C) {
						puzzleBalls.clear();
					}
				}
			}
		});
	}
	// temp
	private static Triangle[][][] triangles;
	private static void checkTriangle(Vector3D position, Vector3D velocity, Collision collision, int x, int y) {
		if (x >= 0 && x < triangles.length && y >= 0 && y < triangles[0].length) {
			CollisionPacket.checkTriangle(position, velocity, triangles[x][y][0], collision);
			CollisionPacket.checkTriangle(position, velocity, triangles[x][y][1], collision);
		}
	}

	private List<PuzzleBall> puzzleBalls;
	private PuzzleGrid puzzleGrid;
	private Renderable marchingCubeModel;

	private static float friction = 0.98f;
	private static float maxSpeed = 0.03f;
	private static float playerSpeed = maxSpeed - maxSpeed * friction;
	private static final Vector3D GRAVITY_VECTOR = Vector3D.unmodifiableVector(new Vector3D(0, -0.005f, 0));

	@Subscribe
	public void update(UpdateEvent event) {
		float angle = (player.getCamera().getRotation().getY() + MathUtil.PI / 2f);
		float sin = (float) Math.sin(angle);
		float cos = (float) Math.cos(angle);
		if (GameControls.STRAFE_LEFT.isActivated()) {
			player.getVelocity().setX(player.getVelocity().getX() - ((float) (playerSpeed)) * sin);
			player.getVelocity().setZ(player.getVelocity().getZ() - ((float) (playerSpeed)) * cos);
		}
		if (GameControls.STRAFE_RIGHT.isActivated()) {
			player.getVelocity().setX(player.getVelocity().getX() + ((float) (playerSpeed)) * sin);
			player.getVelocity().setZ(player.getVelocity().getZ() + ((float) (playerSpeed)) * cos);
		}
		angle = player.getCamera().getRotation().getY();
		sin = (float) Math.sin(angle);
		cos = (float) Math.cos(angle);
		if (GameControls.MOVE_FORWARDS.isActivated()) {
			player.getVelocity().setX(player.getVelocity().getX() - ((float) (playerSpeed)) * sin);
			player.getVelocity().setZ(player.getVelocity().getZ() - ((float) (playerSpeed)) * cos);
		}
		if (GameControls.MOVE_BACKWARDS.isActivated()) {
			player.getVelocity().setX(player.getVelocity().getX() + ((float) (playerSpeed)) * sin);
			player.getVelocity().setZ(player.getVelocity().getZ() + ((float) (playerSpeed)) * cos);
		}
		if (GameControls.MOVE_UP.isActivated()) {
			player.getVelocity().setY(player.getVelocity().getY() + ((float) (playerSpeed)));
		}
		if (GameControls.MOVE_DOWN.isActivated()) {
			player.getVelocity().setY(player.getVelocity().getY() - ((float) (playerSpeed)));
		}
		player.getVelocity().selfMultiply(friction);

		player.update(event);
		updateViewMatrix(CommonPrograms3D.COLOR);
		updateViewMatrix(CommonPrograms3D.TEXTURE);
		updateCubeMapMatrix(CommonPrograms3D.CUBEMAP);

		for (PuzzleBall puzzleBall : puzzleBalls) {
			puzzleBall.getVelocity().selfAdd(GRAVITY_VECTOR);
			CollisionPacket.collideAndSlide(puzzleBall.getPosition(), puzzleBall.getVelocity());
		}
	}
	@Subscribe
	public void onMouseScroll(MouseScrollEvent event) {
		playerSpeed += (float) (event.getYOffset() / 100f);
		if (playerSpeed < 0) {
			playerSpeed = 0;
		}
		player.getCamera().getProjection()
				.setFov(player.getCamera().getProjection().getFov() + ((float) (event.getYOffset() / 10000f)));
		updateProjectionMatrix(CommonPrograms3D.COLOR);
		updateProjectionMatrix(CommonPrograms3D.TEXTURE);
		updateProjectionMatrix(CommonPrograms3D.CUBEMAP);
	}

	private double lastMouseX;
	private double lastMouseY;
	private double mouseX;
	private double mouseY;
	private static final float MOUSE_SENSITIVITY = 0.001f;

	@Subscribe
	public void onMousePosition(CursorPositionEvent event) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		mouseX = event.getX();
		mouseY = event.getY();
		if (GameControls.CAMERA_ROTATE.isActivated()) {
			player.getCamera().getRotation().setY(
					(float) ((((player.getCamera().getRotation().getY() - (mouseX - lastMouseX) * MOUSE_SENSITIVITY)
							% (2f * MathUtil.PI)) + (2 * MathUtil.PI)) % (2 * MathUtil.PI)));
			player.getCamera().getRotation().setX(
					(float) (player.getCamera().getRotation().getX() - (mouseY - lastMouseY) * MOUSE_SENSITIVITY));
			if (player.getCamera().getRotation().getX() < -MathUtil.PI / 2f) {
				player.getCamera().getRotation().setX(-MathUtil.PI / 2f);
			}
			if (player.getCamera().getRotation().getX() > MathUtil.PI / 2f) {
				player.getCamera().getRotation().setX(MathUtil.PI / 2f);
			}
		}
		updateViewMatrix(CommonPrograms3D.COLOR);
		updateViewMatrix(CommonPrograms3D.TEXTURE);
		updateCubeMapMatrix(CommonPrograms3D.CUBEMAP);
	}
	public void updateViewMatrix(ShaderProgramHolder holder) {
		ShaderProgram program = holder.getShaderProgram();
		GL20.glUseProgram(program.getId());
		program.loadMatrix(MatrixType.VIEW_MATRIX, player.getCamera().getInvertedRotationMatrix()
				.multiply(player.getCamera().getInvertedTranslationMatrix()));
		GL20.glUseProgram(0);
	}
	public void updateCubeMapMatrix(ShaderProgramHolder holder) {
		ShaderProgram program = holder.getShaderProgram();
		GL20.glUseProgram(program.getId());
		program.loadMatrix(MatrixType.VIEW_MATRIX, player.getCamera().getInvertedRotationMatrix());
		GL20.glUseProgram(0);
	}
	public void updateProjectionMatrix(ShaderProgramHolder holder) {
		ShaderProgram program = holder.getShaderProgram();
		GL20.glUseProgram(program.getId());
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, player.getCamera().getProjectionMatrix());
		GL20.glUseProgram(0);
	}
	@Subscribe
	public void render(RenderEvent event) {
		frameBuffer.bind(frameBuffer -> {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glDepthMask(false);
			renderSkybox();
			GL11.glDepthMask(true);
			renderHeightMap();
			renderPlatforms();
			for (PuzzleBall puzzleBall : puzzleBalls) {
				puzzleBall.render();
			}
			//puzzleGrid.render();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			CommonPrograms3D.COLOR.getShaderProgram().use(program -> {
				program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(new Vector3D(0f, 10f, 0f)));
				marchingCubeModel.render();
			});
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		});
		CommonPrograms3D.POST_PROCESSING.getShaderProgram().use(program -> {
			Quad.TEXTURED.render();
		});
		if (GameControls.DEBUG_TOGGLE.isActivated()) {
			renderFPS();
		}
	}
	public void renderHeightMap() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL20.glUseProgram(CommonPrograms3D.COLOR.getShaderProgram().getId());
		CommonPrograms3D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		terrain.render();
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	public void renderPlatforms() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	public void renderSkybox() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL20.glUseProgram(CommonPrograms3D.CUBEMAP.getShaderProgram().getId());
		Skybox.INSTANCE.render();
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_BLEND);
	}
	public void renderFPS() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL20.glUseProgram(CommonPrograms2D.LINE.getShaderProgram().getId());
		byte color = 1; // Not Black
		for (String benchmarker : this.benchmarker.getNames()) {
			CommonPrograms2D.LINE.getShaderProgram().loadVector("color", new Vector3D((((color & 0x01) != 0) ? 1f : 0f),
					(((color & 0x02) != 0) ? 1f : 0f), (((color & 0x04) != 0) ? 1f : 0f)));
			CommonPrograms2D.LINE.getShaderProgram().loadFloat("spacing",
					2f / (this.benchmarker.getLineGraph(benchmarker).getSize() - 1));
			this.benchmarker.getLineGraph(benchmarker).render();
			color++;
		}
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_BLEND);
	}
	@Subscribe
	public void onBenchmark(BenchmarkEvent event) {
		benchmarker.benchmark(event.getBenchmark());
	}
	private MollerTrumbore rayTriangleIntersection;
	private RaySphereIntersection raySphereIntersection;
	private Line line = new Line();
	@Subscribe
	public void onClick(MouseButtonEvent event){
		if(event.getAction() == GLFW.GLFW_RELEASE){
			if(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_1){
				Line line = new Line(player.getCamera().getPosition(), player.getVectorDirection());
				System.out.println(rayTriangleIntersection.apply(new Triangle(new Vector3D(-1f, 0f, -1f), new Vector3D(-1f, 0f, 1f), new Vector3D(1f, 0f, 0f)),
						line));
				System.out.println(raySphereIntersection.apply(line, new Sphere(Vector3D.ZERO, 1f)));
				System.out.println(LineLineIntersection.INSTANCE.apply(line, this.line));
			}
		}
	}
}
