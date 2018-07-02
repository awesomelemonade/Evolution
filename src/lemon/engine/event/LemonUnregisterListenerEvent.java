package lemon.engine.event;

public class LemonUnregisterListenerEvent implements UnregisterListenerEvent {
	private Listener listener;
	public LemonUnregisterListenerEvent(Listener listener) {
		this.listener = listener;
	}
	@Override
	public Listener getListener() {
		return listener;
	}
}
