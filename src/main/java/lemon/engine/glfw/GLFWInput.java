package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.event.EventWith;
import lemon.engine.toolbox.Disposables;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharModsCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

public class GLFWInput {
	private final GLFWWindow window;
	private final GLFWCharModsCallback charModsCallback;
	private final GLFWCursorEnterCallback cursorEnterCallback;
	private final GLFWCursorPosCallback cursorPosCallback;
	private final GLFWDropCallback dropCallback;
	private final GLFWFramebufferSizeCallback framebufferSizeCallback;
	private final GLFWKeyCallback keyCallback;
	private final GLFWMouseButtonCallback mouseButtonCallback;
	private final GLFWScrollCallback scrollCallback;
	private final GLFWWindowCloseCallback windowCloseCallback;
	private final GLFWWindowFocusCallback windowFocusCallback;
	private final GLFWWindowIconifyCallback windowIconifyCallback;
	private final GLFWWindowPosCallback windowPosCallback;
	private final GLFWWindowRefreshCallback windowRefreshCallback;
	private final GLFWWindowSizeCallback windowSizeCallback;
	private final EventWith<GLFWCharacterEvent> characterEvent = new EventWith<>();
	private final EventWith<GLFWCursorEnterEvent> cursorEnterEvent = new EventWith<>();
	private final EventWith<GLFWCursorPositionEvent> cursorPositionEvent = new EventWith<>();
	private final EventWith<GLFWFileDropEvent> fileDropEvent = new EventWith<>();
	private final EventWith<GLFWFrameBufferSizeEvent> frameBufferSizeEvent = new EventWith<>();
	private final EventWith<GLFWKeyEvent> keyEvent = new EventWith<>();
	private final EventWith<GLFWMouseButtonEvent> mouseButtonEvent = new EventWith<>();
	private final EventWith<GLFWMouseScrollEvent> mouseScrollEvent = new EventWith<>();
	private final EventWith<GLFWWindowCloseEvent> windowCloseEvent = new EventWith<>();
	private final EventWith<GLFWWindowFocusEvent> windowFocusEvent = new EventWith<>();
	private final EventWith<GLFWWindowMinimizeEvent> windowMinimizeEvent = new EventWith<>();
	private final EventWith<GLFWWindowMoveEvent> windowMoveEvent = new EventWith<>();
	private final EventWith<GLFWWindowRefreshEvent> windowRefreshEvent = new EventWith<>();
	private final EventWith<GLFWWindowSizeEvent> windowSizeEvent = new EventWith<>();
	private final EventWith<GLFWCursorDeltaEvent> cursorDeltaEvent = new EventWith<>();

	public EventWith<GLFWCharacterEvent> characterEvent() {
		return characterEvent;
	}

	public EventWith<GLFWCursorEnterEvent> cursorEnterEvent() {
		return cursorEnterEvent;
	}

	public EventWith<GLFWCursorPositionEvent> cursorPositionEvent() {
		return cursorPositionEvent;
	}

	public EventWith<GLFWFileDropEvent> fileDropEvent() {
		return fileDropEvent;
	}

	public EventWith<GLFWFrameBufferSizeEvent> frameBufferSizeEvent() {
		return frameBufferSizeEvent;
	}

	public EventWith<GLFWKeyEvent> keyEvent() {
		return keyEvent;
	}

	public EventWith<GLFWMouseButtonEvent> mouseButtonEvent() {
		return mouseButtonEvent;
	}

	public EventWith<GLFWMouseScrollEvent> mouseScrollEvent() {
		return mouseScrollEvent;
	}

	public EventWith<GLFWWindowCloseEvent> windowCloseEvent() {
		return windowCloseEvent;
	}

	public EventWith<GLFWWindowFocusEvent> windowFocusEvent() {
		return windowFocusEvent;
	}

	public EventWith<GLFWWindowMinimizeEvent> windowMinimizeEvent() {
		return windowMinimizeEvent;
	}

	public EventWith<GLFWWindowMoveEvent> windowMoveEvent() {
		return windowMoveEvent;
	}

	public EventWith<GLFWWindowRefreshEvent> windowRefreshEvent() {
		return windowRefreshEvent;
	}

	public EventWith<GLFWWindowSizeEvent> windowSizeEvent() {
		return windowSizeEvent;
	}

	public EventWith<GLFWCursorDeltaEvent> cursorDeltaEvent() {
		return cursorDeltaEvent;
	}

