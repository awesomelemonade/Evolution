package lemon.evolution;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import lemon.engine.control.UpdateEvent;
import lemon.engine.math.Matrix;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

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
		CommonProgramsSetup.setup2D(Matrix.IDENTITY_4);
		buttons = new ArrayList<Quad2D>();
		for (int i = 0; i < 3; ++i) {
			buttons.add(new Quad2D(new Box2D(-0.3f, -0.3f - i * 0.2f, 0.6f, 0.1f), new Color(1f, 1f, 1f)));
		}
	}
	@Subscribe
	public void onMouseClick(GLFWMouseButtonEvent event) {
		if (event.getAction() == GLFW.GLFW_RELEASE) {
			event.getWindow().pollMouse((rawMouseX, rawMouseY) -> {
				float mouseX = (2f * rawMouseX / event.getWindow().getWidth()) - 1f;
				float mouseY = (2f * rawMouseY / event.getWindow().getHeight()) - 1f;
				for (int i = 0; i < buttons.size(); ++i) {
					if (buttons.get(i).getBox2D().intersect(mouseX, mouseY)) {
						switch (i) {
							case 0:
								start(Game.INSTANCE);
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
			});
		}
	}
	@Subscribe
	public void update(UpdateEvent event) {
		//start(Game.INSTANCE);
	}
	@Subscribe
	public void render(RenderEvent event) {
		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			for (Quad2D button : buttons) {
				button.render();
			}
		});
	}
	public void start(Listener listener) {
		EventManager.INSTANCE.registerListener(listener);
		EventManager.INSTANCE.unregisterListener(this);
	}
}
