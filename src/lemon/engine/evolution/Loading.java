package lemon.engine.evolution;

import lemon.engine.control.WindowInitEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;

public enum Loading implements Listener {
	INSTANCE;
	@Subscribe
	public void init(WindowInitEvent event){
		Game.INSTANCE.init(event.getWindow());
		EventManager.INSTANCE.registerListener(Game.INSTANCE);
	}
}
