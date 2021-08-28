package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.FrameBufferSizeEvent;

public record GLFWFrameBufferSizeEvent(GLFWWindow glfwWindow, int width, int height) implements FrameBufferSizeEvent, GLFWEvent {
}
