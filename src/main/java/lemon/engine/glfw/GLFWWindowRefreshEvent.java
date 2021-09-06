package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.WindowRefreshEvent;

public record GLFWWindowRefreshEvent(GLFWWindow glfwWindow) implements WindowRefreshEvent, GLFWEvent {
}
