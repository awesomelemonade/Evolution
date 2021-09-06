package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.WindowSizeEvent;

public record GLFWWindowSizeEvent(GLFWWindow glfwWindow, int width, int height) implements WindowSizeEvent, GLFWEvent {
}
