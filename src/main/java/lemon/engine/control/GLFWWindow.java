package lemon.engine.control;

import lemon.engine.event.EventWith;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.time.Benchmark;
import lemon.engine.time.TimeSync;
import lemon.engine.toolbox.Disposable;
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
import java.util.function.BiConsumer;

public class GLFWWindow implements Disposable {
	private final GLFWInput glfwInput;
	private final GLFWErrorCallback errorCallback;
	private final TimeSync timeSync;
	private final long window;
	private final GLFWWindowSettings settings;
	private final int width;
	private final int height;
	private final EventWith<Benchmark> onBenchmark;
	private Screen currentScreen;

	public GLFWWindow(GLFWWindowSettings settings, Screen initialScreen) {
		this.timeSync = new TimeSync();
		this.settings = settings;
		onBenchmark = new EventWith<>();
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
		currentScreen = initialScreen;
		currentScreen.onLoad(this);
	}

	public void run() {
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
			currentScreen.update(delta);
			deltaTime = updateTime;
			updateTime = System.nanoTime() - updateTime;
			long renderTime = System.nanoTime();
			currentScreen.render();
			renderTime = System.nanoTime() - renderTime;
			onBenchmark.callListeners(Benchmark.of(this, (float) (updateTime), (float) (renderTime), ((float) delta) / 1000000f));
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
			timeSync.sync(settings.getTargetFrameRate());
		}
	}

	public void switchScreen(Screen screen) {
		currentScreen.dispose();
		currentScreen = screen;
		currentScreen.onLoad(this);
	}

	@Override
	public void dispose() {
		currentScreen.dispose();
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

	public EventWith<Benchmark> onBenchmark() {
		return onBenchmark;
	}
}
