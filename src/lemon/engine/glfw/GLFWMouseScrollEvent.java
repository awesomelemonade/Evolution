package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.MouseScrollEvent;

public record GLFWMouseScrollEvent(GLFWWindow glfwWindow, double xOffset, double yOffset) implements MouseScrollEvent, GLFWEvent {
}
