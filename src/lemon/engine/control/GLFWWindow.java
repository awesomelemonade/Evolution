package lemon.engine.control;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import lemon.engine.event.EventManager;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.time.TimeSync;

public class GLFWWindow {
	private GLFWInput glfwInput;
	private GLFWErrorCallback errorCallback;
	private TimeSync timeSync;
	private long window;
	private GLFWWindowSettings settings;
	
	public GLFWWindow(GLFWWindowSettings settings){
		timeSync = new TimeSync();
		this.settings = settings;
	}
	
	public void init(){
		GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
		if(GLFW.glfwInit()!=GL11.GL_TRUE){
			throw new IllegalStateException("GLFW not initialized");
		}
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
		window = settings.createWindow();
		if(window==MemoryUtil.NULL){
			throw new IllegalStateException("GLFW window not created");
		}
		glfwInput = new GLFWInput(window);
		glfwInput.init();
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwShowWindow(window);
		GL.createCapabilities(); //GLContext.createFromCurrent();
		EventManager.INSTANCE.callListeners(new LemonInitEvent(window));
	}
	public void run(){
		EventManager.INSTANCE.preload(LemonUpdateEvent.class);
		EventManager.INSTANCE.preload(LemonRenderEvent.class);
		while(GLFW.glfwWindowShouldClose(window)==GL11.GL_FALSE){
			int error = GL11.glGetError();
			while(error!=GL11.GL_NO_ERROR){
				System.out.println(error);
				error = GL11.glGetError();
			}
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			//Event Driven
			EventManager.INSTANCE.callListeners(new LemonUpdateEvent());
			EventManager.INSTANCE.callListeners(new LemonRenderEvent());
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
			timeSync.sync(settings.getTargetFrameRate());
			//GLFW.glfwSetWindowTitle(window, settings.getTitle()+" - "+Integer.toString(timeSync.getFps()));
		}
	}
	public void dump(){
		EventManager.INSTANCE.callListeners(new LemonCleanUpEvent());
		GLFW.glfwDestroyWindow(window);
		Callbacks.glfwReleaseCallbacks(window);
		GLFW.glfwTerminate();
		errorCallback.release();
	}
	public long getId(){
		return window;
	}
}
