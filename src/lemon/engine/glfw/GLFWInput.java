package lemon.engine.glfw;

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
import org.lwjgl.opengl.GL11;

import lemon.engine.event.EventManager;

public class GLFWInput {
	private long window;
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
	
	public GLFWInput(long window){
		this.window = window;
	}
	public void init(){
		GLFW.glfwSetCharModsCallback(window, charModsCallback = new GLFWCharModsCallback(){
			@Override
			public void invoke(long window, int codepoint, int mods){
				EventManager.INSTANCE.callListeners(new GLFWCharacterEvent(window, codepoint, mods));
			}
		});
		GLFW.glfwSetCursorEnterCallback(window, cursorEnterCallback = new GLFWCursorEnterCallback(){
			@Override
			public void invoke(long window, int entered){
				EventManager.INSTANCE.callListeners(new GLFWCursorEnterEvent(window, entered==GL11.GL_TRUE));
			}
		});
		GLFW.glfwSetCursorPosCallback(window, cursorPosCallback = new GLFWCursorPosCallback(){
			@Override
			public void invoke(long window, double xPos, double yPos){
				EventManager.INSTANCE.callListeners(new GLFWCursorPositionEvent(window, xPos, yPos));
			}
		});
		GLFW.glfwSetDropCallback(window, dropCallback = new GLFWDropCallback(){
			@Override
			public void invoke(long window, int count, long names){
				EventManager.INSTANCE.callListeners(new GLFWFileDropEvent(window, count, names));
			}
		});
		GLFW.glfwSetFramebufferSizeCallback(window, framebufferSizeCallback = new GLFWFramebufferSizeCallback(){
			@Override
			public void invoke(long window, int width, int height){
				EventManager.INSTANCE.callListeners(new GLFWFrameBufferSizeEvent(window, width, height));
			}
		});
		GLFW.glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback(){
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods){
				EventManager.INSTANCE.callListeners(new GLFWKeyEvent(window, key, scancode, action, mods));
			}
		});
		GLFW.glfwSetMouseButtonCallback(window, mouseButtonCallback = new GLFWMouseButtonCallback(){
			@Override
			public void invoke(long window, int button, int action, int mods){
				EventManager.INSTANCE.callListeners(new GLFWMouseButtonEvent(window, button, action, mods));
			}
		});
		GLFW.glfwSetScrollCallback(window, scrollCallback = new GLFWScrollCallback(){
			@Override
			public void invoke(long window, double xOffSet, double yOffSet){
				EventManager.INSTANCE.callListeners(new GLFWMouseScrollEvent(window, xOffSet, yOffSet));
			}
		});
		GLFW.glfwSetWindowCloseCallback(window, windowCloseCallback = new GLFWWindowCloseCallback(){
			@Override
			public void invoke(long window){
				EventManager.INSTANCE.callListeners(new GLFWWindowCloseEvent(window));
			}
		});
		GLFW.glfwSetWindowFocusCallback(window, windowFocusCallback = new GLFWWindowFocusCallback(){
			@Override
			public void invoke(long window, int focused){
				EventManager.INSTANCE.callListeners(new GLFWWindowFocusEvent(window, focused==GL11.GL_TRUE));
			}
		});
		GLFW.glfwSetWindowIconifyCallback(window, windowIconifyCallback = new GLFWWindowIconifyCallback(){
			@Override
			public void invoke(long window, int iconified){
				EventManager.INSTANCE.callListeners(new GLFWWindowMinimizeEvent(window, iconified==GL11.GL_TRUE));
			}
		});
		GLFW.glfwSetWindowPosCallback(window, windowPosCallback = new GLFWWindowPosCallback(){
			@Override
			public void invoke(long window, int xPos, int yPos){
				EventManager.INSTANCE.callListeners(new GLFWWindowMoveEvent(window, xPos, yPos));
			}
		});
		GLFW.glfwSetWindowRefreshCallback(window, windowRefreshCallback = new GLFWWindowRefreshCallback(){
			@Override
			public void invoke(long window){
				EventManager.INSTANCE.callListeners(new GLFWWindowRefreshEvent(window));
			}
		});
		GLFW.glfwSetWindowSizeCallback(window, windowSizeCallback = new GLFWWindowSizeCallback(){
			@Override
			public void invoke(long window, int width, int height){
				EventManager.INSTANCE.callListeners(new GLFWWindowSizeEvent(window, width, height));
			}
		});
	}
	public long getWindow(){
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