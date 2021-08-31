package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.WindowMinimizeEvent;

public record GLFWWindowMinimizeEvent(GLFWWindow glfwWindow, boolean minimized) implements WindowMinimizeEvent, GLFWEvent {
}
