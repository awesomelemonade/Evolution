package lemon.evolution;

import com.google.common.collect.Streams;
import lemon.engine.control.GLFWWindow;
import lemon.engine.control.Loader;
import lemon.engine.math.Box2D;
import lemon.engine.math.Matrix;
import lemon.engine.splash.LoadingBar;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.opengl.GL11;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Loading implements Screen {
	private static final Logger logger = Logger.getLogger(Loading.class.getName());
	private Disposables disposables = new Disposables();
	private PerlinBackground background;
	private final Deque<Loader> loaders;
	private final Runnable callback;
	private Instant startTime;
	private LoadingBar loadingBar;

	public Loading(Runnable callback, Collection<? extends Loader> loaders) {
		this.callback = callback;
		this.loaders = new ArrayDeque<>(loaders);
		if (loaders.size() == 0) {
			throw new IllegalStateException("Must have at least 1 loader");
		}
	}

	public Loading(Runnable callback, Loader... loaders) {
		this(callback, Arrays.asList(loaders));
	}

	public Loading(Runnable callback, Collection<? extends Loader> collection, Loader... array) {
		this(callback, Streams.concat(collection.stream(), Arrays.stream(array)).toList());
	}

	@Override
	public void onLoad(GLFWWindow window) {
		this.background = disposables.add(new PerlinBackground(new Box2D(-1, -1, 2, 2)));
		CommonProgramsSetup.setup2D(Matrix.IDENTITY_4);
		this.loadingBar = disposables.add(new LoadingBar(new Box2D(-1f, -1.1f, 2f, 0.3f),
				Color.RED, Color.RED, Color.CLEAR, Color.CLEAR));
		this.loaders.peek().load();
		startTime = Instant.now();
	}

	@Override
	public void update(long deltaTime) {
		if (loaders.isEmpty()) { // Ensures that we wait 1 frame - renders the loading bar full
			callback.run();
		} else {
			var currentLoader = loaders.peek();
			loadingBar.setProgress(currentLoader.getProgress());
			if (currentLoader.isCompleted()) {
				var duration = Duration.between(startTime, Instant.now());
				logger.log(Level.INFO, String.format("Loaded %s in %dm %ds %dms", currentLoader.getDescription(),
						duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart()));
				loaders.poll();
				if (!loaders.isEmpty()) {
					loaders.peek().load();
					startTime = Instant.now();
				}
			}
		}
	}

	@Override
	public void render() {
		background.render();
		CommonPrograms2D.COLOR.use(program -> {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			loadingBar.draw();
			GL11.glDisable(GL11.GL_BLEND);
		});
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
