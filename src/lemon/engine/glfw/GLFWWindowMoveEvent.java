package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.WindowMoveEvent;

public record GLFWWindowMoveEvent(GLFWWindow glfwWindow, int x, int y) implements WindowMoveEvent, GLFWEvent {
}
