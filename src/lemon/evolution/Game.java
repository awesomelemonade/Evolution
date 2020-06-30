package lemon.evolution;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import lemon.engine.control.CleanUpEvent;
import lemon.engine.control.Loader;
import lemon.engine.draw.CommonDrawables;
import lemon.engine.draw.Drawable;
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
import lemon.engine.math.Percentage;
import lemon.engine.math.Projection;
import lemon.engine.math.Sphere;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.ObjLoader;
import lemon.evolution.destructible.beta.ScalarField;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.destructible.beta.TerrainChunk;
import lemon.evolution.destructible.beta.TerrainGenerator;
import lemon.evolution.particle.beta.ParticleSystem;
import lemon.evolution.physicsbeta.Collision;
import lemon.evolution.physicsbeta.CollisionPacket;
import lemon.evolution.puzzle.PuzzleBall;
import lemon.evolution.puzzle.PuzzleGrid;
import lemon.evolution.util.BasicControlActivator;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.util.CommonPrograms3D;
import lemon.evolution.util.PlayerControl;
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
import lemon.engine.terrain.HeightMapGenerator;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.time.BenchmarkEvent;
import lemon.engine.time.Benchmarker;
import lemon.evolution.setup.CommonProgramsSetup;

public enum Game implements Listener {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(Game.class.getName());

	private boolean loaded;

	private Player player;
	private HeightMap heightMap;

	private static final float TILE_SIZE = 0.5f; // 0.2f 1f

	private FrameBuffer frameBuffer;
	private Texture colorTexture;
	private Texture depthTexture;
	private Texture skyboxTexture;

	private Benchmarker benchmarker;

	private Terrain terrain;
	private HeightMapLoader heightMapLoader;
	private ParticleSystem particleSystem;

	private ObjLoader dragonLoader;
	private Drawable dragonModel;
	private Vector3D lightPosition;

	private TextModel debugTextModel;
	private TextModel keyTextModel;

	private int windowWidth;
	private int windowHeight;

	public HeightMapLoader getHeightMapLoader() {
		if (heightMapLoader == null) {
			heightMapLoader = new HeightMapLoader(new HeightMapGenerator(), Math.max((int) (500f / TILE_SIZE), 2),
					Math.max((int) (500f / TILE_SIZE), 2));
		}
		return heightMapLoader;
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
			ToIntFunction<int[]> pairer = (b) -> (int) SzudzikIntPair.pair(b[0], b[1], b[2]);
			PerlinNoise<Vector3D> noise = new PerlinNoise<Vector3D>(MurmurHash::createWithSeed, pairer, x -> 1f, 6);
			ScalarField<Vector3D> scalarField = vector -> vector.getY() < -30f ? 0f : -(vector.getY() + noise.apply(vector.divide(100f)) * 5f);
			ExecutorService pool = Executors.newFixedThreadPool(3);
			EventManager.INSTANCE.registerListener(new Listener() {
				@Subscribe
				public void cleanUp(CleanUpEvent event) {
					pool.shutdownNow();
				}
			});
			TerrainGenerator generator = new TerrainGenerator(pool, scalarField);
			terrain = new Terrain(generator::queueChunk, pool, new Vector3D(5f, 5f, 5f));
			dragonLoader = new ObjLoader("/res/dragon.obj");

			int n = 5;
			int numChunksToPreload = (2 * n + 1) * (2 * n + 1) * (2 * n + 1);
			Percentage terrainLoaderPercentage = new Percentage(numChunksToPreload);
			// Add loaders
			Loading loading = new Loading(() -> {
				EventManager.INSTANCE.registerListener(Game.INSTANCE);
			}, Game.INSTANCE.getHeightMapLoader(), dragonLoader, new Loader() {
				@Override
				public void load() {
					for (int i = -n; i <= n; i++) {
						for (int j = -n; j <= n; j++) {
							for (int k = -n; k <= n; k++) {
								terrain.preloadChunk(i, j, k);
							}
						}
					}
				}
				@Override
				public Percentage getPercentage() {
					terrainLoaderPercentage.setPart(numChunksToPreload - generator.getQueueSize());
					return terrainLoaderPercentage;
				}
			});
			EventManager.INSTANCE.registerListener(loading);
			loaded = true;
			return;
		}


