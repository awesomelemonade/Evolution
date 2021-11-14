package lemon.engine.control;

import lemon.engine.event.EventWith;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.time.Benchmark;
import lemon.engine.time.TimeSync;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.TaskQueue;
import lemon.evolution.screen.beta.Screen;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GLFWWindow implements Disposable {
	private static final Logger logger = Logger.getLogger(GLFWWindow.class.getName());
	private final GLFWInput glfwInput;
	private final GLFWErrorCallback errorCallback;
	private final TimeSync timeSync = new TimeSync();
	private final long window;
	private final GLFWWindowSettings settings;
	private final int width;
	private final int height;
	private final EventWith<Benchmark> onBenchmark = new EventWith<>();
	private final Deque<Screen> screenStack = new ArrayDeque<>();
	private final TaskQueue screenSwitchQueue = TaskQueue.ofSingleThreaded();

	public GLFWWindow(GLFWWindowSettings settings, Screen initialScreen) {
		this.settings = settings;
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
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer width = stack.mallocInt(1);
			IntBuffer height = stack.mallocInt(1);
			GLFW.glfwGetWindowSize(window, width, height);
			this.width = width.get();
			this.height = height.get();
		}
		glfwInput = new GLFWInput(this);
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(0); // Disables v-sync
		GLFW.glfwShowWindow(window);
		GL.createCapabilities(); // GLContext.createFromCurrent();
		pushScreen(initialScreen);
		screenSwitchQueue.run();
	}

	public void run() {
		long deltaTime = System.nanoTime();
		while (!GLFW.glfwWindowShouldClose(window)) {
			int error = GL11.glGetError();
			while (error != GL11.GL_NO_ERROR) {
				logger.log(Level.WARNING, "OpenGL Error " + error);
				error = GL11.glGetError();
			}
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			var currentScreen = screenStack.peek();
			if (currentScreen == null) {
				logger.log(Level.WARNING, "No screens in stack - exiting");
				break;
			}
			long updateTime = System.nanoTime();
			long delta = updateTime - deltaTime;
			currentScreen.update(delta);
			deltaTime = updateTime;
			updateTime = System.nanoTime() - updateTime;
			long renderTime = System.nanoTime();
			currentScreen.render();
			renderTime = System.nanoTime() - renderTime;
			screenSwitchQueue.run();
			GLFW.glfwSwapBuffers(window);
			long pollEventsTime = System.nanoTime();
			GLFW.glfwPollEvents();
			pollEventsTime = System.nanoTime() - pollEventsTime;
			onBenchmark.callListeners(Benchmark.of(
					this, (float) updateTime, (float) renderTime,
					(float) pollEventsTime, ((float) delta) / 1000000f));
			timeSync.sync(settings.getTargetFrameRate());
		}
	}

	public void pushScreen(Screen screen) {
		screenSwitchQueue.add(() -> {
			screenStack.push(screen);
			screen.onLoad(this);
		});
	}

	public void popScreen() {
		screenSwitchQueue.add(() -> {
			screenStack.pop().dispose();
			if (screenStack.isEmpty()) {
				throw new IllegalStateException("Must have at least 1 screen in the stack");
			}
			screenStack.peek().onLoad(this);
		});
	}

	public void popAndPushScreen(Screen screen) {
		screenSwitchQueue.add(() -> {
			screenStack.pop().dispose();
			screenStack.push(screen);
			screen.onLoad(this);
		});
	}

	@Override
	public void dispose() {
		while (!screenStack.isEmpty()) {
			var screen = screenStack.pop();
			logger.log(Level.INFO, "Disposing " + screen.getClass().getName());
			screen.dispose();
		}
		GLFW.glfwDestroyWindow(window);
		Callbacks.glfwFreeCallbacks(window);
		GLFW.glfwTerminate();
		errorCallback.free();
	}

	public long getId() {
		return window;
	}

	public void pollMouse(BiConsumer<Float, Float> consumer) {
		float mouseX;
		float mouseY;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			DoubleBuffer xBuffer = stack.mallocDouble(1);
			DoubleBuffer yBuffer = stack.mallocDouble(1);
			GLFW.glfwGetCursorPos(window, xBuffer, yBuffer);
			mouseX = (float) xBuffer.get();
			mouseY = (float) (this.getHeight() - yBuffer.get());
		}
		consumer.accept(mouseX, mouseY);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public GLFWInput input() {
		return glfwInput;
	}

	public TimeSync timeSync() {
		return timeSync;
	}

	public EventWith<Benchmark> onBenchmark() {
		return onBenchmark;
	}
}
