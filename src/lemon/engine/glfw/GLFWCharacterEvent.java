package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.CharacterEvent;

public record GLFWCharacterEvent(GLFWWindow glfwWindow, int codepoint,
								 int mods) implements CharacterEvent, GLFWEvent, GLFWKeyMods {
}
