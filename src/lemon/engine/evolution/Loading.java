package lemon.engine.evolution;

import java.util.logging.Level;
import java.util.logging.Logger;

import lemon.engine.control.Loader;
import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.control.WindowInitEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;

public enum Loading implements Listener {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(Game.class.getName());
	private Loader loader;
	private long window;
	private boolean started;
	@Subscribe
	public void load(WindowInitEvent event){
		this.loader = Game.INSTANCE.getTerrainLoader();
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
		
	}
	public void start(long window){
		Game.INSTANCE.init(window);
		EventManager.INSTANCE.registerListener(Game.INSTANCE);
		started = true;
	}
}
