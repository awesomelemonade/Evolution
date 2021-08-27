package lemon.engine.glfw;

import lemon.engine.input.KeyMods;
import org.lwjgl.glfw.GLFW;

public interface GLFWKeyMods extends KeyMods {
	@Override
	public default boolean isShiftDown() {
		return (mods() & GLFW.GLFW_MOD_SHIFT) != 0;
	}

	@Override
	public default boolean isAltDown() {
		return (mods() & GLFW.GLFW_MOD_ALT) != 0;
	}

	@Override
	public default boolean isCtrlDown() {
		return (mods() & GLFW.GLFW_MOD_CONTROL) != 0;
	}

	@Override
	public default boolean isCommandDown() {
		return (mods() & GLFW.GLFW_MOD_SUPER) != 0;
	}

	public int mods();
}
