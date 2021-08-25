package lemon.evolution;

import lemon.engine.glfw.GLFWInput;
import org.lwjgl.glfw.GLFW;

import lemon.evolution.util.BasicControlActivator;
import lemon.evolution.util.PlayerControl;

public class GameControls {
	public static final PlayerControl CAMERA_ROTATE = new PlayerControl();
	public static final PlayerControl MOVE_FORWARDS = new PlayerControl();
	public static final PlayerControl MOVE_BACKWARDS = new PlayerControl();
	public static final PlayerControl STRAFE_LEFT = new PlayerControl();
	public static final PlayerControl STRAFE_RIGHT = new PlayerControl();
	public static final PlayerControl MOVE_UP = new PlayerControl();
	public static final PlayerControl MOVE_DOWN = new PlayerControl();
	public static final PlayerControl DEBUG_TOGGLE = new PlayerControl();

	public static void setup(GLFWInput input) {
		BasicControlActivator.setup(input);
		BasicControlActivator.bindMouseHolds(GLFW.GLFW_MOUSE_BUTTON_1, CAMERA_ROTATE);
		BasicControlActivator.bindKeyboardHold(GLFW.GLFW_KEY_W, MOVE_FORWARDS);
		BasicControlActivator.bindKeyboardHold(GLFW.GLFW_KEY_S, MOVE_BACKWARDS);
		BasicControlActivator.bindKeyboardHold(GLFW.GLFW_KEY_A, STRAFE_LEFT);
		BasicControlActivator.bindKeyboardHold(GLFW.GLFW_KEY_D, STRAFE_RIGHT);
		BasicControlActivator.bindKeyboardHold(GLFW.GLFW_KEY_SPACE, MOVE_UP);
		BasicControlActivator.bindKeyboardHold(GLFW.GLFW_KEY_LEFT_SHIFT, MOVE_DOWN);
		BasicControlActivator.bindKeyboardToggle(GLFW.GLFW_KEY_F3, DEBUG_TOGGLE);
	}
}