		logger.log(Level.FINE, "Initializing");
		GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, height);
		windowWidth = width.get();
		windowHeight = height.get();

		GL11.glViewport(0, 0, windowWidth, windowHeight);

		heightMap = new HeightMap(heightMapLoader.getTerrain(), TILE_SIZE);

		benchmarker = new Benchmarker();
		benchmarker.put("updateData", new LineGraph(1000, 100000000));
		benchmarker.put("renderData", new LineGraph(1000, 100000000));
		benchmarker.put("fpsData", new LineGraph(1000, 100));
		benchmarker.put("debugData", new LineGraph(1000, 100));
		benchmarker.put("memory", new LineGraph(1000, 3000000000f));
		benchmarker.put("memoryDerivative", new LineGraph(1000, 30000000f));

		player = new Player(new Projection(MathUtil.toRadians(60f),
				((float) windowWidth) / ((float) windowHeight), 0.01f, 1000f));

		Matrix orthoProjectionMatrix = MathUtil.getOrtho(windowWidth, windowHeight, -1, 1);
		CommonProgramsSetup.setup2D(orthoProjectionMatrix);
		CommonProgramsSetup.setup3D(player.getCamera().getProjectionMatrix());

		updateViewMatrices();

		frameBuffer = new FrameBuffer();
		frameBuffer.bind(frameBuffer -> {
			GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
			colorTexture = new Texture();
			GL13.glActiveTexture(TextureBank.COLOR.getBind());
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.getId());
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, windowWidth, windowHeight, 0, GL11.GL_RGB,
					GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.getId(), 0);
			depthTexture = new Texture();
			GL13.glActiveTexture(TextureBank.DEPTH.getBind());
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.getId());
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, windowWidth, windowHeight, 0,
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
		BasicControlActivator.bindKeyboardHold(GLFW.GLFW_KEY_Y, EXPLODE);

		rayTriangleIntersection = new MollerTrumbore(true);
		raySphereIntersection = new RaySphereIntersection();

		//puzzleBall = new PuzzleBall(new Vector3D(0, 20f, 0), new Vector3D(Vector3D.ZERO));
		puzzleBalls = new ArrayList<PuzzleBall>();
		for (int i = 20; i <= 500; i += 10) {
			puzzleBalls.add(new PuzzleBall(new Vector3D(0, i, 0), new Vector3D(Vector3D.ZERO)));
		}
		puzzleGrid = new PuzzleGrid();

		Game.triangles = heightMap.getTriangles();
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

		particleSystem = new ParticleSystem(20000);

		dragonModel = dragonLoader.toIndexedDrawable();
		lightPosition = new Vector3D(player.getPosition());

		Font font = new Font(Paths.get("/res/fonts/FreeSans.fnt"));
		debugTextModel = new TextModel(font, "[Unknown]", GL15.GL_DYNAMIC_DRAW);
		keyTextModel = new TextModel(font, "[Unknown]", GL15.GL_DYNAMIC_DRAW);

		EventManager.INSTANCE.registerListener(new Listener() {
			@Subscribe
			public void onKeyRelease(KeyEvent event) {
				if(event.getAction() == GLFW.GLFW_RELEASE) {
					if (event.getKey() == GLFW.GLFW_KEY_R) {
						System.out.println("Set Origin: " + player.getPosition());
						line.set(0, new Vector3D(player.getPosition()));
						lightPosition = new Vector3D(player.getPosition());
					}
					if (event.getKey() == GLFW.GLFW_KEY_T) {
						line.set(1, player.getPosition().copy().subtract(line.getOrigin()));
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
	private PlayerControl EXPLODE = new PlayerControl();
	public void generateExplosionAtCrosshair() {
		if (depthDistance != 1f) {
			float realDistance = (float) (0.00997367 * Math.pow(1.0 - depthDistance + 0.0000100616, -1.00036));
			Vector3D position = player.getPosition().copy().add(player.getVectorDirection().multiply(realDistance));
			terrain.generateExplosion(position, 10f);
		}
	}
	float depthDistance = 0;
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

	private static float friction = 0.98f;
	private static float maxSpeed = 0.03f;
	private static float playerSpeed = maxSpeed - maxSpeed * friction;
	private static final Vector3D GRAVITY_VECTOR = Vector3D.unmodifiableVector(new Vector3D(0, -0.005f, 0));

	@Subscribe
	public void update(UpdateEvent event) {
		if (EXPLODE.isActivated()) {
			generateExplosionAtCrosshair();
		}

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
		player.getVelocity().multiply(friction);

		player.update(event);

		float totalLength = 0;
		for (PuzzleBall puzzleBall : puzzleBalls) {
			puzzleBall.getVelocity().add(GRAVITY_VECTOR);
			CollisionPacket.collideAndSlide(puzzleBall.getPosition(), puzzleBall.getVelocity());
			totalLength += puzzleBall.getVelocity().getLength();
		}
		benchmarker.getLineGraph("debugData").add(totalLength);
		float last = benchmarker.getLineGraph("memory").getLast();
		float current = Runtime.getRuntime().freeMemory();
		benchmarker.getLineGraph("memory").add(current);
		benchmarker.getLineGraph("memoryDerivative").add(last - current);
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
		playerSpeed += (float) (event.getYOffset() / 100f);
		if (playerSpeed < 0) {
			playerSpeed = 0;
		}
		player.getCamera().getProjection()
				.setFov(player.getCamera().getProjection().getFov() + ((float) (event.getYOffset() / 10000f)));
		updateProjectionMatrices();
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
		holder.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.VIEW_MATRIX, player.getCamera().getTransformationMatrix());
		});
	}
	public void updateCubeMapMatrix(ShaderProgramHolder holder) {
		holder.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.VIEW_MATRIX, player.getCamera().getInvertedRotationMatrix());
		});
	}
	public void updateProjectionMatrix(ShaderProgramHolder holder) {
		holder.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, player.getCamera().getProjectionMatrix());
		});
	}
	@Subscribe
	public void render(RenderEvent event) {
		updateViewMatrices();
		frameBuffer.bind(frameBuffer -> {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glDepthMask(false);
			renderSkybox();
			GL11.glDepthMask(true);
			//renderHeightMap();
			for (PuzzleBall puzzleBall : puzzleBalls) {
				puzzleBall.render();
			}
			//puzzleGrid.render();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			CommonPrograms3D.COLOR.getShaderProgram().use(program -> {
				int playerChunkX = Math.floorDiv((int) (player.getPosition().getX() / 5f), TerrainChunk.SIZE);
				int playerChunkY = Math.floorDiv((int) (player.getPosition().getY() / 5f), TerrainChunk.SIZE);
				int playerChunkZ = Math.floorDiv((int) (player.getPosition().getZ() / 5f), TerrainChunk.SIZE);
				int n = 5;
				for (int i = -n; i <= n; i++) {
					for (int j = -n; j <= n; j++) {
						for (int k = -n; k <= n; k++) {
							terrain.drawOrQueue(
									playerChunkX + i, playerChunkY + j, playerChunkZ + k,
									(matrix, drawable) -> {
								program.loadMatrix(MatrixType.MODEL_MATRIX, matrix);
								drawable.draw();
							});
						}
					}
				}
			});
			particleSystem.render();
			CommonPrograms3D.LIGHT.getShaderProgram().use(program -> {
				Vector3D position = new Vector3D(96f, 40f, 0f);
				program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(position)
						.multiply(MathUtil.getScalar(new Vector3D(8f, 8f, 8f))));
				program.loadVector("sunlightDirection", lightPosition.copy().subtract(position).normalize());
				program.loadVector("viewPos", player.getPosition());
				dragonModel.draw();
			});
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			FloatBuffer buffer = BufferUtils.createFloatBuffer(1);
			GL11.glReadPixels(windowWidth / 2, windowHeight / 2, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, buffer);
			depthDistance = buffer.get(); // far plane
		});
		CommonPrograms3D.POST_PROCESSING.getShaderProgram().use(program -> {
			CommonDrawables.TEXTURED_QUAD.draw();
		});
		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			// Render crosshair
			program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX,
					MathUtil.getTranslation(new Vector3D(windowWidth / 2f, windowHeight / 2f, 0f))
							.multiply(MathUtil.getScalar(new Vector3D(5f, 1f, 1f))));
			CommonDrawables.COLORED_QUAD.draw();
			program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX,
					MathUtil.getTranslation(new Vector3D(windowWidth / 2f, windowHeight / 2f, 0f))
							.multiply(MathUtil.getScalar(new Vector3D(1f, 5f, 1f))));
			CommonDrawables.COLORED_QUAD.draw();
		});
		if (GameControls.DEBUG_TOGGLE.isActivated()) {
			// render graphs
			byte counter = 1; // Not Black
			for (String benchmarker : this.benchmarker.getNames()) {
				Color color = new Color((((counter & 0x01) != 0) ? 1f : 0f),
						(((counter & 0x02) != 0) ? 1f : 0f), (((counter & 0x04) != 0) ? 1f : 0f));
				CommonPrograms2D.LINE.getShaderProgram().use(program -> {
					program.loadColor3f(color);
					program.loadFloat("spacing",
							2f / (this.benchmarker.getLineGraph(benchmarker).getSize() - 1));
					this.benchmarker.getLineGraph(benchmarker).render();
				});
				byte finalCounter = counter;
				CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
					program.loadColor4f("filterColor", color);
					program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX,
							MathUtil.getTranslation(new Vector3D(windowWidth - 200f, windowHeight - 20f - (finalCounter * 30f), 0f))
									.multiply(MathUtil.getScalar(new Vector3D(10f, 10f, 1f))));
					CommonDrawables.COLORED_QUAD.draw();
					program.loadColor4f("filterColor", Color.WHITE);
				});
				CommonPrograms2D.TEXT.getShaderProgram().use(program -> {
					program.loadMatrix(MatrixType.MODEL_MATRIX,
							MathUtil.getTranslation(new Vector3D(windowWidth - 170f, windowHeight - 25f - (finalCounter * 30f), 0f))
									.multiply(MathUtil.getScalar(new Vector3D(0.2f, 0.2f, 1f))));
					program.loadColor3f("color", Color.WHITE);
					keyTextModel.setText(benchmarker);
					keyTextModel.draw();
				});
				counter++;
			}
			// render debug text
			CommonPrograms2D.TEXT.getShaderProgram().use(program -> {
				program.loadMatrix(MatrixType.MODEL_MATRIX,
						MathUtil.getTranslation(new Vector3D(5f, windowHeight - 20, 0f))
								.multiply(MathUtil.getScalar(new Vector3D(0.2f, 0.2f, 1f))));
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
		heightMap.render();
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
			if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_1){
				Line line = new Line(player.getCamera().getPosition(), player.getVectorDirection());
				System.out.println(rayTriangleIntersection.apply(new Triangle(new Vector3D(-1f, 0f, -1f), new Vector3D(-1f, 0f, 1f), new Vector3D(1f, 0f, 0f)),
						line));
				System.out.println(raySphereIntersection.apply(line, new Sphere(Vector3D.ZERO, 1f)));
				System.out.println(LineLineIntersection.INSTANCE.apply(line, this.line));
			}
			if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_2) {
				generateExplosionAtCrosshair();
			}
		}
	}
}
