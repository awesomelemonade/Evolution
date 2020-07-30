package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
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

import lemon.engine.event.EventManager;

public class GLFWInput {
	private GLFWWindow window;
	private GLFWCharModsCallback charModsCallback;
	private GLFWCursorEnterCallback cursorEnterCallback;
	private GLFWCursorPosCallback cursorPosCallback;
	private GLFWDropCallback dropCallback;
	private GLFWFramebufferSizeCallback framebufferSizeCallback;
	private GLFWKeyCallback keyCallback;
	private GLFWMouseButtonCallback mouseButtonCallback;
	private GLFWScrollCallback scrollCallback;
	private GLFWWindowCloseCallback windowCloseCallback;
	private GLFWWindowFocusCallback windowFocusCallback;
	private GLFWWindowIconifyCallback windowIconifyCallback;
	private GLFWWindowPosCallback windowPosCallback;
	private GLFWWindowRefreshCallback windowRefreshCallback;
	private GLFWWindowSizeCallback windowSizeCallback;

	public GLFWInput(GLFWWindow window) {
		this.window = window;
	}
	public void init() {
		GLFW.glfwSetCharModsCallback(window.getId(), charModsCallback = new GLFWCharModsCallback() {
			@Override
			public void invoke(long window, int codepoint, int mods) {
				EventManager.INSTANCE.callListeners(new GLFWCharacterEvent(GLFWInput.this.window, codepoint, mods));
			}
		});
		GLFW.glfwSetCursorEnterCallback(window.getId(), cursorEnterCallback = new GLFWCursorEnterCallback() {
			@Override
			public void invoke(long window, boolean entered) {
				EventManager.INSTANCE.callListeners(new GLFWCursorEnterEvent(GLFWInput.this.window, entered));
			}
		});
		GLFW.glfwSetCursorPosCallback(window.getId(), cursorPosCallback = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xPos, double yPos) {
				EventManager.INSTANCE.callListeners(new GLFWCursorPositionEvent(GLFWInput.this.window, xPos, yPos));
			}
		});
		GLFW.glfwSetDropCallback(window.getId(), dropCallback = new GLFWDropCallback() {
			@Override
			public void invoke(long window, int count, long names) {
				EventManager.INSTANCE.callListeners(new GLFWFileDropEvent(GLFWInput.this.window, count, names));
			}
		});
		GLFW.glfwSetFramebufferSizeCallback(window.getId(), framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				EventManager.INSTANCE.callListeners(new GLFWFrameBufferSizeEvent(GLFWInput.this.window, width, height));
			}
		});
		GLFW.glfwSetKeyCallback(window.getId(), keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				EventManager.INSTANCE.callListeners(new GLFWKeyEvent(GLFWInput.this.window, key, scancode, action, mods));
			}
		});
		GLFW.glfwSetMouseButtonCallback(window.getId(), mouseButtonCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				EventManager.INSTANCE.callListeners(new GLFWMouseButtonEvent(GLFWInput.this.window, button, action, mods));
			}
		});
		GLFW.glfwSetScrollCallback(window.getId(), scrollCallback = new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double xOffSet, double yOffSet) {
				EventManager.INSTANCE.callListeners(new GLFWMouseScrollEvent(GLFWInput.this.window, xOffSet, yOffSet));
			}
		});
		GLFW.glfwSetWindowCloseCallback(window.getId(), windowCloseCallback = new GLFWWindowCloseCallback() {
			@Override
			public void invoke(long window) {
				EventManager.INSTANCE.callListeners(new GLFWWindowCloseEvent(GLFWInput.this.window));
			}
		});
		GLFW.glfwSetWindowFocusCallback(window.getId(), windowFocusCallback = new GLFWWindowFocusCallback() {
			@Override
			public void invoke(long window, boolean focused) {
				EventManager.INSTANCE.callListeners(new GLFWWindowFocusEvent(GLFWInput.this.window, focused));
			}
		});
		GLFW.glfwSetWindowIconifyCallback(window.getId(), windowIconifyCallback = new GLFWWindowIconifyCallback() {
			@Override
			public void invoke(long window, boolean iconified) {
				EventManager.INSTANCE.callListeners(new GLFWWindowMinimizeEvent(GLFWInput.this.window, iconified));
			}
		});
		GLFW.glfwSetWindowPosCallback(window.getId(), windowPosCallback = new GLFWWindowPosCallback() {
			@Override
			public void invoke(long window, int xPos, int yPos) {
				EventManager.INSTANCE.callListeners(new GLFWWindowMoveEvent(GLFWInput.this.window, xPos, yPos));
			}
		});
		GLFW.glfwSetWindowRefreshCallback(window.getId(), windowRefreshCallback = new GLFWWindowRefreshCallback() {
			@Override
			public void invoke(long window) {
				EventManager.INSTANCE.callListeners(new GLFWWindowRefreshEvent(GLFWInput.this.window));
			}
		});
		GLFW.glfwSetWindowSizeCallback(window.getId(), windowSizeCallback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				EventManager.INSTANCE.callListeners(new GLFWWindowSizeEvent(GLFWInput.this.window, width, height));
			}
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