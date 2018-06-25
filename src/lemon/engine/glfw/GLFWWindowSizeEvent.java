package lemon.engine.glfw;

import lemon.engine.input.WindowSizeEvent;

public class GLFWWindowSizeEvent implements WindowSizeEvent, GLFWEvent {
	private long window;
	private int width;
	private int height;

	public GLFWWindowSizeEvent(long window, int width, int height) {
		this.window = window;
		this.width = width;
		this.height = height;
	}
	@Override
	public long getWindow() {
		return window;
	}
	@Override
	public int getWidth() {
		return width;
	}
	@Override
	public int getHeight() {
		return height;
	}
}
