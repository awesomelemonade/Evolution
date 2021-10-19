package lemon.evolution;

import com.google.common.collect.ImmutableList;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.*;
import lemon.engine.toolbox.TaskQueue;
import lemon.engine.control.GLFWWindow;
import lemon.engine.control.Loader;
import lemon.engine.draw.CommonDrawables;
import lemon.engine.draw.Drawable;
import lemon.engine.draw.IndexedDrawable;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.function.MurmurHash;
import lemon.engine.function.PerlinNoise;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.game.Player;
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
import lemon.engine.toolbox.Histogram;
import lemon.engine.toolbox.ObjLoader;
import lemon.engine.toolbox.SkyboxLoader;
import lemon.engine.toolbox.Toolbox;
import lemon.evolution.destructible.beta.ScalarField;
import lemon.evolution.destructible.beta.Terrain;
import lemon.evolution.destructible.beta.TerrainGenerator;
import lemon.evolution.entity.MissileShowerEntity;
import lemon.evolution.entity.PuzzleBall;
import lemon.evolution.entity.RocketLauncherProjectile;
import lemon.evolution.physics.beta.CollisionContext;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.ui.beta.UIScreen;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.util.CommonPrograms3D;
import lemon.evolution.util.GLFWGameControls;
import lemon.evolution.world.Entity;
import lemon.evolution.world.GameLoop;
import lemon.evolution.world.Location;
import lemon.evolution.world.World;
import lemon.evolution.world.WorldRenderer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum Game implements Screen {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(Game.class.getName());

	private GLFWWindow window;
	private boolean loaded;

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

	private Drawable rocketLauncherUnloadedModel;
	private Drawable rocketLauncherLoadedModel;

	private TaskQueue postLoadTasks = TaskQueue.ofConcurrent();

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
			histogram = new Histogram(0.1f);
			ScalarField<Vector3D> scalarField = vector -> {
				if (vector.y() < 0f) {
					return 0f;
				}
				float distanceSquared = vector.x() * vector.x() + vector.z() * vector.z();
				float cylinder = (float) (100.0 - Math.sqrt(distanceSquared));
				if (cylinder < -100f) {
					return cylinder;
				}
				float terrain = (float) (-Math.tanh(vector.y() / 100.0) * 100.0 +
						Math.pow(2f, noise2d.apply(vector.toXZVector().divide(300f))) * 5.0 +
						Math.pow(2.5f, noise.apply(vector.divide(500f))) * 2.5);
				histogram.add(terrain);
				return Math.min(cylinder, terrain);
				//return Math.min(cylinder, -vector.y() + 10f);
			};
			pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
			pool2 = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
			pool.setRejectedExecutionHandler((runnable, executor) -> {});
			pool2.setRejectedExecutionHandler((runnable, executor) -> {});
			disposables.add(() -> pool.shutdown());
			disposables.add(() -> pool2.shutdown());
			TerrainGenerator generator = new TerrainGenerator(pool, scalarField);
			var terrain = new Terrain(generator, pool2, Vector3D.of(1f, 1f, 1f));
			CollisionContext collisionContext = (position, velocity, checker) -> {
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
							terrain.getChunk(i, j, k).getTriangles()
									.ifPresent(triangles -> triangles.forEach(checker));
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
			entityRenderer.registerIndividual(PuzzleBall.class, ball -> {
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
				GL11.glDisable(GL11.GL_BLEND);
			});
			var dragonLoader = new ObjLoader("/res/dragon.obj", postLoadTasks::add,
					objLoader -> {
						var drawable = objLoader.toIndexedDrawable();
					});
			var rocketLauncherUnloadedLoader = new ObjLoader("/res/rocket-launcher-unloaded.obj", postLoadTasks::add,
					objLoader -> rocketLauncherUnloadedModel = objLoader.toIndexedDrawable());
			var rocketLauncherLoadedLoader = new ObjLoader("/res/rocket-launcher-loaded.obj", postLoadTasks::add,
					objLoader -> rocketLauncherLoadedModel = objLoader.toIndexedDrawable());
			var rocketLauncherProjectileLoader = new ObjLoader("/res/rocket-launcher-projectile.obj", postLoadTasks::add,
					objLoader -> {
						var drawable = objLoader.toIndexedDrawable();
						Consumer<Entity> renderer = entity -> {
							GL11.glEnable(GL11.GL_DEPTH_TEST);
							CommonPrograms3D.LIGHT.use(program -> {
								var sunlightDirection = Vector3D.of(0f, 1f, 0f);
								try (var translationMatrix = MatrixPool.ofTranslation(entity.position());
									 var rotationMatrix = MatrixPool.ofLookAt(entity.velocity());
									 var adjustedMatrix = MatrixPool.ofRotationY(MathUtil.PI / 2f);
									 var scalarMatrix = MatrixPool.ofScalar(entity.scalar())) {
									program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(rotationMatrix).multiply(adjustedMatrix).multiply(scalarMatrix));
									program.loadVector("sunlightDirection", sunlightDirection);
									drawable.draw();
								}
							});
							GL11.glDisable(GL11.GL_DEPTH_TEST);
						};
						entityRenderer.registerIndividual(RocketLauncherProjectile.class, renderer);
						entityRenderer.registerIndividual(MissileShowerEntity.class, renderer);
					});
			var foxLoader = new ObjLoader("/res/fox.obj", postLoadTasks::add,
					objLoader -> {
						var drawable = objLoader.toIndexedDrawable();
						entityRenderer.registerCollection(Player.class, players -> {
							for (var player : players) {
								if (player != gameLoop.currentPlayer() || controls.isActivated(EvolutionControls.FREECAM)) {
									GL11.glEnable(GL11.GL_BLEND);
									GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
									GL11.glEnable(GL11.GL_DEPTH_TEST);
									CommonPrograms3D.COLOR.use(program -> {
										try (var translationMatrix = MatrixPool.ofTranslation(player.position());
											 var rotationMatrix = MatrixPool.ofRotationY(player.rotation().y() + MathUtil.PI);
											 var scalarMatrix = MatrixPool.ofScalar(0.45f, 0.45f, 0.45f)) {
											program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(rotationMatrix).multiply(scalarMatrix));
										}
										drawable.draw();
									});
									GL11.glDisable(GL11.GL_DEPTH_TEST);
									GL11.glDisable(GL11.GL_BLEND);
								}
							}
						});
					});

			window.pushScreen(new Loading(window::popScreen,
					dragonLoader, rocketLauncherUnloadedLoader,
					rocketLauncherLoadedLoader, rocketLauncherProjectileLoader,
					foxLoader,
					new Loader() {
				int generatorStartSize;
				@Override
				public void load() {
					worldRenderer.terrainRenderer().preload(Vector3D.ZERO);
					generatorStartSize = Math.max(1, generator.getQueueSize());
				}

				@Override
				public float getProgress() {
					return 1f - ((float) generator.getQueueSize()) / ((float) generatorStartSize);
				}
			}));
			loaded = true;
			return;
		}


		logger.log(Level.FINE, "Initializing");
		postLoadTasks.run();
		this.window = window;
		GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
		disposables.add(() -> GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL));
		var windowWidth = window.getWidth();
		var windowHeight = window.getHeight();

		GLState.pushViewport(0, 0, windowWidth, windowHeight);
		disposables.add(GLState::popViewport);

		benchmarker = new Benchmarker();
		benchmarker.put("updateData", new LineGraph(1000, 100000000));
		benchmarker.put("renderData", new LineGraph(1000, 100000000));
		benchmarker.put("fpsData", new LineGraph(1000, 100));
		benchmarker.put("debugData", new LineGraph(1000, 100));
		benchmarker.put("freeMemory", new LineGraph(1000, 5000000000f));
		benchmarker.put("totalMemory", new LineGraph(1000, 5000000000f));

		debugOverlay = disposables.add(new DebugOverlay(window, benchmarker));

		this.controls = disposables.add(GLFWGameControls.getDefaultControls(window.input(), EvolutionControls.class));
		var projection = new Projection(MathUtil.toRadians(60f),
				((float) window.getWidth()) / ((float) window.getHeight()), 0.01f, 1000f);
		var playersBuilder = new ImmutableList.Builder<Player>();
		int numPlayers = 2;
		for (int i = 0 ; i < numPlayers; i++) {
			var distance = 25f;
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

		Matrix orthoProjectionMatrix = MathUtil.getOrtho(windowWidth, windowHeight, -1, 1);
		CommonProgramsSetup.setup2D(orthoProjectionMatrix);
		CommonProgramsSetup.setup3D(gameLoop.currentPlayer().camera().getProjectionMatrix());

		updateMatrices();

		frameBuffer = disposables.add(new FrameBuffer(windowWidth, windowHeight));
		frameBuffer.bind(frameBuffer -> {
			GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
			Texture colorTexture = disposables.add(new Texture());
			TextureBank.COLOR.bind(() -> {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.id());
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, windowWidth, windowHeight, 0, GL11.GL_RGB,
						GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.id(), 0);
			});
			Texture depthTexture = disposables.add(new Texture());
			TextureBank.DEPTH.bind(() -> {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.id());
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, windowWidth, windowHeight, 0,
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
				texture.load(new TextureData(Toolbox.readImage(path).orElseThrow()));
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.id());
			});
		});

		var paths = new String[] {
				"/res/minecraft-coal.png",
				"/res/minecraft-copper.png",
				"/res/minecraft-diamond.png",
				"/res/minecraft-emerald.png",
				"/res/minecraft-gold.png",
				"/res/minecraft-lapis.png",
				"/res/minecraft-netherite.png",
				"/res/minecraft-redstone.png",
		};
		var textureArray = new Texture();
		textureArray.load(Arrays.stream(paths).map(path -> new TextureData(Toolbox.readImage(path).orElseThrow())).toArray(TextureData[]::new));
		TextureBank.TERRAIN.bind(() -> {
			GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, textureArray.id());
		});

		lightPosition = gameLoop.currentPlayer().position();

		disposables.add(window.input().keyEvent().add(event -> {
			if (event.action() == GLFW.GLFW_RELEASE) {
				if (event.key() == GLFW.GLFW_KEY_C) {
					world.entities().removeIf(x -> x instanceof PuzzleBall || x instanceof RocketLauncherProjectile);
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
			}
		}));

		disposables.add(controls.activated(EvolutionControls.FREECAM).onChangeTo(true, () -> {
			gameLoop.getGatedControls().setEnabled(false);
			MutableVector3D  pos = MutableVector3D.of(gameLoop.currentPlayer().mutablePosition().asImmutable());
			MutableVector3D  rot = MutableVector3D.of(gameLoop.currentPlayer().mutableRotation().asImmutable());
			freecam = new Camera(pos,rot,gameLoop.currentPlayer().camera().getProjection());
		}));
		disposables.add(controls.activated(EvolutionControls.FREECAM).onChangeTo(false, () -> {
			gameLoop.getGatedControls().setEnabled(true);
		}));


		gameLoop.bindNumberKeys(window.input());

		uiScreen = disposables.add(new UIScreen(window.input()));
		uiScreen.addButton(new Box2D(100f, 100f, 100f, 20f), Color.GREEN, x -> {
			System.out.println("Clicked");
		}).visible().setValue(false);
		uiScreen.addWheel(Vector2D.of(200f, 200f), 50f, 0f, Color.RED).visible().setValue(false);
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
		uiScreen.addMinimap(new Box2D(50f, windowHeight - 250f, 200f, 200f), world, () -> gameLoop.currentPlayer());
		uiScreen.addImage(new Box2D(100, 100, 100, 100), "/res/transparency-test.png").visible().setValue(false);

		disposables.add(window.onBenchmark().add(benchmark -> benchmarker.benchmark(benchmark)));
		disposables.add(() -> loaded = false);
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

		benchmarker.getLineGraph("debugData").add(totalLength);
		float current = Runtime.getRuntime().freeMemory();
		float available = Runtime.getRuntime().totalMemory();
		benchmarker.getLineGraph("freeMemory").add(current);
		benchmarker.getLineGraph("totalMemory").add(available);
		if (controls.isActivated(EvolutionControls.DEBUG_TOGGLE)) {
			var player = gameLoop.currentPlayer();
			debugOverlay.update(
					"FPS=%d, Player=%s, Position=[%.02f, %.02f, %.02f], Velocity=%f, Chunk=[%d, %d, %d], NumTasks=%d, %d NumEntities=%d, PlayerSpeed=%f, isOnGround=%s",
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
			worldRenderer.render(gameLoop.currentPlayer().position());
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			CommonPrograms3D.LIGHT.use(program -> {
				var position = Vector3D.of(96f, 40f, 0f);
				try (var translationMatrix = MatrixPool.ofTranslation(position);
					 var scalarMatrix = MatrixPool.ofScalar(8f, 8f, 8f)) {
					var sunlightDirection = lightPosition.subtract(position).normalize();
					program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(scalarMatrix));
					program.loadVector("sunlightDirection", sunlightDirection);
					program.loadVector("viewPos", gameLoop.currentPlayer().position());
				}
				//dragonModel.draw();
			});
			CommonPrograms3D.LIGHT.use(program -> {
				try (var translationMatrix = MatrixPool.ofTranslation(Vector3D.of(3.5f, -4f, 1f));
					 var rotationMatrix = MatrixPool.ofRotationY(MathUtil.PI / 2f)) {
					var sunlightDirection = Vector3D.of(-3.5f, 4f, -1f).normalize();
					program.loadMatrix(MatrixType.MODEL_MATRIX, (rotationMatrix.multiply(translationMatrix)));
					program.loadVector("sunlightDirection", sunlightDirection);
					program.loadVector("viewPos", gameLoop.currentPlayer().position());
					program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
				}
				rocketLauncherLoadedModel.draw();
				//rocketLauncherUnloadedModel.draw();
			});
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			//xray likely here, loop through players without depth test, maybe not render player themself, maybe also above where players are rendered
		});
		CommonPrograms3D.POST_PROCESSING.use(program -> {
			CommonDrawables.TEXTURED_QUAD.draw();
		});
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
