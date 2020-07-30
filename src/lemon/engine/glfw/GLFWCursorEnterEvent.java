package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.CursorEnterEvent;

public class GLFWCursorEnterEvent implements CursorEnterEvent, GLFWEvent {
	private GLFWWindow window;
	private boolean entered;

	public GLFWCursorEnterEvent(GLFWWindow window, boolean entered) {
		this.window = window;
		this.entered = entered;
	}
	@Override
	public boolean hasEntered() {
		return entered;
	}
	@Override
	public GLFWWindow getWindow() {
		return window;
	}
}
