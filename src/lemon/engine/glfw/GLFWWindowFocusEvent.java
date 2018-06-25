package lemon.engine.glfw;

import lemon.engine.input.WindowFocusEvent;

public class GLFWWindowFocusEvent implements WindowFocusEvent, GLFWEvent {
	private long window;
	private boolean focused;

	public GLFWWindowFocusEvent(long window, boolean focused) {
		this.window = window;
		this.focused = focused;
	}
	@Override
	public long getWindow() {
		return window;
	}
	@Override
	public boolean getFocused() {
		return focused;
	}
}
