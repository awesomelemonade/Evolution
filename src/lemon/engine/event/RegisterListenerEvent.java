package lemon.engine.event;

public interface RegisterListenerEvent extends Event {
	public Listener getListener();
}
