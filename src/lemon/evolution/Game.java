package lemon.evolution;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import lemon.engine.control.CleanUpEvent;
import lemon.engine.control.Loader;
import lemon.engine.draw.CommonDrawables;
import lemon.engine.draw.Drawable;
import lemon.engine.draw.TextModel;
import lemon.engine.font.Font;
import lemon.engine.function.MurmurHash;
import lemon.engine.function.PerlinNoise;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.*;
import lemon.engine.texture.TextureData;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.ObjLoader;
import lemon.evolution.destructible.beta.ScalarField;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.destructible.beta.TerrainChunk;
import lemon.evolution.destructible.beta.TerrainGenerator;
import lemon.evolution.physicsbeta.CollisionPacket;
import lemon.evolution.puzzle.PuzzleBall;
import lemon.evolution.ui.beta.UIButton;
import lemon.evolution.ui.beta.UIScreen;
import lemon.evolution.ui.beta.UIWheel;
import lemon.evolution.util.BasicControlActivator;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.util.CommonPrograms3D;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.PlayerControl;
import lemon.evolution.util.ShaderProgramHolder;
import lemon.evolution.pool.VectorPool;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
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
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.time.BenchmarkEvent;
import lemon.engine.time.Benchmarker;
import lemon.evolution.setup.CommonProgramsSetup;

import javax.imageio.ImageIO;

public enum Game implements Listener {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(Game.class.getName());

	private boolean loaded;

	private Player player;

	private FrameBuffer frameBuffer;
	private Texture colorTexture;
	private Texture depthTexture;

	private Benchmarker benchmarker;

	private Terrain terrain;

	private ObjLoader dragonLoader;
	private Drawable dragonModel;
	private Vector3D lightPosition;

	private TextModel debugTextModel;
	private TextModel keyTextModel;

	private int windowWidth;
	private int windowHeight;

	public List<Vector3D> debug;

	private UIScreen uiScreen;

	private ThreadPoolExecutor pool;

	private static final Color[] DEBUG_GRAPH_COLORS = {
			Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.CYAN
	};

