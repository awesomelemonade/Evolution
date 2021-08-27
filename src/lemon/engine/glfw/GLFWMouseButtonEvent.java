package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.MouseButtonEvent;

public class GLFWMouseButtonEvent implements MouseButtonEvent, GLFWEvent, GLFWKeyMods {
	private GLFWWindow window;
	private int button;
	private int action;
	private int mods;

	public GLFWMouseButtonEvent(GLFWWindow window, int button, int action, int mods) {
		this.window = window;
		this.button = button;
		this.action = action;
		this.mods = mods;
	}

	@Override
	public int getMods() {
		return mods;
	}

	@Override
	public int getAction() {
		return action;
	}

	@Override
	public GLFWWindow getWindow() {
		return window;
	}

	@Override
	public int getButton() {
		return button;
	}
}
