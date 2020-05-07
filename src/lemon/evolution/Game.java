package lemon.evolution;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.draw.Drawable;
import lemon.engine.draw.DynamicIndexedDrawable;
import lemon.engine.draw.TextModel;
import lemon.engine.font.Font;
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
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.ObjLoader;
import lemon.evolution.destructible.beta.MarchingCube;
import lemon.evolution.destructible.beta.ScalarField;
import lemon.evolution.particle.beta.ParticleSystem;
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
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.model.HeightMap;
import lemon.engine.model.LineGraph;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.game.Player;
import lemon.engine.input.CursorPositionEvent;
import lemon.engine.input.KeyEvent;
import lemon.engine.input.MouseButtonEvent;
import lemon.engine.input.MouseScrollEvent;
import lemon.engine.toolbox.SkyboxLoader;
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

	private boolean loaded;

	private Player player;
	private HeightMap terrain;

	private static final float TILE_SIZE = 0.5f; // 0.2f 1f

	private FrameBuffer frameBuffer;
	private Texture colorTexture;
	private Texture depthTexture;
	private Texture skyboxTexture;

	private Benchmarker benchmarker;

	private TerrainLoader terrainLoader;
	private MarchingCube marchingCube;
	private float[][][] marchingCubeData;
	private ParticleSystem particleSystem;

	private ObjLoader dragonLoader;
	private Drawable dragonModel;
	private Vector3D lightPosition;

	private TextModel debugTextModel;

	public TerrainLoader getTerrainLoader() {
		if (terrainLoader == null) {
			terrainLoader = new TerrainLoader(new TerrainGenerator(), Math.max((int) (500f / TILE_SIZE), 2),
					Math.max((int) (500f / TILE_SIZE), 2));
		}
		return terrainLoader;
	}

	public static float getPercentage(Vector3D lower, Vector3D upper, float resolution, Predicate<Vector3D> predicate) {
		float count = 0;
		float total = 0;
		for (float x = lower.getX(); x <= upper.getX(); x += resolution) {
			for (float y = lower.getY(); y <= upper.getY(); y += resolution) {
				for (float z = lower.getZ(); z <= upper.getZ(); z += resolution) {
					if (predicate.test(new Vector3D(x, y, z))) {
						count++;
					}
					total++;
				}
			}
		}
		return count / total;
	}

	@Override
	public void onRegister() {
		if (!loaded) {
			EventManager.INSTANCE.unregisterListener(this);
			// Prepare loaders

			SzudzikIntPair p = SzudzikIntPair.INSTANCE;
			ToIntFunction<int[]> pairer = (b) -> p.applyAsInt(b[0], p.applyAsInt(b[1], b[2]));
			PerlinNoise<Vector3D> noise = new PerlinNoise<Vector3D>(MurmurHash::createWithSeed, pairer, x -> 1f, 6);
			ScalarField<Vector3D> scalarField = vector -> noise.apply(vector.divide(800f));
			marchingCubeData = new float[20][20][20];
			marchingCube = new MarchingCube(marchingCubeData, new Vector3D(100f, 100f, 100f), 0f);
			dragonLoader = new ObjLoader("/res/dragon.obj");

			// Add loaders
			Loading loading = new Loading(() -> {
				EventManager.INSTANCE.registerListener(Game.INSTANCE);
			}, Game.INSTANCE.getTerrainLoader(), dragonLoader,
					ScalarField.getLoader(scalarField, Vector3D.ZERO, new Vector3D(5f, 5f, 5f), marchingCubeData));
			EventManager.INSTANCE.registerListener(loading);
			loaded = true;
			return;
		}


		logger.log(Level.FINE, "Initializing");
		GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, height);
		int window_width = width.get();
		int window_height = height.get();

		GL11.glViewport(0, 0, window_width, window_height);

		terrain = new HeightMap(terrainLoader.getTerrain(), TILE_SIZE);

		benchmarker = new Benchmarker();
		benchmarker.put("updateData", new LineGraph(1000, 100000000));
		benchmarker.put("renderData", new LineGraph(1000, 100000000));
		benchmarker.put("fpsData", new LineGraph(1000, 100));

		player = new Player(new Projection(MathUtil.toRadians(60f),
				((float) window_width) / ((float) window_height), 0.01f, 1000f));

		Matrix orthoProjectionMatrix = MathUtil.getOrtho(window_width, window_height, -1, 1);
		CommonProgramsSetup.setup2D(orthoProjectionMatrix);
		CommonProgramsSetup.setup3D(player.getCamera().getProjectionMatrix());

		updateViewMatrices();

		frameBuffer = new FrameBuffer();
		frameBuffer.bind(frameBuffer -> {
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
		});
		skyboxTexture = new Texture();
		GL13.glActiveTexture(TextureBank.SKYBOX.getBind());
		skyboxTexture.load(new SkyboxLoader("/res/darkskies", "darkskies.cfg").load());
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

		marchingCubeModel = marchingCube.getColoredModel().map(DynamicIndexedDrawable::new);
		particleSystem = new ParticleSystem(20000);

		dragonModel = dragonLoader.toIndexedDrawable();
		lightPosition = new Vector3D(player.getPosition());

		Font font = new Font(new File("res/fonts/FreeSans.fnt"));
		debugTextModel = new TextModel(font, "[Unknown]", GL15.GL_DYNAMIC_DRAW);

		EventManager.INSTANCE.registerListener(new Listener() {
			@Subscribe
			public void onKeyRelease(KeyEvent event) {
				if(event.getAction() == GLFW.GLFW_RELEASE) {
					if (event.getKey() == GLFW.GLFW_KEY_Y) {
						int x = marchingCubeData.length / 2;
						int y = marchingCubeData[0].length / 2;
						int z = marchingCubeData[0][0].length / 2;
						Vector3D sphereCenter = new Vector3D(x, y, z);
						for (int i = 0; i < marchingCubeData.length; i++) {
							for (int j = 0; j < marchingCubeData[0].length; j++) {
								for (int k = 0; k < marchingCubeData[0][0].length; k++) {
									Vector3D center = new Vector3D(i, j, k);
									Vector3D lower = center.subtract(new Vector3D(0.5f, 0.5f, 0.5f));
									Vector3D upper = center.add(new Vector3D(0.5f, 0.5f, 0.5f));
									marchingCubeData[i][j][k] = Math.min(marchingCubeData[i][j][k],
											1f - getPercentage(lower, upper, 0.1f, (v) -> {
												return v.getDistanceSquared(sphereCenter) <= 7 * 7;
											}) * 2f);
								}
							}
						}
						marchingCube.getColoredModel().use((vertices, indices) -> {
							marchingCubeModel.setData(vertices, indices);
						});
					}
					if (event.getKey() == GLFW.GLFW_KEY_R) {
						System.out.println("Set Origin: " + player.getPosition());
						line.set(0, new Vector3D(player.getPosition()));
						lightPosition = new Vector3D(player.getPosition());
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
	private DynamicIndexedDrawable marchingCubeModel;

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
		updateViewMatrices();

		for (PuzzleBall puzzleBall : puzzleBalls) {
			puzzleBall.getVelocity().selfAdd(GRAVITY_VECTOR);
			CollisionPacket.collideAndSlide(puzzleBall.getPosition(), puzzleBall.getVelocity());
		}
		String message = String.format("Listeners Registered=%d, Methods=%d, Preloaded=%d, Time=%d",
				EventManager.INSTANCE.getListenersRegistered(),
				EventManager.INSTANCE.getListenerMethodsRegistered(),
				EventManager.INSTANCE.getPreloadedMethodsRegistered(),
				System.currentTimeMillis());
		if (!debugTextModel.getText().equals(message)) {
			debugTextModel.setText(message);
		}
	}
	@Subscribe
	public void onMouseScroll(MouseScrollEvent event) {
		marchingCube.setThreshold(marchingCube.getThreshold() + ((float) event.getYOffset()) / 20f);
		marchingCube.getColoredModel().use((vertices, indices) -> {
			marchingCubeModel.setData(vertices, indices);
		});
		System.out.println(marchingCube.getThreshold());
		/*
		playerSpeed += (float) (event.getYOffset() / 100f);
		if (playerSpeed < 0) {
			playerSpeed = 0;
		}
		player.getCamera().getProjection()
				.setFov(player.getCamera().getProjection().getFov() + ((float) (event.getYOffset() / 10000f)));
		updateProjectionMatrices();
		 */
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
			updateViewMatrices();
		}
	}
	public void updateViewMatrices() {
		updateViewMatrix(CommonPrograms3D.COLOR);
		updateViewMatrix(CommonPrograms3D.TEXTURE);
		updateCubeMapMatrix(CommonPrograms3D.CUBEMAP);
		updateViewMatrix(CommonPrograms3D.PARTICLE);
		updateViewMatrix(CommonPrograms3D.LIGHT);
	}
	public void updateProjectionMatrices() {
		updateProjectionMatrix(CommonPrograms3D.COLOR);
		updateProjectionMatrix(CommonPrograms3D.TEXTURE);
		updateProjectionMatrix(CommonPrograms3D.CUBEMAP);
		updateProjectionMatrix(CommonPrograms3D.PARTICLE);
		updateProjectionMatrix(CommonPrograms3D.LIGHT);
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
			for (PuzzleBall puzzleBall : puzzleBalls) {
				puzzleBall.render();
			}
			//puzzleGrid.render();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			CommonPrograms3D.COLOR.getShaderProgram().use(program -> {
				program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(new Vector3D(0f, 50f, 0f)));
				marchingCubeModel.draw();
			});
			particleSystem.render();
			CommonPrograms3D.LIGHT.getShaderProgram().use(program -> {
				Vector3D position = new Vector3D(96f, 40f, 0f);
				program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(position)
						.multiply(MathUtil.getScalar(new Vector3D(8f, 8f, 8f))));
				program.loadVector("sunlightDirection", lightPosition.subtract(position).normalize());
				program.loadVector("viewPos", player.getPosition());
				dragonModel.draw();
			});
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		});
		CommonPrograms3D.POST_PROCESSING.getShaderProgram().use(program -> {
			CommonDrawables.TEXTURED_QUAD.draw();
		});
		if (GameControls.DEBUG_TOGGLE.isActivated()) {
			// render graphs
			CommonPrograms2D.LINE.getShaderProgram().use(program -> {
				byte color = 1; // Not Black
				for (String benchmarker : this.benchmarker.getNames()) {
					program.loadVector("color", new Vector3D((((color & 0x01) != 0) ? 1f : 0f),
							(((color & 0x02) != 0) ? 1f : 0f), (((color & 0x04) != 0) ? 1f : 0f)));
					program.loadFloat("spacing",
							2f / (this.benchmarker.getLineGraph(benchmarker).getSize() - 1));
					this.benchmarker.getLineGraph(benchmarker).render();
					color++;
				}
			});
			// render debug text
			CommonPrograms2D.TEXT.getShaderProgram().use(program -> {
				program.loadMatrix(MatrixType.MODEL_MATRIX,
						MathUtil.getTranslation(new Vector3D(0f, 550f, 0f))
								.multiply(MathUtil.getScalar(new Vector3D(0.2f, 0.2f, 0.2f))));
				program.loadColor3f("color", Color.WHITE);
				debugTextModel.draw();
			});
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
	public void renderSkybox() {
		CommonPrograms3D.CUBEMAP.getShaderProgram().use(program -> {
			CommonDrawables.SKYBOX.draw();
		});
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
