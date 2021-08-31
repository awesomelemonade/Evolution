package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.WindowCloseEvent;

public record GLFWWindowCloseEvent(GLFWWindow glfwWindow) implements WindowCloseEvent, GLFWEvent {
}
