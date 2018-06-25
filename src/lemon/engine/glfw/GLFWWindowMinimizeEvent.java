package lemon.engine.glfw;

import lemon.engine.input.WindowMinimizeEvent;

public class GLFWWindowMinimizeEvent implements WindowMinimizeEvent, GLFWEvent {
	private long window;
	private boolean minimized;

	public GLFWWindowMinimizeEvent(long window, boolean minimized) {
		this.window = window;
		this.minimized = minimized;
	}
	@Override
	public long getWindow() {
		return window;
	}
	@Override
	public boolean isMinimized() {
		return minimized;
	}
}
