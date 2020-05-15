package lemon.evolution;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import lemon.engine.control.GLFWWindow;
import lemon.engine.control.GLFWWindowSettings;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.input.KeyEvent;
import lemon.engine.thread.ThreadManager;

public class Evolution {
	private static final Logger logger = Logger.getLogger("");
	
	public static void main(String[] args) {
		LogManager.getLogManager().reset();
		logger.setLevel(Level.ALL);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new SimpleFormatter());
		logger.addHandler(consoleHandler);
		GLFWWindowSettings settings = new GLFWWindowSettings() {
			@Override
			public long createWindow() {
				GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
				/*GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
				return GLFW.glfwCreateWindow(vidmode.width(), vidmode.height(),
						"LWJGL version: " + Version.getVersion(), GLFW.glfwGetPrimaryMonitor(), MemoryUtil.NULL);*/
				return GLFW.glfwCreateWindow(1600, 900, "Evolution", MemoryUtil.NULL, MemoryUtil.NULL);
			}
			@Override
			public int getTargetFrameRate() {
				return 60;
			}
		};
		final GLFWWindow window = new GLFWWindow(settings);
		try {
			EventManager.INSTANCE.registerListener(new Listener() {
				@Subscribe
				public void onKeyPress(KeyEvent event) {
					if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
						GLFW.glfwSetWindowShouldClose(window.getId(), true);
					}
				}
			});
			window.init();
			EventManager.INSTANCE.registerListener(Menu.INSTANCE);
			window.run();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			ThreadManager.INSTANCE.interrupt();
			window.dump();
		}
	}
}
