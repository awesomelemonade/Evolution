package lemon.engine.glfw;

import lemon.engine.input.WindowRefreshEvent;

public class GLFWWindowRefreshEvent implements WindowRefreshEvent, GLFWEvent {
	private long window;

	public GLFWWindowRefreshEvent(long window) {
		this.window = window;
	}
	@Override
	public long getWindow() {
		return window;
	}
}
