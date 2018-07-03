package lemon.engine.event;

public interface Listener {
	public default void onRegister() {}
	public default void onUnregister() {}
}
