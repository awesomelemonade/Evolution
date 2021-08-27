package lemon.evolution;

import lemon.engine.control.GLFWWindow;
import lemon.engine.thread.ThreadManager;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Evolution {
	private static final Logger logger = Logger.getLogger(Evolution.class.getName());

	public static void main(String[] args) {
		LogManager.getLogManager().reset();
		logger.setLevel(Level.ALL);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new SimpleFormatter());
		logger.addHandler(consoleHandler);
		try (GLFWWindow window = new GLFWWindow(() -> {
			// GLFW Window Settings
			GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
			GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
			if (vidmode == null) {
				throw new IllegalStateException();
			}
			return GLFW.glfwCreateWindow(vidmode.width(), vidmode.height(),
					"Evolution", GLFW.glfwGetPrimaryMonitor(), MemoryUtil.NULL);
			//return GLFW.glfwCreateWindow(1600, 900, "Evolution", MemoryUtil.NULL, MemoryUtil.NULL);
		}, Menu.INSTANCE)) {
			window.input().keyEvent().add(event -> {
				if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
					GLFW.glfwSetWindowShouldClose(window.getId(), true);
					ThreadManager.INSTANCE.interrupt();
				}
			});
			logger.log(Level.INFO, String.format("LWJGL Version %s", Version.getVersion()));
			logger.log(Level.INFO, String.format("OpenGL Version %s", GL11.glGetString(GL11.GL_VERSION)));
			window.run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
