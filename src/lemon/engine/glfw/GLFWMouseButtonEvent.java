package lemon.engine.glfw;

import lemon.engine.input.MouseButtonEvent;
import lemon.engine.math.MathUtil;

public class GLFWMouseButtonEvent implements MouseButtonEvent, GLFWEvent {
	private boolean[] mods;
	private long window;
	private int button;
	private int action;
	public GLFWMouseButtonEvent(long window, int button, int action, int mods){
		this.window = window;
		this.button = button;
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
	public int getButton() {
		return button;
	}
}
