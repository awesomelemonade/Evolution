package lemon.engine.game;

import lemon.engine.control.InitEvent;

public class LemonInitEvent implements InitEvent {
	private long window;
	public LemonInitEvent(long window){
		this.window = window;
	}
	@Override
	public long getWindow() {
		return window;
	}
}
