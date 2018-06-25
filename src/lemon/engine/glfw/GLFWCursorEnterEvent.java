package lemon.engine.glfw;

import lemon.engine.input.CursorEnterEvent;

public class GLFWCursorEnterEvent implements CursorEnterEvent, GLFWEvent {
	private long window;
	private boolean entered;

	public GLFWCursorEnterEvent(long window, boolean entered) {
		this.window = window;
		this.entered = entered;
	}
	@Override
	public boolean hasEntered() {
		return entered;
	}
	@Override
	public long getWindow() {
		return window;
	}
}
