package lemon.engine.glfw;

import lemon.engine.input.CharacterEvent;
import lemon.engine.math.MathUtil;

public class GLFWCharacterEvent implements CharacterEvent, GLFWEvent {
	private long window;
	private int codepoint;
	private boolean[] mods;
	public GLFWCharacterEvent(long window, int codepoint, int mods){
		this.window = window;
		this.codepoint = codepoint;
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
	public int getCodepoint() {
		return codepoint;
	}
	@Override
	public long getWindow() {
		return window;
	}
}
