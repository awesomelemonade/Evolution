package lemon.engine.evolution;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.opengl.GL20;

import lemon.engine.control.Loader;
import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.control.WindowInitEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.splash.Box2D;
import lemon.engine.splash.LoadingBar;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Toolbox;

public enum Loading implements Listener {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(Game.class.getName());
	private ShaderProgram shaderProgram;
	private LoadingBar loadingBar;
	private Loader loader;
	private long window;
	private boolean started;
	@Subscribe
	public void load(WindowInitEvent event){
		shaderProgram = new ShaderProgram(
				new int[]{0, 1},
				new String[]{"position", "color"},
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/colorVertexShader")),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/colorFragmentShader"))
		);
		this.loader = Game.INSTANCE.getTerrainLoader();
		this.loadingBar = new LoadingBar(this.loader.getPercentage(),
				new Box2D(-1f, -1.1f, 2f, 0.3f),
				new Color(1f, 0f, 0f), new Color(1f, 0f, 0f),
				new Color(0f, 0f, 0f), new Color(0f, 0f, 0f));
		loader.load();
		this.window = event.getWindow();
		started = false;
	}
	@Subscribe
	public void update(UpdateEvent event){
		if(started){
			return;
		}
		if(loader.getPercentage().getPart()!=loader.getPercentage().getWhole()){
			logger.log(Level.INFO, loader.getPercentage().getPercentage()+"%");
		}else{
			start(window);
		}
	}
	@Subscribe
	public void render(RenderEvent event){
		if(started){
			return;
		}
		GL20.glUseProgram(shaderProgram.getId());
		loadingBar.render();
		GL20.glUseProgram(0);
	}
	public void start(long window){
		Game.INSTANCE.init(window);
		EventManager.INSTANCE.registerListener(Game.INSTANCE);
		started = true;
	}
}