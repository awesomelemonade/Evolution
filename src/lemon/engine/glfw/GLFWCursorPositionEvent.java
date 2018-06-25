package lemon.engine.glfw;

import lemon.engine.input.CursorPositionEvent;

public class GLFWCursorPositionEvent implements CursorPositionEvent, GLFWEvent {
	private long window;
	private double x;
	private double y;

	public GLFWCursorPositionEvent(long window, double x, double y) {
		this.window = window;
		this.x = x;
		this.y = y;
	}
	@Override
	public long getWindow() {
		return window;
	}
	@Override
	public double getX() {
		return x;
	}
	@Override
	public double getY() {
		return y;
	}
}
