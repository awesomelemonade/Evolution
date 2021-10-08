package lemon.evolution;

import lemon.engine.glfw.GLFWInput;
import lemon.evolution.util.GLFWGameControls;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;

public enum EvolutionControls {
	CAMERA_ROTATE(mouseHold(GLFW.GLFW_MOUSE_BUTTON_2)),
	MOVE_FORWARDS(keyboardHold(GLFW.GLFW_KEY_W)),
	MOVE_BACKWARDS(keyboardHold(GLFW.GLFW_KEY_S)),
	STRAFE_LEFT(keyboardHold(GLFW.GLFW_KEY_A)),
	STRAFE_RIGHT(keyboardHold(GLFW.GLFW_KEY_D)),
	JUMP(keyboardHold(GLFW.GLFW_KEY_SPACE)),
	CROUCH(keyboardHold(GLFW.GLFW_KEY_LEFT_SHIFT)),
	DEBUG_TOGGLE(keyboardToggle(GLFW.GLFW_KEY_F3)),
	ADD_TERRAIN(keyboardHold(GLFW.GLFW_KEY_T)),
	REMOVE_TERRAIN(keyboardHold(GLFW.GLFW_KEY_Y)),
	END_TURN(keyboardHold(GLFW.GLFW_KEY_BACKSPACE)),
	START_GAME(keyboardHold(GLFW.GLFW_KEY_ENTER)),
	USE_ITEM(mouseHold(GLFW.GLFW_MOUSE_BUTTON_1));

	private final BiConsumer<GLFWGameControls<EvolutionControls>, EvolutionControls> defaultBinder;
	private EvolutionControls(BiConsumer<GLFWGameControls<EvolutionControls>, EvolutionControls> defaultBinder) {
		this.defaultBinder = defaultBinder;
	}

	public static GLFWGameControls<EvolutionControls> getDefaultControls(GLFWInput input) {
		var controls = new GLFWGameControls<EvolutionControls>(input);
		for (var control : EvolutionControls.values()) {
			control.defaultBinder.accept(controls, control);
		}
		return controls;
	}

	private static BiConsumer<GLFWGameControls<EvolutionControls>, EvolutionControls> mouseHold(int key) {
		return (input, self) -> input.bindMouseHold(key, self);
	}

	private static BiConsumer<GLFWGameControls<EvolutionControls>, EvolutionControls> keyboardHold(int key) {
		return (input, self) -> input.bindKeyboardHold(key, self);
	}

	private static BiConsumer<GLFWGameControls<EvolutionControls>, EvolutionControls> keyboardToggle(int key) {
		return (input, self) -> input.bindKeyboardToggle(key, self);
	}
}
