package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.CursorPositionEvent;

public record GLFWCursorPositionEvent(GLFWWindow glfwWindow, double x, double y) implements CursorPositionEvent, GLFWEvent {
}
