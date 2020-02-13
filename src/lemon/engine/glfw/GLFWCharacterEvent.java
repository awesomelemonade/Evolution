package lemon.engine.glfw;

import lemon.engine.input.CharacterEvent;
import lemon.engine.math.MathUtil;

public class GLFWCharacterEvent implements CharacterEvent, GLFWEvent, GLFWKeyMods {
	private long window;
	private int codepoint;
	private int mods;

	public GLFWCharacterEvent(long window, int codepoint, int mods) {
		this.window = window;
		this.codepoint = codepoint;
		this.mods = mods;
	}
	@Override
	public int getMods() {
		return mods;
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
