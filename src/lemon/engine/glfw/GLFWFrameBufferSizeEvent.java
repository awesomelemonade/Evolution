package lemon.engine.glfw;

import lemon.engine.input.FrameBufferSizeEvent;

public class GLFWFrameBufferSizeEvent implements FrameBufferSizeEvent, GLFWEvent {
	private long window;
	private int width;
	private int height;

	public GLFWFrameBufferSizeEvent(long window, int width, int height) {
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
