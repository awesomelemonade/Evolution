package lemon.engine.event;

public interface ExceptionEvent extends Event {
	public Exception getException();
}
