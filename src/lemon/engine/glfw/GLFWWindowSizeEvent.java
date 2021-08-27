package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.WindowSizeEvent;

public class GLFWWindowSizeEvent implements WindowSizeEvent, GLFWEvent {
	private GLFWWindow window;
	private int width;
	private int height;

	public GLFWWindowSizeEvent(GLFWWindow window, int width, int height) {
		this.window = window;
		this.width = width;
		this.height = height;
	}

	@Override
	public GLFWWindow getWindow() {
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
