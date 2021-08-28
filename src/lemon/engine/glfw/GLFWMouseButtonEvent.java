package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.MouseButtonEvent;

public record GLFWMouseButtonEvent(GLFWWindow glfwWindow, int button, int action, int mods) implements MouseButtonEvent, GLFWEvent, GLFWKeyMods {
}
