package lemon.game2d;

import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.math.Matrix;
import lemon.evolution.setup.CommonProgramsSetup;

public enum Game2D implements Listener {
	INSTANCE;
	private Player2D player;
	@Override
	public void onRegister() {
		CommonProgramsSetup.setup2D(Matrix.IDENTITY_4);
		player = new Player2D();
	}
	@Subscribe
	public void update(UpdateEvent event) {
		
	}
	@Subscribe
	public void render(RenderEvent event) {
		player.render();
	}
}
