package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.WindowFocusEvent;

public class GLFWWindowFocusEvent implements WindowFocusEvent, GLFWEvent {
	private GLFWWindow window;
	private boolean focused;

	public GLFWWindowFocusEvent(GLFWWindow window, boolean focused) {
		this.window = window;
		this.focused = focused;
	}

	@Override
	public GLFWWindow getWindow() {
		return window;
	}

	@Override
	public boolean getFocused() {
		return focused;
	}
}
