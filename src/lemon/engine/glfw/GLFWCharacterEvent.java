package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.CharacterEvent;
import lemon.engine.math.MathUtil;

public class GLFWCharacterEvent implements CharacterEvent, GLFWEvent, GLFWKeyMods {
	private GLFWWindow window;
	private int codepoint;
	private int mods;

	public GLFWCharacterEvent(GLFWWindow window, int codepoint, int mods) {
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
	public GLFWWindow getWindow() {
		return window;
	}
}
