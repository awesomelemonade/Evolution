package lemon.evolution.util;

import com.google.errorprone.annotations.CheckReturnValue;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class BasicControlActivator {
	private static final Map<Integer, PlayerControl> keyboardHolds = new HashMap<>();
	private static final Map<Integer, PlayerControl> keyboardToggles = new HashMap<>();
	private static final Map<Integer, PlayerControl> mouseHolds = new HashMap<>();

	@CheckReturnValue
	public static Disposable setup(GLFWInput input) {
		var kHolds = input.keyEvent().add(event -> {
			PlayerControl control = keyboardHolds.get(event.key());
			if (control == null) {
				return;
			}
			if (event.action() == GLFW.GLFW_PRESS) {
				control.setActivated(true);
			}
			if (event.action() == GLFW.GLFW_RELEASE) {
				control.setActivated(false);
			}
		});
		var kToggles = input.keyEvent().add(event -> {
			PlayerControl control = keyboardToggles.get(event.key());
			if (control == null) {
				return;
			}
			if (event.action() == GLFW.GLFW_RELEASE) {
				control.setActivated(!control.isActivated());
			}
		});
		var mHolds = input.mouseButtonEvent().add(event -> {
			PlayerControl control = mouseHolds.get(event.button());
			if (control == null) {
				return;
			}
			if (event.action() == GLFW.GLFW_PRESS) {
				control.setActivated(true);
			}
			if (event.action() == GLFW.GLFW_RELEASE) {
				control.setActivated(false);
			}
		});
		return Disposable.of(kHolds, kToggles, mHolds);
	}

	public static void bindKeyboardHold(int key, PlayerControl control) {
		keyboardHolds.put(key, control);
	}

	public static void bindKeyboardToggle(int key, PlayerControl control) {
		keyboardToggles.put(key, control);
	}

	public static void bindMouseHolds(int key, PlayerControl control) {
		mouseHolds.put(key, control);
	}
}
