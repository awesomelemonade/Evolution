package lemon.engine.glfw;

import lemon.engine.input.KeyEvent;
import lemon.engine.math.MathUtil;

public class GLFWKeyEvent implements KeyEvent, GLFWEvent, GLFWKeyMods {
	private long window;
	private int key;
	private int scancode;
	private int action;
	private int mods;

	public GLFWKeyEvent(long window, int key, int scancode, int action, int mods) {
		this.window = window;
		this.key = key;
		this.scancode = scancode;
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
	public int getKey() {
		return key;
	}
	@Override
	public int getScancode() {
		return scancode;
	}
}
