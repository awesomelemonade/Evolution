package lemon.engine.event;

public class LemonExceptionEvent implements ExceptionEvent {
	private Exception exception;
	public LemonExceptionEvent(Exception exception){
		this.exception = exception;
	}
	public Exception getException(){
		return exception;
	}
}
