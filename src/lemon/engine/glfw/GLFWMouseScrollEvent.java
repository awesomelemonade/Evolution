package lemon.engine.glfw;

import lemon.engine.input.MouseScrollEvent;

public class GLFWMouseScrollEvent implements MouseScrollEvent, GLFWEvent {
	private long window;
	private double xOffset;
	private double yOffset;

	public GLFWMouseScrollEvent(long window, double xOffset, double yOffset) {
		this.window = window;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	@Override
	public long getWindow() {
		return window;
	}
	@Override
	public double getXOffset() {
		return xOffset;
	}
	@Override
	public double getYOffset() {
		return yOffset;
	}
}
