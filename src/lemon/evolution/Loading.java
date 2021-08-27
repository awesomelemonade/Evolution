package lemon.evolution;

import java.util.logging.Level;
import java.util.logging.Logger;

import lemon.engine.control.GLFWWindow;
import lemon.engine.math.Matrix;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;

import lemon.engine.control.Loader;
import lemon.engine.math.Box2D;
import lemon.engine.splash.LoadingBar;
import lemon.engine.toolbox.Color;

public class Loading implements Screen {
	private static final Logger logger = Logger.getLogger(Loading.class.getName());
	private LoadingBar loadingBar;
	private Loader[] loaders;
	private int loaderIndex; // index of loaders
	private Runnable callback;

	public Loading(Runnable callback, Loader... loaders) {
		this.callback = callback;
		this.loaders = loaders;
		this.loaderIndex = 0;
		if (loaders.length == 0) {
			throw new IllegalStateException("Must have at least 1 loader");
		}
	}

	@Override
	public void onLoad(GLFWWindow window) {
		CommonProgramsSetup.setup2D(Matrix.IDENTITY_4);
		this.loadingBar = new LoadingBar(loaders[loaderIndex].getPercentage(),
				new Box2D(-1f, -1.1f, 2f, 0.3f),
				new Color(1f, 0f, 0f), new Color(1f, 0f, 0f),
				new Color(0f, 0f, 0f), new Color(0f, 0f, 0f));
		loaders[loaderIndex].load();
	}

	@Override
	public void update() {
		if (loaderIndex >= loaders.length) { // Ensures that we wait 1 frame - renders the loading bar full
			callback.run();
		} else {
			Loader currentLoader = loaders[loaderIndex];
			if (currentLoader.isCompleted()) {
				logger.log(Level.INFO, String.format("Loaded %s", currentLoader.toString()));
				loaderIndex++;
				if (loaderIndex < loaders.length) {
					loadingBar.setPercentage(loaders[loaderIndex].getPercentage());
					loaders[loaderIndex].load();
				}
			}
		}
	}

	@Override
	public void render() {
		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			loadingBar.render();
		});
	}

	@Override
	public void dispose() {

	}
}
