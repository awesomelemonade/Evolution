package lemon.evolution.util;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.input.KeyEvent;
import lemon.engine.input.MouseButtonEvent;

public class BasicControlActivator {
	private static final Map<Integer, PlayerControl> keyboardHolds;
	private static final Map<Integer, PlayerControl> keyboardToggles;
	private static final Map<Integer, PlayerControl> mouseHolds;
	static {
		keyboardHolds = new HashMap<Integer, PlayerControl>();
		keyboardToggles = new HashMap<Integer, PlayerControl>();
		mouseHolds = new HashMap<Integer, PlayerControl>();
	}

	public static void setup() {
		EventManager.INSTANCE.registerListener(new Listener() {
			@Subscribe
			public void onKeyHold(KeyEvent event) {
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
			}
			@Subscribe
			public void onKeyToggle(KeyEvent event) {
				PlayerControl control = keyboardToggles.get(event.getKey());
				if (control == null) {
					return;
				}
				if (event.getAction() == GLFW.GLFW_RELEASE) {
					control.setActivated(!control.isActivated());
				}
			}
			@Subscribe
			public void onMouse(MouseButtonEvent event) {
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
