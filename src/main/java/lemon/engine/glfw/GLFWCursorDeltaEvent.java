package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.CursorPositionEvent;

public record GLFWCursorDeltaEvent(GLFWWindow glfwWindow, double x, double y) implements CursorPositionEvent, GLFWEvent {
}
