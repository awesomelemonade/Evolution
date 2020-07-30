package lemon.engine.control;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import lemon.engine.event.EventManager;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.time.Benchmark;
import lemon.engine.time.LemonBenchmarkEvent;
import lemon.engine.time.TimeSync;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.function.BiConsumer;

public class GLFWWindow {
	private GLFWInput glfwInput;
	private GLFWErrorCallback errorCallback;
	private TimeSync timeSync;
	private long window;
	private GLFWWindowSettings settings;
	private DoubleBuffer mouseXBuffer;
	private DoubleBuffer mouseYBuffer;
	private int width;
	private int height;

	public GLFWWindow(GLFWWindowSettings settings) {
		this.timeSync = new TimeSync();
		this.settings = settings;
		this.mouseXBuffer = BufferUtils.createDoubleBuffer(1);
		this.mouseYBuffer = BufferUtils.createDoubleBuffer(1);
	}

	public void init() {
		GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("GLFW not initialized");
		}
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
		window = settings.createWindow();
		if (window == MemoryUtil.NULL) {
			throw new IllegalStateException("GLFW window not created");
		}
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		this.width = width.get();
		this.height = height.get();
		glfwInput = new GLFWInput(this);
		glfwInput.init();
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(0); // Disables v-sync
		GLFW.glfwShowWindow(window);
		GL.createCapabilities(); // GLContext.createFromCurrent();
		EventManager.INSTANCE.callListeners(new LemonWindowInitEvent(this));
	}
	public void run() {
		EventManager.INSTANCE.preload(LemonUpdateEvent.class);
		EventManager.INSTANCE.preload(LemonRenderEvent.class);
		long deltaTime = System.nanoTime();
		while (!GLFW.glfwWindowShouldClose(window)) {
			int error = GL11.glGetError();
			while (error != GL11.GL_NO_ERROR) {
				System.out.println(error);
				error = GL11.glGetError();
			}
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			// Event Driven
			long updateTime = System.nanoTime();
			long delta = updateTime - deltaTime;
			EventManager.INSTANCE.callListeners(new LemonUpdateEvent(delta));
			deltaTime = updateTime;
			updateTime = System.nanoTime() - updateTime;
			long renderTime = System.nanoTime();
			EventManager.INSTANCE.callListeners(new LemonRenderEvent());
			renderTime = System.nanoTime() - renderTime;
			EventManager.INSTANCE.callListeners(new LemonBenchmarkEvent(
					new Benchmark(this, (float) (updateTime), (float) (renderTime), ((float) delta) / 1000000f)));
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
			timeSync.sync(settings.getTargetFrameRate());
			// GLFW.glfwSetWindowTitle(window, settings.getTitle()+" -
			// "+Integer.toString(timeSync.getFps()));
		}
	}
	public void dump() {
		EventManager.INSTANCE.callListeners(new LemonCleanUpEvent());
		GLFW.glfwDestroyWindow(window);
		Callbacks.glfwFreeCallbacks(window);
		GLFW.glfwTerminate();
		errorCallback.free();
	}
	public long getId() {
		return window;
	}
	public void pollMouse() {
		mouseXBuffer.clear();
		mouseYBuffer.clear();
		GLFW.glfwGetCursorPos(window, mouseXBuffer, mouseYBuffer);
	}
	public void pollMouse(BiConsumer<Float, Float> consumer) {
		pollMouse();
		consumer.accept((float) this.getMouseX(), (float) (this.getHeight() - this.getMouseY()));
	}
	public double getMouseX() {
		return mouseXBuffer.get(0);
	}
	public double getMouseY() {
		return mouseYBuffer.get(0);
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
}
