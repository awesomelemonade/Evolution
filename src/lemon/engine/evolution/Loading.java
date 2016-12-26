package lemon.engine.evolution;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.opengl.GL20;

import lemon.engine.control.Loader;
import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.game2d.Box2D;
import lemon.engine.math.Matrix;
import lemon.engine.render.MatrixType;
import lemon.engine.splash.LoadingBar;
import lemon.engine.toolbox.Color;

public enum Loading implements Listener {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(Game.class.getName());
	private LoadingBar loadingBar;
	private Loader loader;
	private long window;
	public void start(long window){
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		GL20.glUseProgram(0);
		this.loader = Game.INSTANCE.getTerrainLoader();
		this.loadingBar = new LoadingBar(this.loader.getPercentage(),
				new Box2D(-1f, -1.1f, 2f, 0.3f),
				new Color(1f, 0f, 0f), new Color(1f, 0f, 0f),
				new Color(0f, 0f, 0f), new Color(0f, 0f, 0f));
		loader.load();
		this.window = window;
		EventManager.INSTANCE.registerListener(this);
	}
	@Subscribe
	public void update(UpdateEvent event){
		if(loader.getPercentage().getPart()!=loader.getPercentage().getWhole()){
			logger.log(Level.INFO, loader.getPercentage().getPercentage()+"%");
		}else{
			startGame(window);
		}
	}
	@Subscribe
	public void render(RenderEvent event){
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		loadingBar.render();
		GL20.glUseProgram(0);
	}
	public void startGame(long window){
		Game.INSTANCE.init(window);
		EventManager.INSTANCE.unregisterListener(this);
	}
}
