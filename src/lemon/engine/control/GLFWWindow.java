package lemon.engine.control;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import lemon.engine.event.EventManager;
import lemon.engine.evolution.Game;
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
		if(!GLFW.glfwInit()){
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
		GLFW.glfwSwapInterval(0);
		GLFW.glfwShowWindow(window);
		GL.createCapabilities(); //GLContext.createFromCurrent();
		EventManager.INSTANCE.callListeners(new LemonWindowInitEvent(window));
	}
	public void run(){
		EventManager.INSTANCE.preload(LemonUpdateEvent.class);
		EventManager.INSTANCE.preload(LemonRenderEvent.class);
		long deltaTime = System.nanoTime();
		while(!GLFW.glfwWindowShouldClose(window)){
			int error = GL11.glGetError();
			while(error!=GL11.GL_NO_ERROR){
				System.out.println(error);
				error = GL11.glGetError();
			}
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			//Event Driven
			long updateTime = System.nanoTime();
			long delta = updateTime-deltaTime;
			EventManager.INSTANCE.callListeners(new LemonUpdateEvent(delta));
			deltaTime = updateTime;
			updateTime = System.nanoTime()-updateTime;
			long renderTime = System.nanoTime();
			EventManager.INSTANCE.callListeners(new LemonRenderEvent());
			renderTime = System.nanoTime()-renderTime;
			Game.INSTANCE.updateData.add((float)(updateTime));
			Game.INSTANCE.renderData.add((float)(renderTime));
			Game.INSTANCE.fpsData.add(((float)delta)/1000000f);
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
			timeSync.sync(settings.getTargetFrameRate());
			//GLFW.glfwSetWindowTitle(window, settings.getTitle()+" - "+Integer.toString(timeSync.getFps()));
		}
	}
	public void dump(){
		EventManager.INSTANCE.callListeners(new LemonCleanUpEvent());
		GLFW.glfwDestroyWindow(window);
		Callbacks.glfwFreeCallbacks(window);
		GLFW.glfwTerminate();
		errorCallback.free();
	}
	public long getId(){
		return window;
	}
}
