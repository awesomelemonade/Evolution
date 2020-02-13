package lemon.engine.glfw;

import lemon.engine.input.MouseButtonEvent;
import lemon.engine.math.MathUtil;

public class GLFWMouseButtonEvent implements MouseButtonEvent, GLFWEvent, GLFWKeyMods {
	private long window;
	private int button;
	private int action;
	private int mods;

	public GLFWMouseButtonEvent(long window, int button, int action, int mods) {
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
	public long getWindow() {
		return window;
	}
	@Override
	public int getButton() {
		return button;
	}
}
