package lemon.evolution.util;

import lemon.engine.glfw.GLFWInput;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class BasicControlActivator {
	private static final Map<Integer, PlayerControl> keyboardHolds;
	private static final Map<Integer, PlayerControl> keyboardToggles;
	private static final Map<Integer, PlayerControl> mouseHolds;

	static {
		keyboardHolds = new HashMap<>();
		keyboardToggles = new HashMap<>();
		mouseHolds = new HashMap<>();
	}

	public static void setup(GLFWInput input) {
		input.keyEvent().add(event -> {
			PlayerControl control = keyboardHolds.get(event.getKey());
			if (control == null) {
				return;
			}
			if (event.getAction() == GLFW.GLFW_PRESS) {
				control.setActivated(true);
			}
			if (event.getAction() == GLFW.GLFW_RELEASE) {
				control.setActivated(false);
			}
		});
		input.keyEvent().add(event -> {
			PlayerControl control = keyboardToggles.get(event.getKey());
			if (control == null) {
				return;
			}
			if (event.getAction() == GLFW.GLFW_RELEASE) {
				control.setActivated(!control.isActivated());
			}
		});
		input.mouseButtonEvent().add(event -> {
			PlayerControl control = mouseHolds.get(event.getButton());
			if (control == null) {
				return;
			}
			if (event.getAction() == GLFW.GLFW_PRESS) {
				control.setActivated(true);
			}
			if (event.getAction() == GLFW.GLFW_RELEASE) {
				control.setActivated(false);
			}
		});
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
