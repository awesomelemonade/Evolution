package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.WindowMinimizeEvent;

public class GLFWWindowMinimizeEvent implements WindowMinimizeEvent, GLFWEvent {
	private GLFWWindow window;
	private boolean minimized;

	public GLFWWindowMinimizeEvent(GLFWWindow window, boolean minimized) {
		this.window = window;
		this.minimized = minimized;
	}

	@Override
	public GLFWWindow getWindow() {
		return window;
	}

	@Override
	public boolean isMinimized() {
		return minimized;
	}
}