	@Override
	public void onRegister() {
		if (!loaded) {
			EventManager.INSTANCE.unregisterListener(this);
			// Prepare loaders
			ToIntFunction<int[]> pairer = (b) -> (int) SzudzikIntPair.pair(b[0], b[1], b[2]);
			var noise2d = new PerlinNoise<Vector2D>(2, MurmurHash::createWithSeed, (b) -> SzudzikIntPair.pair(b[0], b[1]), x -> 1f, 6);
			PerlinNoise<Vector3D> noise = new PerlinNoise<>(3, MurmurHash::createWithSeed, pairer, x -> 1f, 6);
			ScalarField<Vector3D> scalarField = vector -> vector.getY() < -30f ? 0f : -(vector.getY() + noise.apply(vector.divide(100f)) * 5f);
			ThreadLocal<Vector2D> threadLocal = ThreadLocal.withInitial(Vector2D::new);
			scalarField = vector -> {
				Vector2D temp = threadLocal.get();
				temp.setX(vector.getX() / 300f);
				temp.setY(vector.getZ() / 300f);
				float distanceSquared = vector.getX() * vector.getX() + vector.getZ() * vector.getZ();
				float terrain =  -vector.getY() + (float) Math.pow(2f, noise2d.apply(temp)) * 5f + (float) Math.pow(2.5f, noise.apply(vector.divide(500f))) * 2.5f;
				return vector.getY() < 0 ? 0f : Math.min((float) (250.0 - Math.sqrt(distanceSquared)), terrain);
			};
			pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
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
			}, dragonLoader, new Loader() {
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
		//GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, height);
		windowWidth = width.get();
		windowHeight = height.get();

		GL11.glViewport(0, 0, windowWidth, windowHeight);

		benchmarker = new Benchmarker();
		benchmarker.put("updateData", new LineGraph(1000, 100000000));
		benchmarker.put("renderData", new LineGraph(1000, 100000000));
		benchmarker.put("fpsData", new LineGraph(1000, 100));
		benchmarker.put("debugData", new LineGraph(1000, 100));
		benchmarker.put("freeMemory", new LineGraph(1000, 5000000000f));
		benchmarker.put("totalMemory", new LineGraph(1000, 5000000000f));

		player = new Player(new Projection(MathUtil.toRadians(60f),
				((float) windowWidth) / ((float) windowHeight), 0.01f, 1000f));
		player.getPosition().set(0f, 300f, 0f);

		Matrix orthoProjectionMatrix = MathUtil.getOrtho(windowWidth, windowHeight, -1, 1);
		CommonProgramsSetup.setup2D(orthoProjectionMatrix);
		CommonProgramsSetup.setup3D(player.getCamera().getProjectionMatrix());

		updateViewMatrices();

		frameBuffer = new FrameBuffer();
		frameBuffer.bind(frameBuffer -> {
			GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
			colorTexture = new Texture();
			TextureBank.COLOR.bind(() -> {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.getId());
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, windowWidth, windowHeight, 0, GL11.GL_RGB,
						GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.getId(), 0);
			});
			depthTexture = new Texture();
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
		TextureBank.GRASS.bind(() -> {
			try {
				Texture grassTexture = new Texture();
				grassTexture.load(new TextureData(ImageIO.read(Game.class.getResourceAsStream("/res/grass.png"))));
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, grassTexture.getId());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		TextureBank.SLOPE.bind(() -> {
			try {
				Texture slopeTexture = new Texture();
				slopeTexture.load(new TextureData(ImageIO.read(Game.class.getResourceAsStream("/res/slope.png"))));
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, slopeTexture.getId());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		TextureBank.ROCK.bind(() -> {
			try {
				Texture rockTexture = new Texture();
				rockTexture.load(new TextureData(ImageIO.read(Game.class.getResourceAsStream("/res/rock.png"))));
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, rockTexture.getId());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		TextureBank.BASE.bind(() -> {
			try {
				Texture baseTexture = new Texture();
				baseTexture.load(new TextureData(ImageIO.read(Game.class.getResourceAsStream("/res/base.png"))));
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, baseTexture.getId());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});

		GameControls.setup();
		BasicControlActivator.bindKeyboardHold(GLFW.GLFW_KEY_Y, EXPLODE);

		puzzleBalls = new ArrayList<>();
		projectiles = new ArrayList<>();
		int size = 20;
		for (int i = -size; i <= size; i += 5) {
			for (int j = -size; j <= size; j += 5) {
				puzzleBalls.add(new PuzzleBall(new Vector3D(i, 100, j), Vector3D.ZERO.copy()));
			}
		}
		for (int i = 100; i <= 600; i += 10) {
			//puzzleBalls.add(new PuzzleBall(new Vector3D(0, i, 0), new Vector3D(Vector3D.ZERO)));
		}

		debug = new ArrayList<>();
		CollisionPacket.consumers.add((position, velocity) -> collision -> {
			try (var after = VectorPool.of(position, x -> x.add(velocity))) {
				int minChunkX = terrain.getChunkX(Math.min(position.getX(), after.getX()) - 1f);
				int maxChunkX = terrain.getChunkX(Math.max(position.getX(), after.getX()) + 1f);
				int minChunkY = terrain.getChunkY(Math.min(position.getY(), after.getY()) - 1f);
				int maxChunkY = terrain.getChunkY(Math.max(position.getY(), after.getY()) + 1f);
				int minChunkZ = terrain.getChunkZ(Math.min(position.getZ(), after.getZ()) - 1f);
				int maxChunkZ = terrain.getChunkZ(Math.max(position.getZ(), after.getZ()) + 1f);
				for (int i = minChunkX; i <= maxChunkX; i++) {
					for (int j = minChunkY; j <= maxChunkY; j++) {
						for (int k = minChunkZ; k <= maxChunkZ; k++) {
							// TODO: something similar to i * i + j * j + k * k <= indexRadius * indexRadius
							TerrainChunk chunk = terrain.getChunk(i, j, k);
							for (Triangle triangle : chunk.getTriangles()) {
								CollisionPacket.checkTriangle(position, velocity, triangle, collision);
							}
						}
					}
				}
			}
		});
		System.out.println("Triangles: " + CollisionPacket.triangles.size());

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
						line.getOrigin().set(player.getPosition());
						lightPosition.set(player.getPosition());
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
						debug.clear();
					}
					if (event.getKey() == GLFW.GLFW_KEY_K) {
						puzzleBalls.forEach(x -> {
							float spray = 0.2f;
							float randX = (float) (Math.random() * spray - spray / 2f);
							float randZ = (float) (Math.random() * spray - spray / 2f);
							projectiles.add(new PuzzleBall(x.getPosition().copy(), new Vector3D(randX, 0.5f, randZ)));
						});
						/*for (int i = -20; i <= 20; i += 5) {
							for (int j = -20; j <= 20; j += 5) {
								puzzleBalls.add(new PuzzleBall(new Vector3D(i, 100, j), Vector3D.ZERO.copy()));
							}
						}*/
					}
				}
			}
		});

		uiScreen = new UIScreen();
		uiScreen.addComponent(new UIButton(new Box2D(100f, 100f, 100f, 20f), Color.GREEN, x -> {

		}));
		uiScreen.addComponent(new UIWheel(new Vector2D(200f, 200f), 50f, 0f, Color.RED));
	}
	private PlayerControl EXPLODE = new PlayerControl();
	public void generateExplosionAtCrosshair() {
		if (depthDistance != 1f) {
			float realDistance = (float) (0.00997367 * Math.pow(1.0 - depthDistance + 0.0000100616, -1.00036));
			Vector3D position = player.getPosition().copy().add(player.getVectorDirection().multiply(realDistance));
			terrain.generateExplosion(position, 5f);
		}
	}
	float depthDistance = 0;

	private List<PuzzleBall> puzzleBalls;
	private List<PuzzleBall> projectiles;

	private static float friction = 1f;
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
		player.getVelocity().add(GRAVITY_VECTOR);

		CollisionPacket.collideAndSlide(player.getPosition(), player.getVelocity(), 20);

		try (var targetRotation = VectorPool.of((float) Math.atan(player.getVelocity().getY() / Math.hypot(player.getVelocity().getX(), player.getVelocity().getZ())),
				(float) (Math.PI + Math.atan2(player.getVelocity().getX(), player.getVelocity().getZ())), 0f);
				var diff = VectorPool.of(targetRotation, x -> x.subtract(player.getCamera().getRotation()))) {
			diff.operate(x -> {
				x %= MathUtil.TAU;
				x += x < -MathUtil.PI ? MathUtil.TAU : 0f;
				x -= x > MathUtil.PI ? MathUtil.TAU : 0f;
				return x;
			});
			float diffLength = diff.getLength();
			if (diffLength > 0.0075f) {
				diff.scaleToLength(Math.max(diffLength * 0.125f, 0.0075f));
			}
			player.getCamera().getRotation().add(diff);
		}


		float totalLength = 0;
		for (PuzzleBall puzzleBall : puzzleBalls) {
			puzzleBall.getVelocity().add(GRAVITY_VECTOR);
			CollisionPacket.collideAndSlide(puzzleBall.getPosition(), puzzleBall.getVelocity());
			totalLength += puzzleBall.getVelocity().getLength();
		}
		puzzleBalls.removeIf(x -> x.getPosition().getY() <= -300f);

		for (PuzzleBall projectile : projectiles) {
			projectile.getVelocity().add(GRAVITY_VECTOR);
		}
		projectiles.removeIf(x -> x.getPosition().getY() <= 0f);
		projectiles.removeIf(x -> {
			x.getVelocity().add(GRAVITY_VECTOR);
			if (CollisionPacket.collideAndSlideIntersect(x.getPosition(), x.getVelocity())) {
				terrain.generateExplosion(x.getPosition(), 10f);
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
		    debugMessage.setLength(0);
			debugFormatter.format("Listeners Registered=%d, Methods=%d, Preloaded=%d, VectorPool=%d, Position=[%.02f, %.02f, %.02f], Chunk=[%d, %d, %d], NumTasks=%d",
					EventManager.INSTANCE.getListenersRegistered(),
					EventManager.INSTANCE.getListenerMethodsRegistered(),
					EventManager.INSTANCE.getPreloadedMethodsRegistered(),
					VectorPool.getCount(),
					player.getPosition().getX(),
					player.getPosition().getY(),
					player.getPosition().getZ(),
					terrain.getChunkX(player.getPosition().getX()),
					terrain.getChunkY(player.getPosition().getY()),
					terrain.getChunkZ(player.getPosition().getZ()),
					pool.getTaskCount() - pool.getCompletedTaskCount());
			debugTextModel.setText(debugMessage);
		}
	}
	private StringBuilder debugMessage = new StringBuilder();
	private Formatter debugFormatter = new Formatter(debugMessage);
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
			for (PuzzleBall puzzleBall : puzzleBalls) {
				puzzleBall.render();
			}
			for (PuzzleBall puzzleBall : projectiles) {
				puzzleBall.render();
			}
			for (Vector3D x : debug) {
				try (var translation = VectorPool.of(x);
					 var scalar = VectorPool.of(0.2f, 0.2f, 0.2f)) {
					PuzzleBall.render(translation, scalar);
				}
			}
			//debug.clear();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			CommonPrograms3D.TERRAIN.getShaderProgram().use(program -> {
				//GL11.glEnable(GL11.GL_CULL_FACE);
				//GL11.glCullFace(GL11.GL_FRONT);
				int playerChunkX = terrain.getChunkX(player.getPosition().getX());
				int playerChunkY = terrain.getChunkX(player.getPosition().getY());
				int playerChunkZ = terrain.getChunkX(player.getPosition().getZ());
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
				//GL11.glDisable(GL11.GL_CULL_FACE);
			});
			CommonPrograms3D.LIGHT.getShaderProgram().use(program -> {
				try (var position = VectorPool.of(96f, 40f, 0f);
					 var translationMatrix = MatrixPool.ofTranslation(position);
					 var scalarMatrix = MatrixPool.ofScalar(8f, 8f, 8f);
					 var sunlightDirection = VectorPool.of(lightPosition, v -> v.subtract(position).normalize())) {
					program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(scalarMatrix));
					program.loadVector("sunlightDirection", sunlightDirection);
					program.loadVector("viewPos", player.getPosition());
				}
				dragonModel.draw();
			});
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			FloatBuffer depthPixelBuffer = BufferUtils.createFloatBuffer(1);
			GL11.glReadPixels(windowWidth / 2, windowHeight / 2, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, depthPixelBuffer);
			depthDistance = depthPixelBuffer.get(); // far plane
		});
		CommonPrograms3D.POST_PROCESSING.getShaderProgram().use(program -> {
			CommonDrawables.TEXTURED_QUAD.draw();
		});
		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			// Render crosshair
			try (var translationMatrix = MatrixPool.ofTranslation(windowWidth / 2f, windowHeight / 2f, 0f);
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
			// render graphs
			byte counter = 0;
			for (String benchmarker : this.benchmarker.getNames()) {
				Color color = DEBUG_GRAPH_COLORS[counter % DEBUG_GRAPH_COLORS.length];
				CommonPrograms2D.LINE.getShaderProgram().use(program -> {
					program.loadColor3f(color);
					program.loadFloat("spacing",
							2f / (this.benchmarker.getLineGraph(benchmarker).getSize() - 1));
					this.benchmarker.getLineGraph(benchmarker).render();
				});
				byte finalCounter = counter;
				CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
					program.loadColor4f("filterColor", color);
					try (var translationMatrix = MatrixPool.ofTranslation(windowWidth - 200f, windowHeight - 50f - (finalCounter * 30f), 0f);
						 var scalarMatrix = MatrixPool.ofScalar(10f, 10f, 1f);
						 var transformationMatrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
						program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, transformationMatrix);
					}
					CommonDrawables.COLORED_QUAD.draw();
					program.loadColor4f("filterColor", Color.WHITE);
				});
				CommonPrograms2D.TEXT.getShaderProgram().use(program -> {
					try (var translationMatrix = MatrixPool.ofTranslation(windowWidth - 170f, windowHeight - 55f - (finalCounter * 30f), 0f);
						 var scalarMatrix = MatrixPool.ofScalar(0.2f, 0.2f, 1f);
						 var transformationMatrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
						program.loadMatrix(MatrixType.MODEL_MATRIX, transformationMatrix);
					}
					program.loadColor3f("color", Color.WHITE);
					keyTextModel.setText(benchmarker);
					keyTextModel.draw();
				});
				counter++;
			}
			// render debug text
			CommonPrograms2D.TEXT.getShaderProgram().use(program -> {
				try (var translationMatrix = MatrixPool.ofTranslation(5f, windowHeight - 20, 0f);
					 var scalarMatrix = MatrixPool.ofScalar(0.2f, 0.2f, 1f);
					 var transformationMatrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
					program.loadMatrix(MatrixType.MODEL_MATRIX, transformationMatrix);
				}
				program.loadColor3f("color", Color.WHITE);
				debugTextModel.draw();
			});
		}
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
	private Line line = new Line();
	@Subscribe
	public void onClick(MouseButtonEvent event) {
		if (event.getAction() == GLFW.GLFW_RELEASE) {
			if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_2) {
				generateExplosionAtCrosshair();
			}
		}
	}
}
