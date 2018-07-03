package lemon.evolution;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.RenderEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.game2d.Quad2D;
import lemon.engine.glfw.GLFWMouseButtonEvent;
import lemon.engine.math.Box2D;
import lemon.engine.toolbox.Color;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;
import lemon.game2d.Game2D;

public enum Menu implements Listener {
	INSTANCE;
	private List<Quad2D> buttons;

	@Override
	public void onRegister() {
		CommonProgramsSetup.setup2D();
		buttons = new ArrayList<Quad2D>();
		for (int i = 0; i < 3; ++i) {
			buttons.add(new Quad2D(new Box2D(-0.3f, -0.3f - i * 0.2f, 0.6f, 0.1f), new Color(1f, 1f, 1f)));
		}
	}
	@Subscribe
	public void onMouseClick(GLFWMouseButtonEvent event) {
		if (event.getAction() == GLFW.GLFW_RELEASE) {
			DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
			DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
			GLFW.glfwGetCursorPos(event.getWindow(), xBuffer, yBuffer);
			float mouseX = (float) xBuffer.get();
			float mouseY = (float) yBuffer.get();
			IntBuffer width = BufferUtils.createIntBuffer(1);
			IntBuffer height = BufferUtils.createIntBuffer(1);
			GLFW.glfwGetWindowSize(event.getWindow(), width, height);
			int window_width = width.get();
			int window_height = height.get();
			mouseX = (2f * mouseX / window_width) - 1f;
			mouseY = -1f * ((2f * mouseY / window_height) - 1f);
			for (int i = 0; i < buttons.size(); ++i) {
				if (buttons.get(i).getBox2D().intersect(mouseX, mouseY)) {
					switch (i) {
						case 0:
							start(Loading.INSTANCE);
							break;
						case 1:
							start(Game2D.INSTANCE);
							break;
						case 2:
							start(FontTest.INSTANCE);
							break;
					}
				}
			}
		}
	}
	@Subscribe
	public void render(RenderEvent event) {
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		for (Quad2D button : buttons) {
			button.render();
		}
		GL20.glUseProgram(0);
	}
	public void start(Listener listener) {
		EventManager.INSTANCE.registerListener(listener);
		EventManager.INSTANCE.unregisterListener(this);
	}
}