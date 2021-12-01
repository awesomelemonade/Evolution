package lemon.evolution;

import com.google.common.collect.ImmutableList;
import lemon.engine.control.GLFWWindow;
import lemon.engine.control.Loader;
import lemon.engine.draw.CommonDrawables;
import lemon.engine.draw.IndexedDrawable;
import lemon.engine.draw.TextModel;
import lemon.engine.event.Observable;
import lemon.engine.font.CommonFonts;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.game.Player;
import lemon.engine.game.Team;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.*;
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
import lemon.evolution.audio.BackgroundAudio;
import lemon.evolution.destructible.beta.ScalarField;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.destructible.beta.TerrainChunk;
import lemon.evolution.destructible.beta.TerrainGenerator;
import lemon.evolution.entity.*;
import lemon.evolution.item.BasicItems;
import lemon.evolution.particle.beta.ParticleSystem;
import lemon.evolution.physics.beta.CollisionContext;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.ui.beta.UIProgressBar;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class Game implements Screen {
	private static final Logger logger = Logger.getLogger(Game.class.getName());

	private GameResources resources;

	private GLFWWindow window;
	private boolean loaded = false;
	private boolean initialized = false;

	private GLFWGameControls<EvolutionControls> controls;
	private GameLoop gameLoop;

	private AggregateCamera camera;
	private FreeCamera freecam;

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

	private int resolutionWidth;
	private int resolutionHeight;
	private MapInfo map;

	private Map<String, TextModel> cachedTextModels = new HashMap<>();

	public Game(int resolutionWidth, int resolutionHeight, MapInfo map) {
		this.resolutionWidth = resolutionWidth;
		this.resolutionHeight = resolutionHeight;
		this.map = map;
	}

	@Override
	public void onLoad(GLFWWindow window) {
		if (!loaded) {
			// Music
			BackgroundAudio.play(BackgroundAudio.Track.COMBAT);
			disposables.add(() -> BackgroundAudio.play(BackgroundAudio.Track.MENU));
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
			world = disposables.add(new World(terrain, collisionContext, map));
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
			entityRenderer.registerIndividual(TeleportBallEntity.class, sphereRenderer);

			var csvLoader = new CsvWorldLoader("/res/" + map.csvPath(), world.terrain(), postLoadTasks::add,
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
						logger.info(csvWorldLoader.materialCount().entrySet().stream()
								.filter(entry -> !entry.getKey().isEmpty() && entry.getKey().textureFile().isEmpty()).toList().toString());
					});

			this.controls = disposables.add(GLFWGameControls.getDefaultControls(window.input(), EvolutionControls.class));
			var projection = new Projection(MathUtil.toRadians(60f),
					((float) window.getWidth()) / ((float) window.getHeight()), 0.01f, 1000f);

			var namesList = new NamesList("/res/names.txt");

			var playersBuilder = new ImmutableList.Builder<Player>();
			int numPlayers = 6;
			for (int i = 0; i < numPlayers; i++) {
				var distance = map.playerSpawnRadius();
				var angle = MathUtil.TAU * ((float) i) / numPlayers;
				var cos = (float) Math.cos(angle);
				var sin = (float) Math.sin(angle);
				var player = disposables.add(new Player(namesList.random(), Team.values()[i % Team.values().length], new Location(world, Vector3D.of(distance * cos, 100f, distance * sin)), projection));
				player.mutableRotation().setY(-angle + MathUtil.PI / 2);
				playersBuilder.add(player);
			}
			var players = playersBuilder.build();
			world.entities().addAll(players);
			world.entities().flush();
			gameLoop = disposables.add(new GameLoop(map, world, players, controls));
			disposables.add(gameLoop.onWinner(player -> {
				logger.info(player.team() + " won");
				window.popAndPushScreen(Menu.INSTANCE);
			}));

			camera = new AggregateCamera(gameLoop.currentPlayer().camera());
			freecam = new FreeCamera(camera, controls);
			resources = new GameResources(window, worldRenderer, gameLoop, controls, camera);

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

			disposables.add(gameLoop.observableCurrentPlayer().onChange(player -> {
				camera.interpolateTo(player.camera());
			}));
			disposables.add(controls.activated(EvolutionControls.FREECAM).onChangeTo(true, () -> {
				if (camera.camera() == freecam) {
					camera.interpolateTo(gameLoop.currentPlayer().camera());
				} else {
					freecam.setPositionAndRotation(gameLoop.currentPlayer().camera());
					camera.set(freecam);
				}
			}));
			var observableNotInControl = new Observable<>(false);
			disposables.add(camera.observableCamera().onChangeAndRun(camera -> {
				var inControl = camera == gameLoop.currentPlayer().camera();
				observableNotInControl.setValue(!inControl);
				viewModel.setVisible(inControl);
			}));
			disposables.add(gameLoop.getGatedControls().gate().addInput(observableNotInControl));

			Matrix orthoProjectionMatrix = MathUtil.getOrtho(windowWidth, windowHeight, -1, 1);
			CommonProgramsSetup.setup2D(orthoProjectionMatrix);
			CommonProgramsSetup.setup3D(camera.projectionMatrix());

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
				skyboxTexture.load(new SkyboxLoader("/res/" + map.skyboxInfo().directoryPath()).load());
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
					if (event.key() == GLFW.GLFW_KEY_L) {
						for (var player : gameLoop.players()) {
							for (var item : BasicItems.values()) {
								player.inventory().addItems(item, 100);
							}
						}
					}
					if (event.key() == GLFW.GLFW_KEY_T) {
						world.entities().add(new ItemDropEntity(gameLoop.currentPlayer().location().add(Vector3D.of(0f, 100f, 0f))));
					}
				}
			}));

			uiScreen = disposables.add(new UIScreen(window.input()));
			disposables.add(controls.activated(EvolutionControls.SHOW_UI).onChangeAndRun(activated -> uiScreen.visible().setValue(activated)));

			var minimap = uiScreen.addMinimap(new Box2D(50f, windowHeight - 250f, 200f, 200f), world, () -> gameLoop.currentPlayer());
			disposables.add(controls.activated(EvolutionControls.MINIMAP).onChangeAndRun(visible -> {
				minimap.visible().setValue(visible);
			}));

			var playerInfoHeight = 35f;
			for (int i = 0; i < gameLoop.players().size(); i++) {
				var player = gameLoop.players().get(i);
				uiScreen.addPlayerInfo(new Box2D(50f, windowHeight - 250f - (i + 1) * (playerInfoHeight + 5f), 180f, playerInfoHeight), player);
			}

			Supplier<Float> progressGetter = () -> {
				if (gameLoop.startTime != null && gameLoop.endTime != null) {
					float progressedTime = Duration.between(gameLoop.startTime, Instant.now()).toMillis();
					float totalTime = Duration.between(gameLoop.startTime, gameLoop.endTime).toMillis();
					return progressedTime / totalTime;
				} else {
					return 0f;
				}
			};
			var bottomInfoHeight = 35f;
			var bottomInfoMargin = 10f;
			var powerMeterWidth = 180f;
			var progressBox = new Box2D(bottomInfoMargin, bottomInfoMargin, windowWidth - 3f * bottomInfoMargin - powerMeterWidth, bottomInfoHeight);
			var progressInfo = uiScreen.addPlayerInfo(progressBox, "Turn Timer", " ", Color.RED, progressGetter);

			var powerMeterBox = new Box2D(windowWidth - powerMeterWidth - bottomInfoMargin, bottomInfoMargin, powerMeterWidth, bottomInfoHeight);
			var powerMeterInfo = uiScreen.addPlayerInfo(powerMeterBox, "Power Meter", " ", Color.RED, () -> gameLoop.controller().powerMeter());

			disposables.add(gameLoop.started().onChangeAndRun(started -> {
				progressInfo.visible().setValue(started);
				powerMeterInfo.visible().setValue(started);
			}));
			disposables.add(gameLoop.observableCurrentPlayer().onChangeAndRun(player -> {
				var color = player.team().color();
				progressInfo.progressBar().setColor(color);
				powerMeterInfo.progressBar().setColor(color);
			}));

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
			}));
			disposables.add(uiInventory.visible().onChangeAndRun(visible -> {
				if (visible) {
					GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
				} else {
					GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
				}
			}));
			disposables.add(gameLoop.getGatedControls().gate().addInput(uiInventory.visible()));

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
		freecam.update();

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
		var position = camera.position();
		var rotation = camera.rotation();
		var translationMatrix = MathUtil.getTranslation(position.invert());
		var rotationMatrix = MathUtil.getRotation(rotation.invert());
		var transformationMatrix = rotationMatrix.multiply(translationMatrix);
		CommonPrograms3D.setMatrices(MatrixType.VIEW_MATRIX, transformationMatrix);
		CommonPrograms3D.setMatrices(MatrixType.PROJECTION_MATRIX, camera.projectionMatrix());
		CommonPrograms3D.CUBEMAP.use(program -> {
			CommonPrograms3D.CUBEMAP.loadMatrix(MatrixType.VIEW_MATRIX, rotationMatrix);
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

			GL11.glEnable(GL11.GL_DEPTH_TEST);
			for (var player : gameLoop.players()) {
				if (!player.alive().getValue()) {
					continue;
				}
				var model = cachedTextModels.computeIfAbsent(player.name(), name -> new TextModel(CommonFonts.freeSansTightened(), name));
				CommonPrograms3D.TEXT.use(program -> {
					var scale = 0.3f / model.height();
					var initialTranslation = MathUtil.getTranslation(Vector3D.of(-model.width() / 2f, 0f, 0f));
					var translation = player.position().add(Vector3D.of(0f, 0.9f, 0f));
					var translationMatrix = MathUtil.getTranslation(translation);
					var delta = player.position().subtract(gameLoop.currentPlayer().position());
					var angle = delta.isZero() ? 0f : ((float) (Math.atan2(-delta.z(), delta.x()) - Math.PI / 2.0));
					if (player == gameLoop.currentPlayer()) {
						var scaledTime = (System.currentTimeMillis() % 20000) / 20000.0f;
						angle = scaledTime * MathUtil.TAU;
					}
					var rotationMatrix = MathUtil.getRotationY(angle);
					var scalarMatrix = MathUtil.getScalar(Vector3D.of(scale, scale, scale));

					program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(scalarMatrix.multiply(rotationMatrix.multiply(initialTranslation))));
					model.draw();
				});
			}
			GL11.glDisable(GL11.GL_DEPTH_TEST);
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
