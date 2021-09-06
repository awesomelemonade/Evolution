package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.CursorEnterEvent;

public record GLFWCursorEnterEvent(GLFWWindow glfwWindow, boolean entered) implements CursorEnterEvent, GLFWEvent {
}
