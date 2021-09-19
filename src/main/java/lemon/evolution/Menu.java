package lemon.evolution;

import lemon.engine.control.GLFWWindow;
import lemon.engine.game2d.Quad2D;
import lemon.engine.math.Box2D;
import lemon.engine.math.Matrix;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public enum Menu implements Screen {
	INSTANCE;
	private GLFWWindow window;
	private List<Quad2D> buttons;
	private final Disposables disposables = new Disposables();

	@Override
	public void onLoad(GLFWWindow window) {
		this.window = window;
		CommonProgramsSetup.setup2D(Matrix.IDENTITY_4);
		buttons = new ArrayList<>();
		for (int i = 0; i < 3; ++i) {
			buttons.add(new Quad2D(new Box2D(-0.3f, -0.3f - i * 0.2f, 0.6f, 0.1f), new Color(1f, 1f, 1f)));
		}
		disposables.add(window.input().mouseButtonEvent().add(event -> {
			if (event.action() == GLFW.GLFW_RELEASE) {
				event.glfwWindow().pollMouse((rawMouseX, rawMouseY) -> {
					float mouseX = (2f * rawMouseX / event.glfwWindow().getWidth()) - 1f;
					float mouseY = (2f * rawMouseY / event.glfwWindow().getHeight()) - 1f;
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
	public void update(long deltaTime) {

	}

	@Override
	public void render() {
		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			for (Quad2D button : buttons) {
				button.draw();
			}
		});
	}

	public void start(Screen screen) {
		window.popAndPushScreen(screen);
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
