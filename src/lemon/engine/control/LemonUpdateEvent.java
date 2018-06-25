package lemon.engine.control;

public class LemonUpdateEvent implements UpdateEvent {
	private long delta;

	public LemonUpdateEvent(long delta) {
		this.delta = delta;
	}
	@Override
	public long getDelta() {
		return delta;
	}
}