	public GLFWInput(GLFWWindow window) {
		this.window = window;
		GLFW.glfwSetCharModsCallback(window.getId(), charModsCallback = new GLFWCharModsCallback() {
			@Override
			public void invoke(long window, int codepoint, int mods) {
				characterEvent.callListeners(new GLFWCharacterEvent(GLFWInput.this.window, codepoint, mods));
			}
		});
		GLFW.glfwSetCursorEnterCallback(window.getId(), cursorEnterCallback = new GLFWCursorEnterCallback() {
			@Override
			public void invoke(long window, boolean entered) {
				cursorEnterEvent.callListeners(new GLFWCursorEnterEvent(GLFWInput.this.window, entered));
			}
		});
		GLFW.glfwSetCursorPosCallback(window.getId(), cursorPosCallback = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xPos, double yPos) {
				cursorPositionEvent.callListeners(new GLFWCursorPositionEvent(GLFWInput.this.window, xPos, yPos));
			}
		});
		GLFW.glfwSetDropCallback(window.getId(), dropCallback = new GLFWDropCallback() {
			@Override
			public void invoke(long window, int count, long names) {
				fileDropEvent.callListeners(new GLFWFileDropEvent(GLFWInput.this.window, count, names));
			}
		});
		GLFW.glfwSetFramebufferSizeCallback(window.getId(), framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				frameBufferSizeEvent.callListeners(new GLFWFrameBufferSizeEvent(GLFWInput.this.window, width, height));
			}
		});
		GLFW.glfwSetKeyCallback(window.getId(), keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				keyEvent.callListeners(new GLFWKeyEvent(GLFWInput.this.window, key, scancode, action, mods));
			}
		});
		GLFW.glfwSetMouseButtonCallback(window.getId(), mouseButtonCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				mouseButtonEvent.callListeners(new GLFWMouseButtonEvent(GLFWInput.this.window, button, action, mods));
			}
		});
		GLFW.glfwSetScrollCallback(window.getId(), scrollCallback = new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double xOffSet, double yOffSet) {
				mouseScrollEvent.callListeners(new GLFWMouseScrollEvent(GLFWInput.this.window, xOffSet, yOffSet));
			}
		});
		GLFW.glfwSetWindowCloseCallback(window.getId(), windowCloseCallback = new GLFWWindowCloseCallback() {
			@Override
			public void invoke(long window) {
				windowCloseEvent.callListeners(new GLFWWindowCloseEvent(GLFWInput.this.window));
			}
		});
		GLFW.glfwSetWindowFocusCallback(window.getId(), windowFocusCallback = new GLFWWindowFocusCallback() {
			@Override
			public void invoke(long window, boolean focused) {
				windowFocusEvent.callListeners(new GLFWWindowFocusEvent(GLFWInput.this.window, focused));
			}
		});
		GLFW.glfwSetWindowIconifyCallback(window.getId(), windowIconifyCallback = new GLFWWindowIconifyCallback() {
			@Override
			public void invoke(long window, boolean iconified) {
				windowMinimizeEvent.callListeners(new GLFWWindowMinimizeEvent(GLFWInput.this.window, iconified));
			}
		});
		GLFW.glfwSetWindowPosCallback(window.getId(), windowPosCallback = new GLFWWindowPosCallback() {
			@Override
			public void invoke(long window, int xPos, int yPos) {
				windowMoveEvent.callListeners(new GLFWWindowMoveEvent(GLFWInput.this.window, xPos, yPos));
			}
		});
		GLFW.glfwSetWindowRefreshCallback(window.getId(), windowRefreshCallback = new GLFWWindowRefreshCallback() {
			@Override
			public void invoke(long window) {
				windowRefreshEvent.callListeners(new GLFWWindowRefreshEvent(GLFWInput.this.window));
			}
		});
		GLFW.glfwSetWindowSizeCallback(window.getId(), windowSizeCallback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				windowSizeEvent.callListeners(new GLFWWindowSizeEvent(GLFWInput.this.window, width, height));
			}
		});
		var lastMousePositions = new Object() {
			double x = 0.0;
			double y = 0.0;
		};
		cursorPositionEvent.add(event -> {
			var mouseX = event.x();
			var mouseY = event.y();
			cursorDeltaEvent.callListeners(new GLFWCursorDeltaEvent(event.glfwWindow(), mouseX - lastMousePositions.x, mouseY - lastMousePositions.y));
			lastMousePositions.x = mouseX;
			lastMousePositions.y = mouseY;
		});
	}

	public GLFWWindow getWindow() {
		return window;
	}

	public GLFWCharModsCallback getCharModsCallback() {
		return charModsCallback;
	}

	public GLFWCursorEnterCallback getCursorEnterCallback() {
		return cursorEnterCallback;
	}

	public GLFWCursorPosCallback getCursorPosCallback() {
		return cursorPosCallback;
	}

	public GLFWDropCallback getDropCallback() {
		return dropCallback;
	}

	public GLFWFramebufferSizeCallback getFramebufferSizeCallback() {
		return framebufferSizeCallback;
	}

	public GLFWKeyCallback getKeyCallback() {
		return keyCallback;
	}

	public GLFWMouseButtonCallback getMouseButtonCallback() {
		return mouseButtonCallback;
	}

	public GLFWScrollCallback getScrollCallback() {
		return scrollCallback;
	}

	public GLFWWindowCloseCallback getWindowCloseCallback() {
		return windowCloseCallback;
	}

	public GLFWWindowFocusCallback getWindowFocusCallback() {
		return windowFocusCallback;
	}

	public GLFWWindowIconifyCallback getWindowIconifyCallback() {
		return windowIconifyCallback;
	}

	public GLFWWindowPosCallback getWindowPosCallback() {
		return windowPosCallback;
	}

	public GLFWWindowRefreshCallback getWindowRefreshCallback() {
		return windowRefreshCallback;
	}

	public GLFWWindowSizeCallback getWindowSizeCallback() {
		return windowSizeCallback;
	}
}