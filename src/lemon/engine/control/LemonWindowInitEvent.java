package lemon.engine.control;

public class LemonWindowInitEvent implements WindowInitEvent {
	private GLFWWindow window;

	public LemonWindowInitEvent(GLFWWindow window) {
		this.window = window;
	}
	@Override
	public GLFWWindow getWindow() {
		return window;
	}
}
