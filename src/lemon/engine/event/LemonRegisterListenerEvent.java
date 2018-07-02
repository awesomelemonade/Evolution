package lemon.engine.event;

public class LemonRegisterListenerEvent implements RegisterListenerEvent {
	private Listener listener;
	public LemonRegisterListenerEvent(Listener listener) {
		this.listener = listener;
	}
	@Override
	public Listener getListener() {
		return listener;
	}
}
