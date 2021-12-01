package lemon.evolution;

import lemon.evolution.util.GLFWGameControls;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;

import static lemon.evolution.util.GLFWGameControls.DefaultBinder.*;

public enum EvolutionControls implements GLFWGameControls.DefaultBinder<EvolutionControls> {
	CAMERA_ROTATE(mouseHold(GLFW.GLFW_MOUSE_BUTTON_2)),
	MOVE_FORWARDS(keyboardHold(GLFW.GLFW_KEY_W)),
	MOVE_BACKWARDS(keyboardHold(GLFW.GLFW_KEY_S)),
	STRAFE_LEFT(keyboardHold(GLFW.GLFW_KEY_A)),
	STRAFE_RIGHT(keyboardHold(GLFW.GLFW_KEY_D)),
	JUMP(keyboardHold(GLFW.GLFW_KEY_SPACE)),
	FLY(keyboardHold(GLFW.GLFW_KEY_SPACE)),
	FREECAM(keyboardHold(GLFW.GLFW_KEY_J)),
	FALL(keyboardHold(GLFW.GLFW_KEY_LEFT_SHIFT)),
	CROUCH(keyboardHold(GLFW.GLFW_KEY_LEFT_SHIFT)),
	DEBUG_TOGGLE(keyboardToggle(GLFW.GLFW_KEY_F3, false)),
	END_TURN(keyboardHold(GLFW.GLFW_KEY_BACKSPACE)),
	START_GAME(keyboardHold(GLFW.GLFW_KEY_ENTER)),
	USE_ITEM(mouseHold(GLFW.GLFW_MOUSE_BUTTON_1)),
	MINIMAP(keyboardToggle(GLFW.GLFW_KEY_M, true)),
	TOGGLE_INVENTORY(keyboardHold(GLFW.GLFW_KEY_E)),
	SCREENSHOT(keyboardHold(GLFW.GLFW_KEY_F2)),
	SHOW_UI(keyboardToggle(GLFW.GLFW_KEY_F1, true));

	private final BiConsumer<GLFWGameControls<EvolutionControls>, EvolutionControls> defaultBinder;
	private EvolutionControls(BiConsumer<GLFWGameControls<EvolutionControls>, EvolutionControls> defaultBinder) {
		this.defaultBinder = defaultBinder;
	}

	@Override
	public BiConsumer<GLFWGameControls<EvolutionControls>, EvolutionControls> defaultBinder() {
		return defaultBinder;
	}
}
