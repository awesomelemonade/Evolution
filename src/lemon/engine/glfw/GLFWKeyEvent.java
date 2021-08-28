package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.KeyEvent;

public record GLFWKeyEvent(GLFWWindow glfwWindow, int key, int scancode, int action, int mods) implements KeyEvent, GLFWEvent, GLFWKeyMods {
}
