package lemon.engine.glfw;

import lemon.engine.input.WindowCloseEvent;

public class GLFWWindowCloseEvent implements WindowCloseEvent, GLFWEvent {
	private long window;

	public GLFWWindowCloseEvent(long window) {
		this.window = window;
	}
	@Override
	public long getWindow() {
		return window;
	}
}
