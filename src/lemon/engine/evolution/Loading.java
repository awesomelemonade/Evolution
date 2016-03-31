package lemon.engine.evolution;

import java.util.logging.Level;
import java.util.logging.Logger;

import lemon.engine.control.Loader;
import lemon.engine.control.WindowInitEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;

public enum Loading implements Listener {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(Game.class.getName());
	@Subscribe
	public void init(WindowInitEvent event){
		Loader loader = Game.INSTANCE.getTerrainLoader();
		loader.load();
		while(loader.getPercentage().getPart()!=loader.getPercentage().getWhole()){
			logger.log(Level.INFO, loader.getPercentage().getPercentage()+"%");
			try{
				Thread.sleep(1000);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		Game.INSTANCE.init(event.getWindow());
		EventManager.INSTANCE.registerListener(Game.INSTANCE);
	}
}
