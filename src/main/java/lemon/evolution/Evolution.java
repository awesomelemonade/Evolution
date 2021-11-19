package lemon.evolution;

import lemon.engine.control.GLFWWindow;
import lemon.engine.thread.ThreadManager;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Evolution {
	private static final Logger logger = Logger.getLogger(Evolution.class.getName());
	private static final int TARGET_OPENGL_VERSION_MAJOR = 3;
	private static final int TARGET_OPENGL_VERSION_MINOR = 3;

	public static long createWindow(String[] args) {
		if (args.length > 0) {
			var width = Integer.parseInt(args[0]);
			var height = Integer.parseInt(args[1]);
			return GLFW.glfwCreateWindow(width, height, "Evolution", MemoryUtil.NULL, MemoryUtil.NULL);
		} else {
			var vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
			if (vidmode == null) {
				throw new IllegalStateException();
			}
			return GLFW.glfwCreateWindow(vidmode.width(), vidmode.height(),
					"Evolution", GLFW.glfwGetPrimaryMonitor(), MemoryUtil.NULL);
		}
	}

	public static void main(String[] args) {
		LogManager.getLogManager().reset();
		Logger rootLogger = Logger.getLogger("");
		logger.setLevel(Level.ALL);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		consoleHandler.setFormatter(new SimpleFormatter());
		rootLogger.addHandler(consoleHandler);
		logger.info("Program Arguments: " + Arrays.toString(args));
		try (GLFWWindow window = new GLFWWindow(() -> {
			// GLFW Window Settings
			GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
			// Needed for Mac Support
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, TARGET_OPENGL_VERSION_MAJOR);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, TARGET_OPENGL_VERSION_MINOR);
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
			return createWindow(args);
		}, Menu.INSTANCE)) {
			var disposable = window.input().keyEvent().add(event -> {
				if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
					GLFW.glfwSetWindowShouldClose(window.getId(), true);
					ThreadManager.INSTANCE.interrupt();
				}
			});
			System.getProperties().list(System.out);
			logger.log(Level.INFO, String.format("LWJGL Version: %s", Version.getVersion()));
			logger.log(Level.INFO, String.format("OpenGL Version: %s", GL11.glGetString(GL11.GL_VERSION)));
			logger.log(Level.INFO, String.format("GLFW Version: %s", GLFW.glfwGetVersionString()));
			logger.log(Level.INFO, String.format("Supported GLSL Version: %s", GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION)));
			window.run();
			disposable.dispose();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
