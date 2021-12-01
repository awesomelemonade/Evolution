package lemon.evolution;

import lemon.engine.control.GLFWWindow;
import lemon.engine.draw.TextModel;
import lemon.engine.font.CommonFonts;
import lemon.engine.game2d.Quad2D;
import lemon.engine.math.Box2D;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector2D;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposables;
import lemon.engine.toolbox.Toolbox;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.ui.beta.UIScreen;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public enum Menu implements Screen {
	INSTANCE;
	private GLFWWindow window;
	private List<Quad2D> buttons;
	private List<TextModel> buttonsText;
	private final Disposables disposables = new Disposables();
	private int drawnButtons = 0;
	private final int NUM_BUTTONS = 3;
	private Quad2D scrollBar;
	private boolean mouseDown = false;
	private final Matrix buttonSize = MathUtil.getScalar(Vector3D.of(.001f, .0013f, .001f));
	private List<MenuButton> menuButtons;
	private SkyboxBackground background;
	private Matrix orthoProjectionMatrix;
	private UIScreen screen;

	@Override
	public void onLoad(GLFWWindow window) {
		System.gc(); // Manual Garbage Collection
		this.window = window;
		this.background = disposables.add(new SkyboxBackground(MathUtil.randomChoice(EvolutionSkyboxes.values())));

		var windowWidth = window.getWidth();
		var windowHeight = window.getHeight();
		orthoProjectionMatrix = MathUtil.getOrtho(windowWidth, windowHeight, -1, 1);

		screen = disposables.add(new UIScreen(window.input()));
		screen.addCenteredText(CommonFonts.freeSansTightened(), "EVOLUTION", Vector2D.of(windowWidth / 2f, windowHeight * 3f / 4f), 0.75f, Color.WHITE);

		menuButtons = new ArrayList<>();
		menuButtons.add(new MenuButton("Instructions", this::showInstructions));
		var highResWidth = window.getWidth();
		var highResHeight = window.getHeight();
		var lowResWidth = window.getWidth() / 2;
		var lowResHeight = window.getHeight() / 2;
		for (var map : EvolutionMaps.values()) {
			menuButtons.add(new MenuButton(map.mapName(), () -> start(new Game(highResWidth, highResHeight, map))));
			menuButtons.add(new MenuButton(map.mapName() + "*", () -> start(new Game(lowResWidth, lowResHeight, map))));
		}
		menuButtons.add(new MenuButton("Splash", () -> start(TitleScreen.INSTANCE)));
		menuButtons.add(new MenuButton("Credits", this::showCredits));

		var font = CommonFonts.freeSansTightened();
		CommonProgramsSetup.setup2D(Matrix.IDENTITY_4);
		var projection = new Projection(MathUtil.toRadians(60f),
				((float) window.getWidth()) / ((float) window.getHeight()), 0.01f, 1000f);
		CommonProgramsSetup.setup3D(MathUtil.getPerspective(projection));
		buttons = new ArrayList<>();
		buttonsText = new ArrayList<>();
		for (int i = 0; i < menuButtons.size(); ++i) {
			buttons.add(new Quad2D(new Box2D(-0.3f, -.3f - i * 0.2f, 0.6f, 0.1f), new Color(1f, 1f, 1f)));
			buttonsText.add(new TextModel(font, menuButtons.get(i).text()));
		}
		float height = (.5f  / (float)(buttons.size()-2));
		scrollBar = new Quad2D(new Box2D(0.35f, -.2f - height, .025f, height), new Color(1f, 1f, 1f));
		disposables.add(window.input().mouseScrollEvent().add(event -> {
			if (event.yOffset() > 0) {
				if (drawnButtons != 0) {
					--drawnButtons;
				}
			} else {
				if (drawnButtons != buttons.size() - 3) {
					++drawnButtons;
				}
			}
		}));
		disposables.add(window.input().cursorPositionEvent().add(event -> {
			if (mouseDown) {
				event.glfwWindow().pollMouse((rawMouseX, rawMouseY) -> {
					float mouseY = (2f * rawMouseY / event.glfwWindow().getHeight()) - 1f;
					if (mouseY < -.7f) {
						mouseY = -.7f;
					}
					else if (mouseY > -.2f) {
						mouseY = -.2f;
					}
					drawnButtons = (int) (-((mouseY + .2)) / height);
				});
			}
		}));
		disposables.add(window.input().mouseButtonEvent().add(event -> {
			if (event.action() == GLFW.GLFW_PRESS) {
				event.glfwWindow().pollMouse((rawMouseX, rawMouseY) -> {
					float mouseX = (2f * rawMouseX / event.glfwWindow().getWidth()) - 1f;
					float mouseY = (2f * rawMouseY / event.glfwWindow().getHeight()) - 1f;
					if (0.35f <= mouseX && mouseX <= 0.375f) {
						if (-.2f >= mouseY && mouseY >= -.7f) {
							drawnButtons = (int) (-((mouseY + .2)) / height);
							mouseDown = true;
						}
					}
				});
			}
			if (event.action() == GLFW.GLFW_RELEASE) {
				event.glfwWindow().pollMouse((rawMouseX, rawMouseY) -> {
					mouseDown = false;
					float mouseX = (2f * rawMouseX / event.glfwWindow().getWidth()) - 1f;
					float mouseY = (2f * rawMouseY / event.glfwWindow().getHeight()) - 1f;
					for (int i = 0; i < buttons.size(); ++i) {
						if (buttons.get(i).getBox2D().intersect(mouseX, mouseY - drawnButtons * .2f)) {
							menuButtons.get(i).action().run();
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
		background.render();
		CommonPrograms2D.MENUBUTTON.use(program -> {
			for (int i = 0; i < NUM_BUTTONS; ++i) {
				program.loadFloat("yOffset", .2f * (drawnButtons + i) - .2f * i);
				buttons.get(drawnButtons + i).draw();
			}
		});
		CommonPrograms2D.MENUSCROLLER.use(program -> {
			program.loadFloat("scrollPortion", ((.5f / (float)(buttons.size() - 2)) * drawnButtons));
			scrollBar.draw();
		});
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		CommonPrograms2D.TEXT.use(program -> {
			program.loadColor3f(Color.BLACK);
			for (int i = 0; i < NUM_BUTTONS; ++i) {
				var buttonText = buttonsText.get(drawnButtons + i);
				program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(Vector3D.of(-buttonText.width() / 48f / 2f * 0.0475f,
						-.3f - (i * .2f), 0f)).multiply(buttonSize));
				buttonText.draw();
			}
		});
		GL11.glDisable(GL11.GL_BLEND);
		CommonPrograms2D.setMatrices(MatrixType.PROJECTION_MATRIX, orthoProjectionMatrix);
		screen.render();
		CommonPrograms2D.setMatrices(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
	}

	public void start(Screen screen) {
		window.popAndPushScreen(screen);
	}

	public void showInstructions() {
		window.popAndPushScreen(new TextScreen(List.of(
				"Instructions",
				"Move - WASD",
				"Jump/Fall - Space/Shift",
				"Use Item - Mouse1",
				"Rotate Camera - Mouse2",
				"Start Game - Enter",
				"End Turn - Backspace",
				"Inventory - E",
				"Free Camera - J",
				"Toggle Minimap - M",
				"Toggle UI - F1",
				"Screenshot - F2",
				"Debug - F3"
		), this));
	}

	public void showCredits() {
		var text = Toolbox.getFileInLines("/res/credits.txt")
				.orElseGet(() -> List.of("Unable to load credits"));
		window.popAndPushScreen(new TextScreen(text, this));
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}

	private record MenuButton(String text, Runnable action) {}
}
