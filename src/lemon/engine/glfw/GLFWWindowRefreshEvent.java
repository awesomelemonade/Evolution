package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.WindowRefreshEvent;

public class GLFWWindowRefreshEvent implements WindowRefreshEvent, GLFWEvent {
	private GLFWWindow window;

	public GLFWWindowRefreshEvent(GLFWWindow window) {
		this.window = window;
	}

	@Override
	public GLFWWindow getWindow() {
		return window;
	}
}
