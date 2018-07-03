package lemon.game2d;

import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.physics.CollisionManager;
import lemon.evolution.setup.CommonProgramsSetup;

public enum Game2D implements Listener {
	INSTANCE;
	private CollisionManager manager;
	@Override
	public void onRegister() {
		CommonProgramsSetup.setup2D();
		manager = new CollisionManager();
	}
	@Subscribe
	public void update(UpdateEvent event) {
		
	}
	@Subscribe
	public void render(RenderEvent event) {
		
	}
}
