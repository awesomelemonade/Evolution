package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.WindowFocusEvent;

public record GLFWWindowFocusEvent(GLFWWindow glfwWindow, boolean focused) implements WindowFocusEvent, GLFWEvent {
}
