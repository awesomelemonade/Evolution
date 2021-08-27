package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.WindowCloseEvent;

public class GLFWWindowCloseEvent implements WindowCloseEvent, GLFWEvent {
	private GLFWWindow window;

	public GLFWWindowCloseEvent(GLFWWindow window) {
		this.window = window;
	}

	@Override
	public GLFWWindow getWindow() {
		return window;
	}
}
