package lemon.evolution;

import com.google.common.collect.ImmutableList;
import lemon.engine.control.GLFWWindow;
import lemon.engine.control.Loader;
import lemon.engine.draw.CommonDrawables;
import lemon.engine.draw.IndexedDrawable;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.game.Player;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.Box2D;
import lemon.engine.math.Camera;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector2D;
import lemon.engine.math.Vector3D;
import lemon.engine.model.LineGraph;
import lemon.engine.model.Model;
import lemon.engine.model.SphereModelBuilder;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.texture.TextureData;
import lemon.engine.time.Benchmarker;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposables;
import lemon.engine.toolbox.GLState;
import lemon.engine.toolbox.SkyboxLoader;
import lemon.engine.toolbox.TaskQueue;
import lemon.engine.toolbox.Toolbox;
import lemon.evolution.destructible.beta.ScalarField;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.destructible.beta.TerrainChunk;
import lemon.evolution.destructible.beta.TerrainGenerator;
import lemon.evolution.entity.*;
import lemon.evolution.particle.beta.ParticleSystem;
import lemon.evolution.physics.beta.CollisionContext;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.ui.beta.UIScreen;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.util.CommonPrograms3D;
import lemon.evolution.util.GLFWGameControls;
import lemon.evolution.world.CsvWorldLoader;
import lemon.evolution.world.Entity;
import lemon.evolution.world.GameLoop;
import lemon.evolution.world.Location;
import lemon.evolution.world.World;
import lemon.evolution.world.WorldRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Game implements Screen {
	private static final Logger logger = Logger.getLogger(Game.class.getName());

	private GameResources resources;

	private GLFWWindow window;
	private boolean loaded = false;
	private boolean initialized = false;

	private GLFWGameControls<EvolutionControls> controls;
	private GameLoop gameLoop;

	private Camera freecam;
	private float lastMouseX;
	private float lastMouseY;

	private FrameBuffer frameBuffer;

	private DebugOverlay debugOverlay;
	private Benchmarker benchmarker;

	private World world;
	private WorldRenderer worldRenderer;

	private Vector3D lightPosition;

	private ViewModel viewModel;

	private TaskQueue postLoadTasks = TaskQueue.ofConcurrent();

	private ParticleSystem particleSystem;

	private UIScreen uiScreen;

	private ThreadPoolExecutor pool;
	private ThreadPoolExecutor pool2;

	private final Disposables disposables = new Disposables();

	private ScalarField<Vector3D> scalarfield;
	private int resolutionWidth;
	private int resolutionHeight;

	public Game(ScalarField<Vector3D> scalarfield, int resolutionWidth, int resolutionHeight) {
		this.scalarfield = scalarfield;
		this.resolutionWidth = resolutionWidth;
		this.resolutionHeight = resolutionHeight;
	}

	@Override
	public void onLoad(GLFWWindow window) {
		if (!loaded) {
			// Prepare loaders
			ScalarField<Vector3D> scalarField = vector -> -1f;
			pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
			pool2 = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
			pool.setRejectedExecutionHandler((runnable, executor) -> {});
			pool2.setRejectedExecutionHandler((runnable, executor) -> {});
			disposables.add(() -> pool.shutdown());
			disposables.add(() -> pool2.shutdown());
			TerrainGenerator generator = new TerrainGenerator(pool, scalarField);
			var terrain = new Terrain(generator, pool2, Vector3D.of(0.5f, 0.5f, 0.5f));
			CollisionContext collisionContext = (position, velocity, checker) -> {
				var after = position.add(velocity);
				int minCollideX = terrain.getCollideX(Math.min(position.x(), after.x()) - 1f);
				int maxCollideX = terrain.getCollideX(Math.max(position.x(), after.x()) + 1f);
				int minCollideY = terrain.getCollideY(Math.min(position.y(), after.y()) - 1f);
				int maxCollideY = terrain.getCollideY(Math.max(position.y(), after.y()) + 1f);
				int minCollideZ = terrain.getCollideZ(Math.min(position.z(), after.z()) - 1f);
				int maxCollideZ = terrain.getCollideZ(Math.max(position.z(), after.z()) + 1f);
				for (int i = minCollideX; i <= maxCollideX; i++) {
					for (int j = minCollideY; j <= maxCollideY; j++) {
						for (int k = minCollideZ; k <= maxCollideZ; k++) {
							terrain.getTriangles(i, j, k).forEach(checker);
						}
					}
				}
			};
			world = disposables.add(new World(terrain, collisionContext));
			worldRenderer = disposables.add(new WorldRenderer(world));

			var entityRenderer = worldRenderer.entityRenderer();
			var sphereDrawable = SphereModelBuilder.of(1, 5)
					.build((indices, vertices) -> {
						Color[] colors = new Color[vertices.length];
						for (int i = 0; i < colors.length; i++) {
							colors[i] = Color.randomOpaque();
						}
						return new Model(indices, vertices, colors);
					}).map(IndexedDrawable::new);
			Consumer<Entity> sphereRenderer = ball -> {
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				CommonPrograms3D.COLOR.use(program -> {
					try (var translationMatrix = MatrixPool.ofTranslation(ball.position());
						 var scalarMatrix = MatrixPool.ofScalar(ball.scalar());
						 var transformationMatrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
						program.loadMatrix(MatrixType.MODEL_MATRIX, transformationMatrix);
					}
					sphereDrawable.draw();
				});
				GL11.glDisable(GL11.GL_DEPTH_TEST);
			};
			// Render spheres
			entityRenderer.registerIndividual(PuzzleBall.class, sphereRenderer);
			entityRenderer.registerIndividual(ExplodeOnTimeProjectile.class, entity -> entity.isType(ExplodeType.GRENADE), sphereRenderer);
			entityRenderer.registerIndividual(ExplodeOnHitProjectile.class, entity -> entity.isType(ExplodeType.RAIN_DROPLET), sphereRenderer);

			var csvLoader = new CsvWorldLoader("/res/SkullIsland.csv", world.terrain(), postLoadTasks::add,
					csvWorldLoader -> {
						var mapping = csvWorldLoader.blockMapping();
						var materials = new MCMaterial[mapping.size()];
						mapping.forEach((material, index) -> materials[index] = material);
						if (materials.length > TerrainChunk.NUM_TEXTURES) {
							for (int i = TerrainChunk.NUM_TEXTURES; i < materials.length; i++) {
								logger.warning("Not enough textures for " + materials[i] + " (" + (i + 1) + ")");
							}
						}
						var textureArray = new Texture();
						textureArray.load(Arrays.stream(materials)
								.map(material -> "/res/block/" + material.textureFile().orElseGet(() -> {
									logger.warning("No texture for " + material);
									return "diamond_block.png";
								}))
								.map(path -> new TextureData(Toolbox.readImage(path)
								.orElseThrow(() -> new IllegalStateException("Cannot find " + path)), true))
								.toArray(TextureData[]::new));
						TextureBank.TERRAIN.bind(() -> {
							GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, textureArray.id());
						});
					});

			this.controls = disposables.add(GLFWGameControls.getDefaultControls(window.input(), EvolutionControls.class));
			var projection = new Projection(MathUtil.toRadians(60f),
					((float) window.getWidth()) / ((float) window.getHeight()), 0.01f, 1000f);
			var playersBuilder = new ImmutableList.Builder<Player>();
			int numPlayers = 8;
			for (int i = 0; i < numPlayers; i++) {
				var distance = 15f;
				var angle = MathUtil.TAU * ((float) i) / numPlayers;
				var cos = (float) Math.cos(angle);
				var sin = (float) Math.sin(angle);
				var player = disposables.add(new Player("Player " + (i + 1), new Location(world, Vector3D.of(distance * cos, 100f, distance * sin)), projection));
				player.mutableRotation().setY((float) Math.atan2(player.position().y(), player.position().x()));
				playersBuilder.add(player);
			}
			var players = playersBuilder.build();
			world.entities().addAll(players);
			world.entities().flush();
			gameLoop = disposables.add(new GameLoop(players, controls));
			disposables.add(gameLoop.onWinner(player -> {
				window.popAndPushScreen(Menu.INSTANCE);
			}));

			resources = new GameResources(window, worldRenderer, gameLoop, controls);

			window.pushScreen(new Loading(window::popScreen, resources.loaders(postLoadTasks::add),
					new Loader() {
				int generatorStartSize;
				@Override
				public void load() {
					var currentRenderDistance = worldRenderer.terrainRenderer().getRenderDistance();
					postLoadTasks.add(() -> worldRenderer.terrainRenderer().setRenderDistance(currentRenderDistance));
					worldRenderer.terrainRenderer().setRenderDistance(256f / TerrainChunk.SIZE);
					worldRenderer.terrainRenderer().preload(Vector3D.ZERO);
					generatorStartSize = Math.max(1, generator.getQueueSize());
				}

				@Override
				public float getProgress() {
					return 1f - ((float) generator.getQueueSize()) / ((float) generatorStartSize);
				}
			}, csvLoader, new Loader() {
				int poolStartSize;
				@Override
				public void load() {
					poolStartSize = Math.max(1, pool2.getQueue().size());
				}

				@Override
				public float getProgress() {
					return 1f - ((float) pool2.getQueue().size()) / ((float) poolStartSize);
				}
			}, new Loader() {
				int poolStartSize;
				@Override
				public void load() {
					worldRenderer.terrainRenderer().preinit(Vector3D.ZERO);
					poolStartSize = Math.max(1, pool2.getQueue().size());
				}

				@Override
				public float getProgress() {
					return 1f - ((float) pool2.getQueue().size()) / ((float) poolStartSize);
				}
			}));
			loaded = true;
			return;
		}

		if (!initialized) {
			logger.fine("Initializing");
			postLoadTasks.run();
			this.window = window;
			this.viewModel = resources.viewModel();
			GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
			disposables.add(() -> GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL));
			var windowWidth = window.getWidth();
			var windowHeight = window.getHeight();

			GLState.pushViewport(0, 0, windowWidth, windowHeight);
			disposables.add(GLState::popViewport);

			benchmarker = new Benchmarker();
			benchmarker.put("update", new LineGraph(1000, 100000000));
			benchmarker.put("render", new LineGraph(1000, 100000000));
			benchmarker.put("pollEvents", new LineGraph(1000, 100000000));
			benchmarker.put("fps", new LineGraph(1000, 100));
			benchmarker.put("debug", new LineGraph(1000, 100));
			benchmarker.put("freeMemory", new LineGraph(1000, 5000000000f));
			benchmarker.put("totalMemory", new LineGraph(1000, 5000000000f));
			benchmarker.put("worldRenderTime", new LineGraph(1000, 100000000));
			benchmarker.put("particleTime", new LineGraph(1000, 100000000));

			debugOverlay = disposables.add(new DebugOverlay(window, benchmarker));

			Matrix orthoProjectionMatrix = MathUtil.getOrtho(windowWidth, windowHeight, -1, 1);
			CommonProgramsSetup.setup2D(orthoProjectionMatrix);
			CommonProgramsSetup.setup3D(gameLoop.currentPlayer().camera().getProjectionMatrix());

			updateMatrices();

			frameBuffer = disposables.add(new FrameBuffer(resolutionWidth, resolutionHeight));
			frameBuffer.bind(frameBuffer -> {
				GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
				Texture colorTexture = disposables.add(new Texture());
				TextureBank.COLOR.bind(() -> {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.id());
					GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, resolutionWidth, resolutionHeight, 0, GL11.GL_RGB,
							GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
					GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
					GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
					GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.id(), 0);
				});
				Texture depthTexture = disposables.add(new Texture());
				TextureBank.DEPTH.bind(() -> {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.id());
					GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, resolutionWidth, resolutionHeight, 0,
							GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
					GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
					GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
					GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthTexture.id(), 0);
				});
			});
			TextureBank.SKYBOX.bind(() -> {
				Texture skyboxTexture = new Texture();
				skyboxTexture.load(new SkyboxLoader("/res/darkskies", "darkskies.cfg").load());
				GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, skyboxTexture.id());
			});
			Map.of(
					TextureBank.GRASS, "/res/grass.png",
					TextureBank.SLOPE, "/res/slope.png",
					TextureBank.ROCK, "/res/rock.png",
					TextureBank.BASE, "/res/base.png"
			).forEach((textureBank, path) -> {
				textureBank.bind(() -> {
					var texture = new Texture();
					texture.load(new TextureData(Toolbox.readImage(path).orElseThrow(), true));
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.id());
				});
			});


			lightPosition = gameLoop.currentPlayer().position();

			var texture = new Texture();
			texture.load(new TextureData(Toolbox.readImage("/res/particles/fire_01.png").orElseThrow(), true));
			particleSystem = disposables.add(new ParticleSystem(100000, texture));
			disposables.add(world.onExplosion((position, radius) -> particleSystem.addExplosionParticles(position, radius)));

			disposables.add(window.input().keyEvent().add(event -> {
				if (event.action() == GLFW.GLFW_RELEASE) {
					if (event.key() == GLFW.GLFW_KEY_C) {
						world.entities().removeIf(x -> x instanceof PuzzleBall || x instanceof ExplodeOnHitProjectile || x instanceof StaticEntity);
					}
					if (event.key() == GLFW.GLFW_KEY_H) {
						var ballSize = (float) (Math.random());
						int size = 20;
						for (int i = -size; i <= size; i += 5) {
							for (int j = -size; j <= size; j += 5) {
								world.entities().add(new PuzzleBall(new Location(world, Vector3D.of(i, 100, j)), Vector3D.ZERO, Vector3D.of(ballSize, ballSize, ballSize)));
							}
						}
					}
					if (event.key() == GLFW.GLFW_KEY_R) {
						world.entities().add(new StaticEntity(gameLoop.currentPlayer().location(), MathUtil.randomChoice(StaticEntity.Type.values())));
					}
					if (event.key() == GLFW.GLFW_KEY_T) {
						world.entities().add(new ItemDropEntity(gameLoop.currentPlayer().location().add(Vector3D.of(0f, 100f, 0f))));
					}
				}
			}));

			disposables.add(controls.activated(EvolutionControls.FREECAM).onChangeTo(true, () -> {
				gameLoop.getGatedControls().setEnabled(false);
				MutableVector3D pos = MutableVector3D.of(gameLoop.currentPlayer().mutablePosition().asImmutable());
				MutableVector3D rot = MutableVector3D.of(gameLoop.currentPlayer().mutableRotation().asImmutable());
				freecam = new Camera(pos, rot, gameLoop.currentPlayer().camera().getProjection());
				viewModel.setVisible(false);
			}));
			disposables.add(controls.activated(EvolutionControls.FREECAM).onChangeTo(false, () -> {
				gameLoop.getGatedControls().setEnabled(true);
				viewModel.setVisible(true);
			}));

			uiScreen = disposables.add(new UIScreen(window.input()));
			//disposables.add(controls.activated(EvolutionControls.SHOW_UI).onChangeAndRun(activated -> uiScreen.visible().setValue(activated)));

			var progressBar = uiScreen.addProgressBar(new Box2D(10f, 10f, windowWidth - 20f, 25f), () -> {
				if (gameLoop.startTime != null && gameLoop.endTime != null) {
					float progressedTime = Duration.between(gameLoop.startTime, Instant.now()).toMillis();
					float totalTime = Duration.between(gameLoop.startTime, gameLoop.endTime).toMillis();
					return progressedTime / totalTime;
				} else {
					return 0f;
				}
			});
			disposables.add(gameLoop.started().onChangeAndRun(started -> progressBar.visible().setValue(started)));

			uiScreen.addProgressBar(new Box2D(10f, 45f, windowWidth - 20f, 25f),
					() -> gameLoop.controller().powerMeter()).visible().setValue(false);

			var minimap = uiScreen.addMinimap(new Box2D(50f, windowHeight - 250f, 200f, 200f), world, () -> gameLoop.currentPlayer());
			disposables.add(controls.activated(EvolutionControls.MINIMAP).onChangeAndRun(visible -> {
				minimap.visible().setValue(visible);
			}));

			for (int i = 0; i < gameLoop.players().size(); i++) {
				var player = gameLoop.players().get(i);
				uiScreen.addProgressBar(new Box2D(50f, windowHeight - 280f - i * 30f, 100f, 15f),
						() -> player.health().getValue() / Player.START_HEALTH).visible().setValue(false);
			}

			var uiInventory = uiScreen.addInventory(gameLoop.currentPlayer().inventory());
			uiInventory.visible().setValue(false);
			disposables.add(gameLoop.observableCurrentPlayer().onChange(player -> {
				var inventory = player.inventory();
				uiInventory.visible().setValue(false);
				uiInventory.setInventory(inventory);
			}));
			// sets up inventory toggle
			disposables.add(controls.onActivated(EvolutionControls.TOGGLE_INVENTORY, () -> {
				uiInventory.visible().setValue(!uiInventory.visible().getValue());
				if (uiInventory.isVisible()) {
					GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
					gameLoop.getGatedControls().setEnabled(false);
				} else {
					GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
					gameLoop.getGatedControls().setEnabled(true);
				}
			}));

			controls.onActivated(EvolutionControls.SCREENSHOT, () -> {
				try {
					var buffer = BufferUtils.createIntBuffer(windowWidth * windowHeight);
					GL11.glReadPixels(0, 0, windowWidth, windowHeight, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
					int[] array = new int[buffer.remaining()];
					buffer.get(array);
					var image = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_ARGB);
					image.setRGB(0, 0, windowWidth, windowHeight, array, 0, windowWidth);
					ImageIO.write(image, "png", new File("evolution-screenshot.png"));
				} catch (IOException e) {
					logger.warning(e.getMessage());
				}
			});

			disposables.add(window.onBenchmark().add(benchmark -> benchmarker.benchmark(benchmark)));
			disposables.add(() -> loaded = false);
			disposables.add(() -> initialized = false);
		}
	}

	@Override
	public void update(long deltaTime) {
		float dt = (float) (((double) deltaTime) / 3.0e7);

		var totalLength = world.entities().stream().map(entity -> entity instanceof PuzzleBall ball ? ball.velocity().length() : 0f).reduce(0f, Float::sum);
		world.entities().removeIf(entity -> entity instanceof PuzzleBall ball && ball.position().y() <= -300f);
		world.update(dt);

		gameLoop.update();
		if (controls.isActivated(EvolutionControls.FREECAM)) {
			float MOUSE_SENSITIVITY = .001f;
			controls.addCallback(GLFWInput::cursorPositionEvent, event -> {
				if (controls.isActivated(EvolutionControls.CAMERA_ROTATE)) {
					float deltaY = (float) (-(event.x() - lastMouseX) * MOUSE_SENSITIVITY);
					float deltaX = (float) (-(event.y() - lastMouseY) * MOUSE_SENSITIVITY);
					freecam.mutableRotation().asXYVector().add(deltaX, deltaY)
							.clampX(-MathUtil.PI / 2f, MathUtil.PI / 2f).modY(MathUtil.TAU);
				}
				lastMouseX = (float) event.x();
				lastMouseY = (float) event.y();
			});

			float speed = .5f;
			float angle = (freecam.rotation().y() + MathUtil.PI / 2f);
			float sin = (float) Math.sin(angle);
			float cos = (float) Math.cos(angle);
			var playerHorizontalVector = Vector2D.of(speed * sin, speed * cos);
			var mutableForce = MutableVector3D.ofZero();
			if (controls.isActivated(EvolutionControls.STRAFE_LEFT)) {
				mutableForce.asXZVector().subtract(playerHorizontalVector);
			}
			if (controls.isActivated(EvolutionControls.STRAFE_RIGHT)) {
				mutableForce.asXZVector().add(playerHorizontalVector);
			}
			var playerForwardVector = Vector2D.of(speed * cos, -speed * sin);
			if (controls.isActivated(EvolutionControls.MOVE_FORWARDS)) {
				mutableForce.asXZVector().add(playerForwardVector);
			}
			if (controls.isActivated(EvolutionControls.MOVE_BACKWARDS)) {
				mutableForce.asXZVector().subtract(playerForwardVector);
			}
			if (controls.isActivated(EvolutionControls.FLY)) {
				mutableForce.addY(speed);
			}
			if (controls.isActivated(EvolutionControls.FALL)) {
				mutableForce.subtractY(speed);
			}
			freecam.mutablePosition().add(mutableForce.asImmutable());
		}

		benchmarker.getLineGraph("debug").add(totalLength);
		float current = Runtime.getRuntime().freeMemory();
		float available = Runtime.getRuntime().totalMemory();
		benchmarker.getLineGraph("freeMemory").add(current);
		benchmarker.getLineGraph("totalMemory").add(available);
		if (controls.isActivated(EvolutionControls.DEBUG_TOGGLE)) {
			var player = gameLoop.currentPlayer();
			debugOverlay.update(
					"FPS=%d, Player=%s, Pos=[%.02f, %.02f, %.02f], Vel=%f, Chunk=[%d, %d, %d], NumTasks=%d, %d, ChunkCount=%d, NumEntities=%d, PlayerSpeed=%f, isOnGround=%s",
					window.timeSync().getFps(),
					player.name(),
					player.position().x(),
					player.position().y(),
					player.position().z(),
					player.velocity().length(),
					world.terrain().getChunkX(player.position().x()),
					world.terrain().getChunkY(player.position().y()),
					world.terrain().getChunkZ(player.position().z()),
					pool.getTaskCount() - pool.getCompletedTaskCount(),
					pool2.getTaskCount() - pool2.getCompletedTaskCount(),
					world.terrain().chunkCount(),
					world.entities().size(),
					gameLoop.controller().playerSpeed(),
					player.groundWatcher().isOnGround() ? "true" : "false");
		}
	}

	public void updateMatrices() {
		Camera camera;
		if (controls.isActivated(EvolutionControls.FREECAM)) {
			camera = freecam;
		} else {
			camera = gameLoop.currentPlayer().camera();
		}
		CommonPrograms3D.setMatrices(MatrixType.VIEW_MATRIX, camera.getTransformationMatrix());
		CommonPrograms3D.setMatrices(MatrixType.PROJECTION_MATRIX, camera.getProjectionMatrix());
		CommonPrograms3D.CUBEMAP.use(program -> {
			CommonPrograms3D.CUBEMAP.loadMatrix(MatrixType.VIEW_MATRIX, camera.getInvertedRotationMatrix());
		});
	}

	@Override
	public void render() {
		updateMatrices();
		frameBuffer.bind(frameBuffer -> {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glDepthMask(false);
			// render skybox
			CommonPrograms3D.CUBEMAP.use(program -> {
				CommonDrawables.SKYBOX.draw();
			});
			GL11.glDepthMask(true);
			var worldRenderTime = System.nanoTime();
			worldRenderer.render(gameLoop.currentPlayer().position());
			worldRenderTime = System.nanoTime() - worldRenderTime;
			benchmarker.getLineGraph("worldRenderTime").add(worldRenderTime);
			var particleTime = System.nanoTime();
			particleSystem.render(gameLoop.currentPlayer().position());
			particleTime = System.nanoTime() - particleTime;
			benchmarker.getLineGraph("particleTime").add(particleTime);
		});
		CommonPrograms3D.POST_PROCESSING.use(program -> {
			CommonDrawables.TEXTURED_QUAD.draw();
		});
		viewModel.render();
		CommonPrograms2D.COLOR.use(program -> {
			// Render crosshair
			try (var translationMatrix = MatrixPool.ofTranslation(window.getWidth() / 2f, window.getHeight() / 2f, 0f);
				 var scalarMatrixA = MatrixPool.ofScalar(5f, 1f, 1f);
				 var scalarMatrixB = MatrixPool.ofScalar(1f, 5f, 1f);
				 var matrixA = MatrixPool.ofMultiplied(translationMatrix, scalarMatrixA);
				 var matrixB = MatrixPool.ofMultiplied(translationMatrix, scalarMatrixB)) {
				program.loadColor4f("filterColor", Color.WHITE);
				program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrixA);
				CommonDrawables.COLORED_QUAD.draw();
				program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrixB);
				CommonDrawables.COLORED_QUAD.draw();
			}
		});
		uiScreen.render();
		if (controls.isActivated(EvolutionControls.DEBUG_TOGGLE)) {
			debugOverlay.render();
		}
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
