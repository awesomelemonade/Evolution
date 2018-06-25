package lemon.engine.glfw;

import lemon.engine.input.WindowMoveEvent;

public class GLFWWindowMoveEvent implements WindowMoveEvent, GLFWEvent {
	private long window;
	private int x;
	private int y;

	public GLFWWindowMoveEvent(long window, int x, int y) {
		this.window = window;
		this.x = x;
		this.y = y;
	}
	@Override
	public long getWindow() {
		return window;
	}
	@Override
	public int getX() {
		return x;
	}
	@Override
	public int getY() {
		return y;
	}
}
