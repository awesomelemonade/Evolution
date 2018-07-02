package lemon.engine.event;

public interface UnregisterListenerEvent extends Event {
	public Listener getListener();
}
