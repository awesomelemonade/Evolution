package lemon.engine.control;

public class LemonWindowInitEvent implements WindowInitEvent {
	private long window;
	public LemonWindowInitEvent(long window){
		this.window = window;
	}
	@Override
	public long getWindow() {
		return window;
	}
}
