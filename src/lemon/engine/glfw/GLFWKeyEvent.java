package lemon.engine.glfw;

import lemon.engine.input.KeyEvent;
import lemon.engine.math.MathUtil;

public class GLFWKeyEvent implements KeyEvent, GLFWEvent {
	private long window;
	private int key;
	private int scancode;
	private int action;
	private boolean[] mods;

	public GLFWKeyEvent(long window, int key, int scancode, int action, int mods) {
		this.window = window;
		this.key = key;
		this.scancode = scancode;
		this.action = action;
		this.mods = MathUtil.convertMods(mods);
	}
	@Override
	public boolean isShiftDown() {
		return mods[0];
	}
	@Override
	public boolean isAltDown() {
		return mods[1];
	}
	@Override
	public boolean isCtrlDown() {
		return mods[2];
	}
	@Override
	public boolean isCommandDown() {
		return mods[3];
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
