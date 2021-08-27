package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.CursorPositionEvent;

public class GLFWCursorPositionEvent implements CursorPositionEvent, GLFWEvent {
	private GLFWWindow window;
	private double x;
	private double y;

	public GLFWCursorPositionEvent(GLFWWindow window, double x, double y) {
		this.window = window;
		this.x = x;
		this.y = y;
	}

	@Override
	public GLFWWindow getWindow() {
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
