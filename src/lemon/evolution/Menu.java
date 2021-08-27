package lemon.evolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lemon.engine.control.GLFWWindow;
import lemon.engine.math.Matrix;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.screen.beta.Screen;
import org.lwjgl.glfw.GLFW;

import lemon.engine.game2d.Quad2D;
import lemon.engine.math.Box2D;
import lemon.engine.toolbox.Color;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;

public enum Menu implements Screen {
	INSTANCE;
	private List<Quad2D> buttons;
	private final Disposables disposables = new Disposables();

	@Override
	public void onLoad(GLFWWindow window) {
		CommonProgramsSetup.setup2D(Matrix.IDENTITY_4);
		buttons = new ArrayList<>();
		for (int i = 0; i < 3; ++i) {
			buttons.add(new Quad2D(new Box2D(-0.3f, -0.3f - i * 0.2f, 0.6f, 0.1f), new Color(1f, 1f, 1f)));
		}
		disposables.add(window.input().mouseButtonEvent().add(event -> {
			if (event.getAction() == GLFW.GLFW_RELEASE) {
				event.getWindow().pollMouse((rawMouseX, rawMouseY) -> {
					float mouseX = (2f * rawMouseX / event.getWindow().getWidth()) - 1f;
					float mouseY = (2f * rawMouseY / event.getWindow().getHeight()) - 1f;
					for (int i = 0; i < buttons.size(); ++i) {
						if (buttons.get(i).getBox2D().intersect(mouseX, mouseY)) {
							switch (i) {
								case 0, 1 -> start(Game.INSTANCE);
								case 2 -> start(FontTest.INSTANCE);
							}
						}
					}
				});
			}
		}));
	}

	@Override
	public void update() {

	}

	@Override
	public void render() {
		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			for (Quad2D button : buttons) {
				button.render();
			}
		});
	}

	public void start(Screen screen) {
		EventManager.INSTANCE.registerListener(listener);
		EventManager.INSTANCE.unregisterListener(this);
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
