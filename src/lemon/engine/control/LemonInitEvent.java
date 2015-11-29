package lemon.engine.control;

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
