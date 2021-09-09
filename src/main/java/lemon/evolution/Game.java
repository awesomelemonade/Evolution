package lemon.evolution;

import lemon.engine.control.GLFWWindow;
import lemon.engine.control.Loader;
import lemon.engine.draw.CommonDrawables;
import lemon.engine.draw.Drawable;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.function.MurmurHash;
import lemon.engine.function.PerlinNoise;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.game.Player;
import lemon.engine.input.CursorPositionEvent;
import lemon.engine.input.MouseScrollEvent;
import lemon.engine.math.Box2D;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector2D;
import lemon.engine.math.Vector3D;
import lemon.engine.model.LineGraph;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.texture.TextureData;
import lemon.engine.time.Benchmarker;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposables;
import lemon.engine.toolbox.Histogram;
import lemon.engine.toolbox.ObjLoader;
import lemon.engine.toolbox.SkyboxLoader;
import lemon.engine.toolbox.Toolbox;
import lemon.evolution.destructible.beta.ScalarField;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.destructible.beta.TerrainChunk;
import lemon.evolution.destructible.beta.TerrainGenerator;
import lemon.evolution.destructible.beta.TerrainRenderer;
import lemon.evolution.physics.beta.CollisionPacket;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.puzzle.PuzzleBall;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.ui.beta.UIScreen;
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
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum Game implements Screen {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(Game.class.getName());

	private GLFWWindow window;
	private boolean loaded;

	private Player player;

	private FrameBuffer frameBuffer;

	private DebugOverlay debugOverlay;
	private Benchmarker benchmarker;

	private Terrain terrain;
	private TerrainRenderer terrainRenderer;

	private ObjLoader dragonLoader;
	private Drawable dragonModel;
	private Vector3D lightPosition;

	public List<Vector3D> debug;

	private UIScreen uiScreen;

	private ThreadPoolExecutor pool;
	private ThreadPoolExecutor pool2;

	private Histogram histogram;

	private final Disposables disposables = new Disposables();

	@Override
	public void onLoad(GLFWWindow window) {
		if (!loaded) {
			// Prepare loaders
			ToIntFunction<int[]> pairer = (b) -> (int) SzudzikIntPair.pair(b[0], b[1], b[2]);
			var noise2d = new PerlinNoise<Vector2D>(2, MurmurHash::createWithSeed, (b) -> SzudzikIntPair.pair(b[0], b[1]), x -> 1f, 6);
			PerlinNoise<Vector3D> noise = new PerlinNoise<>(3, MurmurHash::createWithSeed, pairer, x -> 1f, 6);
			ScalarField<Vector3D> scalarField = vector -> vector.y() < -30f ? 0f : -(vector.y() + noise.apply(vector.divide(100f)) * 5f);
			histogram = new Histogram(0.1f);
			scalarField = vector -> {
				float distanceSquared = vector.x() * vector.x() + vector.z() * vector.z();
				float terrain = (float) (-Math.tanh(vector.y() / 100.0) * 100.0 +
						Math.pow(2f, noise2d.apply(vector.toXZVector().divide(300f))) * 5.0 +
						Math.pow(2.5f, noise.apply(vector.divide(500f))) * 2.5);
				histogram.add(terrain);
				/*float x = vector.getY() < 0 ? 0f : Math.min((float) (250.0 - Math.sqrt(distanceSquared)), terrain);
				test[0] = Math.min(test[0], x);
				test[1] = Math.max(test[1], x);
				System.out.println(Arrays.toString(test));
				return x;*/
				return vector.y() < 0 ? 0f : Math.min((float) (250.0 - Math.sqrt(distanceSquared)), terrain);
			};
			pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
			pool2 = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
			pool.setRejectedExecutionHandler((runnable, executor) -> {});
			pool2.setRejectedExecutionHandler((runnable, executor) -> {});
			TerrainGenerator generator = new TerrainGenerator(pool, scalarField);
			terrain = new Terrain(generator, pool2, Vector3D.of(5f, 5f, 5f));
			terrainRenderer = new TerrainRenderer(terrain, 5);
			dragonLoader = new ObjLoader("/res/dragon.obj");

			player = new Player(new Projection(MathUtil.toRadians(60f),
					((float) window.getWidth()) / ((float) window.getHeight()), 0.01f, 1000f));
			player.mutablePosition().set(0f, 300f, 0f);

			// Add loaders
			Loading loading = new Loading(() -> {
				window.switchScreen(Game.INSTANCE);
			}, dragonLoader, new Loader() {
				int generatorStartSize;
				@Override
				public void load() {
					terrainRenderer.preload(player.position());
					generatorStartSize = generator.getQueueSize();
				}

				@Override
				public float getProgress() {
					return 1f - ((float) generator.getQueueSize()) / ((float) generatorStartSize);
				}
			});
			window.switchScreen(loading);
			loaded = true;
			return;
		}


		logger.log(Level.FINE, "Initializing");
		this.window = window;
		disposables.add(() -> pool.shutdown());
		disposables.add(() -> pool2.shutdown());
		//GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
		var windowWidth = window.getWidth();
		var windowHeight = window.getHeight();

		GL11.glViewport(0, 0, windowWidth, windowHeight);

		benchmarker = new Benchmarker();
		benchmarker.put("updateData", new LineGraph(1000, 100000000));
		benchmarker.put("renderData", new LineGraph(1000, 100000000));
		benchmarker.put("fpsData", new LineGraph(1000, 100));
		benchmarker.put("debugData", new LineGraph(1000, 100));
		benchmarker.put("freeMemory", new LineGraph(1000, 5000000000f));
		benchmarker.put("totalMemory", new LineGraph(1000, 5000000000f));

		debugOverlay = disposables.add(new DebugOverlay(window, benchmarker));

		Matrix orthoProjectionMatrix = MathUtil.getOrtho(windowWidth, windowHeight, -1, 1);
		CommonProgramsSetup.setup2D(orthoProjectionMatrix);
		CommonProgramsSetup.setup3D(player.camera().getProjectionMatrix());

		updateViewMatrices();

		frameBuffer = disposables.add(new FrameBuffer());
		frameBuffer.bind(frameBuffer -> {
			GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
			Texture colorTexture = disposables.add(new Texture());
			TextureBank.COLOR.bind(() -> {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.getId());
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, windowWidth, windowHeight, 0, GL11.GL_RGB,
						GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.getId(), 0);
			});
			Texture depthTexture = disposables.add(new Texture());
			TextureBank.DEPTH.bind(() -> {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.getId());
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, windowWidth, windowHeight, 0,
						GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthTexture.getId(), 0);
			});
		});
		TextureBank.SKYBOX.bind(() -> {
			Texture skyboxTexture = new Texture();
			skyboxTexture.load(new SkyboxLoader("/res/darkskies", "darkskies.cfg").load());
			GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, skyboxTexture.getId());
		});
		Map.of(
				TextureBank.GRASS, "/res/grass.png",
				TextureBank.SLOPE, "/res/slope.png",
				TextureBank.ROCK, "/res/rock.png",
				TextureBank.BASE, "/res/base.png"
		).forEach((textureBank, path) -> {
			textureBank.bind(() -> {
				var texture = new Texture();
				texture.load(new TextureData(Toolbox.readImage(path).orElseThrow()));
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
			});
		});

		disposables.add(GameControls.setup(window.input()));
		BasicControlActivator.bindKeyboardHold(GLFW.GLFW_KEY_T, ADD_TERRAIN);
		BasicControlActivator.bindKeyboardHold(GLFW.GLFW_KEY_Y, REMOVE_TERRAIN);

		puzzleBalls = new ArrayList<>();
		projectiles = new ArrayList<>();
		int size = 20;
		for (int i = -size; i <= size; i += 5) {
			for (int j = -size; j <= size; j += 5) {
				puzzleBalls.add(new PuzzleBall(Vector3D.of(i, 100, j), Vector3D.ZERO));
			}
		}
		for (int i = 100; i <= 600; i += 10) {
			puzzleBalls.add(new PuzzleBall(Vector3D.of(0, i, 0), Vector3D.ZERO));
		}

		debug = new ArrayList<>();
		CollisionPacket.consumers.add((position, velocity) -> collision -> {
			var after = position.add(velocity);
			int minChunkX = terrain.getChunkX(Math.min(position.x(), after.x()) - 1f);
			int maxChunkX = terrain.getChunkX(Math.max(position.x(), after.x()) + 1f);
			int minChunkY = terrain.getChunkY(Math.min(position.y(), after.y()) - 1f);
			int maxChunkY = terrain.getChunkY(Math.max(position.y(), after.y()) + 1f);
			int minChunkZ = terrain.getChunkZ(Math.min(position.z(), after.z()) - 1f);
			int maxChunkZ = terrain.getChunkZ(Math.max(position.z(), after.z()) + 1f);
			for (int i = minChunkX; i <= maxChunkX; i++) {
				for (int j = minChunkY; j <= maxChunkY; j++) {
					for (int k = minChunkZ; k <= maxChunkZ; k++) {
						// TODO: something similar to i * i + j * j + k * k <= indexRadius * indexRadius
						TerrainChunk chunk = terrain.getChunk(i, j, k);
						chunk.getTriangles().ifPresent(triangles -> {
							triangles.forEach(triangle -> {
								CollisionPacket.checkTriangle(position, velocity, triangle, collision);
							});
						});
					}
				}
			}
		});
		System.out.println("Triangles: " + CollisionPacket.triangles.size());
		System.out.println("=====[Histogram]=====");
		histogram.print();
		System.out.println("=====================");

		dragonModel = dragonLoader.toIndexedDrawable();
		lightPosition = player.position();

		disposables.add(window.input().keyEvent().add(event -> {
			if (event.action() == GLFW.GLFW_RELEASE) {
				if (event.key() == GLFW.GLFW_KEY_G) {
					puzzleBalls.add(new PuzzleBall(player.position(), player.velocity()));
				}
				if (event.key() == GLFW.GLFW_KEY_C) {
					puzzleBalls.clear();
					debug.clear();
				}
				if (event.key() == GLFW.GLFW_KEY_K) {
					puzzleBalls.forEach(x -> {
						float spray = 0.2f;
						float randX = (float) (Math.random() * spray - spray / 2f);
						float randZ = (float) (Math.random() * spray - spray / 2f);
						projectiles.add(new PuzzleBall(x.position(), Vector3D.of(randX, 0.5f, randZ)));
					});
				}
			}
		}));

		uiScreen = disposables.add(new UIScreen(window.input()));
		uiScreen.addButton(new Box2D(100f, 100f, 100f, 20f), Color.GREEN, x -> {
			System.out.println("Clicked");
		});
		uiScreen.addWheel(Vector2D.of(200f, 200f), 50f, 0f, Color.RED);

		disposables.add(window.onBenchmark().add(benchmark -> benchmarker.benchmark(benchmark)));
		disposables.add(window.input().mouseButtonEvent().add(event -> {
			if (event.action() == GLFW.GLFW_RELEASE) {
				if (event.button() == GLFW.GLFW_MOUSE_BUTTON_2) {
					generateExplosionAtCrosshair();
				}
			}
		}));
		disposables.add(window.input().mouseScrollEvent().add(this::onMouseScroll));
		disposables.add(window.input().cursorPositionEvent().add(this::onMousePosition));
	}

	private final PlayerControl ADD_TERRAIN = new PlayerControl();
	private final PlayerControl REMOVE_TERRAIN = new PlayerControl();

	public Vector3D getCrosshairLocation() {
		float realDistance = (float) (0.00997367 * Math.pow(1.0 - depthDistance + 0.0000100616, -1.00036));
		return player.position().add(player.getVectorDirection().multiply(realDistance));
	}

	public void generateExplosionAtCrosshair() {
		if (depthDistance != 1f) {
			var position = getCrosshairLocation();
			terrain.generateExplosion(position, 5f);
		}
	}

	float depthDistance = 0;

	private List<PuzzleBall> puzzleBalls;
	private List<PuzzleBall> projectiles;

	private static float friction = 1f;
	private static float maxSpeed = 0.03f;
	private static float playerSpeed = maxSpeed - maxSpeed * friction;
	private static final Vector3D GRAVITY_VECTOR = Vector3D.of(0, -0.005f, 0);

	@Override
	public void update(long deltaTime) {
		if (ADD_TERRAIN.isActivated()) {
			float dt = (float) (((double) deltaTime) / 3.0e7);
			var point = getCrosshairLocation();
			terrain.terraform(point, 8f, dt, 5f);
		}
		if (REMOVE_TERRAIN.isActivated()) {
			float dt = (float) (((double) deltaTime) / 3.0e7);
			var point = getCrosshairLocation();
			terrain.terraform(point, 8f, dt, -5f);
		}

		float angle = (player.rotation().y() + MathUtil.PI / 2f);
		float sin = (float) Math.sin(angle);
		float cos = (float) Math.cos(angle);
		var playerHorizontalVector = Vector2D.of(playerSpeed * sin, playerSpeed * cos);
		if (GameControls.STRAFE_LEFT.isActivated()) {
			player.mutableVelocity().asXZVector().subtract(playerHorizontalVector);
		}
		if (GameControls.STRAFE_RIGHT.isActivated()) {
			player.mutableVelocity().asXZVector().add(playerHorizontalVector);
		}
		var playerForwardVector = Vector2D.of(playerSpeed * cos, -playerSpeed * sin);
		if (GameControls.MOVE_FORWARDS.isActivated()) {
			player.mutableVelocity().asXZVector().add(playerForwardVector);
		}
		if (GameControls.MOVE_BACKWARDS.isActivated()) {
			player.mutableVelocity().asXZVector().subtract(playerForwardVector);
		}
		if (GameControls.MOVE_UP.isActivated()) {
			player.mutableVelocity().addY(playerSpeed);
		}
		if (GameControls.MOVE_DOWN.isActivated()) {
			player.mutableVelocity().subtractY(playerSpeed);
		}
		player.mutableVelocity().multiply(friction);
		//player.mutableVelocity().add(GRAVITY_VECTOR);

		CollisionPacket.collideAndSlide(player.mutablePosition(), player.mutableVelocity(), player.velocity(), 20);

		var targetRotation = Vector3D.of(
				(float) Math.atan(player.velocity().y() / Math.hypot(player.velocity().x(), player.velocity().z())),
				(float) (Math.PI + Math.atan2(player.velocity().x(), player.velocity().z())), 0f);
		var diff = targetRotation.subtract(player.rotation())
				.operate(x -> {
					x %= MathUtil.TAU;
					x += x < -MathUtil.PI ? MathUtil.TAU : 0f;
					x -= x > MathUtil.PI ? MathUtil.TAU : 0f;
					return x;
				});
		float diffLength = diff.length();
		if (diffLength > 0.0075f) {
			diff = diff.scaleToLength(Math.max(diffLength * 0.125f, 0.0075f));
		}
		//player.mutableRotation().add(diff);


		float totalLength = 0;
		for (PuzzleBall puzzleBall : puzzleBalls) {
			puzzleBall.mutableVelocity().add(GRAVITY_VECTOR);
			CollisionPacket.collideAndSlide(puzzleBall.mutablePosition(), puzzleBall.mutableVelocity(), puzzleBall.velocity());
			totalLength += puzzleBall.velocity().length();
		}
		puzzleBalls.removeIf(x -> x.position().y() <= -300f);

		for (PuzzleBall projectile : projectiles) {
			projectile.mutableVelocity().add(GRAVITY_VECTOR);
		}
		projectiles.removeIf(x -> x.position().y() <= 0f);
		projectiles.removeIf(x -> {
			x.mutableVelocity().add(GRAVITY_VECTOR);
			if (CollisionPacket.collideAndSlideIntersect(x.mutablePosition(), x.mutableVelocity())) {
				terrain.generateExplosion(x.position(), 10f);
				return true;
			}
			return false;
		});

		benchmarker.getLineGraph("debugData").add(totalLength);
		float current = Runtime.getRuntime().freeMemory();
		float available = Runtime.getRuntime().totalMemory();
		benchmarker.getLineGraph("freeMemory").add(current);
		benchmarker.getLineGraph("totalMemory").add(available);
		if (GameControls.DEBUG_TOGGLE.isActivated()) {
			debugOverlay.update(
					"Position=[%.02f, %.02f, %.02f], Chunk=[%d, %d, %d], NumTasks=%d, PuzzleBalls=%d, Projectiles=%d",
					player.position().x(),
					player.position().y(),
					player.position().z(),
					terrain.getChunkX(player.position().x()),
					terrain.getChunkY(player.position().y()),
					terrain.getChunkZ(player.position().z()),
					pool.getTaskCount() - pool.getCompletedTaskCount(),
					puzzleBalls.size(),
					projectiles.size());
		}
	}

	public void onMouseScroll(MouseScrollEvent event) {
		playerSpeed += (float) (event.yOffset() / 100f);
		if (playerSpeed < 0) {
			playerSpeed = 0;
		}
		player.camera().getProjection()
				.setFov(player.camera().getProjection().getFov() + ((float) (event.yOffset() / 10000f)));
		updateProjectionMatrices();
	}

	private double lastMouseX;
	private double lastMouseY;
	private double mouseX;
	private double mouseY;
	private static final float MOUSE_SENSITIVITY = 0.001f;

	public void onMousePosition(CursorPositionEvent event) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		mouseX = event.x();
		mouseY = event.y();
		if (GameControls.CAMERA_ROTATE.isActivated()) {
			float deltaY = (float) (-(mouseX - lastMouseX) * MOUSE_SENSITIVITY);
			float deltaX = (float) (-(mouseY - lastMouseY) * MOUSE_SENSITIVITY);
			player.mutableRotation().asXYVector().add(deltaX, deltaY)
					.clampX(-MathUtil.PI / 2f, MathUtil.PI / 2f).modY(MathUtil.TAU);
		}
	}

	public void updateViewMatrices() {
		updateViewMatrix(CommonPrograms3D.COLOR);
		updateViewMatrix(CommonPrograms3D.TEXTURE);
		updateCubeMapMatrix(CommonPrograms3D.CUBEMAP);
		updateViewMatrix(CommonPrograms3D.PARTICLE);
		updateViewMatrix(CommonPrograms3D.LIGHT);
		updateViewMatrix(CommonPrograms3D.TERRAIN);
	}

	public void updateProjectionMatrices() {
		updateProjectionMatrix(CommonPrograms3D.COLOR);
		updateProjectionMatrix(CommonPrograms3D.TEXTURE);
		updateProjectionMatrix(CommonPrograms3D.CUBEMAP);
		updateProjectionMatrix(CommonPrograms3D.PARTICLE);
		updateProjectionMatrix(CommonPrograms3D.LIGHT);
		updateProjectionMatrix(CommonPrograms3D.TERRAIN);
	}

	public void updateViewMatrix(ShaderProgramHolder holder) {
		holder.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.VIEW_MATRIX, player.camera().getTransformationMatrix());
		});
	}

	public void updateCubeMapMatrix(ShaderProgramHolder holder) {
		holder.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.VIEW_MATRIX, player.camera().getInvertedRotationMatrix());
		});
	}

	public void updateProjectionMatrix(ShaderProgramHolder holder) {
		holder.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, player.camera().getProjectionMatrix());
		});
	}

	@Override
	public void render() {
		updateViewMatrices();
		frameBuffer.bind(frameBuffer -> {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glDepthMask(false);
			// render skybox
			CommonPrograms3D.CUBEMAP.getShaderProgram().use(program -> {
				CommonDrawables.SKYBOX.draw();
			});
			GL11.glDepthMask(true);
			for (PuzzleBall puzzleBall : puzzleBalls) {
				puzzleBall.render();
			}
			for (PuzzleBall puzzleBall : projectiles) {
				puzzleBall.render();
			}
			for (Vector3D x : debug) {
				PuzzleBall.render(x, Vector3D.of(0.2f, 0.2f, 0.2f));
			}
			//debug.clear();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			terrainRenderer.render(player.position());
			CommonPrograms3D.LIGHT.getShaderProgram().use(program -> {
				var position = Vector3D.of(96f, 40f, 0f);
				try (var translationMatrix = MatrixPool.ofTranslation(position);
					 var scalarMatrix = MatrixPool.ofScalar(8f, 8f, 8f)) {
					var sunlightDirection = lightPosition.subtract(position).normalize();
					program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(scalarMatrix));
					program.loadVector("sunlightDirection", sunlightDirection);
					program.loadVector("viewPos", player.position());
				}
				dragonModel.draw();
			});
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			FloatBuffer depthPixelBuffer = BufferUtils.createFloatBuffer(1);
			GL11.glReadPixels(window.getWidth() / 2, window.getHeight() / 2, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, depthPixelBuffer);
			depthDistance = depthPixelBuffer.get(); // far plane
		});
		CommonPrograms3D.POST_PROCESSING.getShaderProgram().use(program -> {
			CommonDrawables.TEXTURED_QUAD.draw();
		});
		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			// Render crosshair
			try (var translationMatrix = MatrixPool.ofTranslation(window.getWidth() / 2f, window.getHeight() / 2f, 0f);
				 var scalarMatrixA = MatrixPool.ofScalar(5f, 1f, 1f);
				 var scalarMatrixB = MatrixPool.ofScalar(1f, 5f, 1f);
				 var matrixA = MatrixPool.ofMultiplied(translationMatrix, scalarMatrixA);
				 var matrixB = MatrixPool.ofMultiplied(translationMatrix, scalarMatrixB)) {
				program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrixA);
				CommonDrawables.COLORED_QUAD.draw();
				program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrixB);
				CommonDrawables.COLORED_QUAD.draw();
			}
		});
		//uiScreen.render();
		if (GameControls.DEBUG_TOGGLE.isActivated()) {
			debugOverlay.render();
		}
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
