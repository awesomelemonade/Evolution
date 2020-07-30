package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.event.Event;

public interface GLFWEvent extends Event {
	public GLFWWindow getWindow();
}
