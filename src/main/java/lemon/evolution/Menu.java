package lemon.evolution;

import lemon.engine.control.GLFWWindow;
import lemon.engine.font.Font;
import lemon.engine.function.MurmurHash;
import lemon.engine.function.PerlinNoise;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.game2d.Quad2D;
import lemon.engine.math.Box2D;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector2D;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposables;
import lemon.engine.toolbox.Histogram;
import lemon.evolution.destructible.beta.ScalarField;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;
import lemon.engine.draw.TextModel;
import lemon.engine.math.MathUtil;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public enum Menu implements Screen {
	INSTANCE;
	private GLFWWindow window;
	private List<Quad2D> buttons;
	private List<TextModel> buttonsText;
	private Font font;
	private final Disposables disposables = new Disposables();
	private int drawnButtons = 0;
	private final int NUM_BUTTONS = 3;
	private Quad2D scrollBar;
	private boolean mouseDown = false;
	private final Matrix buttonSize = new Matrix(MathUtil.getScalar(Vector3D.of(.001f, .0013f, .001f)));


	@Override
	public void onLoad(GLFWWindow window) {
		System.gc(); // Manual Garbage Collection
		this.window = window;
		font = disposables.add(new Font(Paths.get("/res/fonts/FreeSans.fnt")));
		CommonProgramsSetup.setup2D(Matrix.IDENTITY_4);
		buttons = new ArrayList<>();
		buttonsText = new ArrayList<>();
		buttonsText.add(new TextModel(font, "Field"));
		buttonsText.add(new TextModel(font, "Flatter Field"));
		buttonsText.add(new TextModel(font, "Font Test"));
		for (int i = 0; i < 100; ++i) {
			buttons.add(new Quad2D(new Box2D(-0.3f, -.3f - i * 0.2f, 0.6f, 0.1f), new Color(1f, 1f, 1f)));
			buttonsText.add(new TextModel(font, "Button " + (i+4)));
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
							System.out.println((int) (-((mouseY + .2)) / height));
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
							ToIntFunction<int[]> pairer = (b) -> (int) SzudzikIntPair.pair(b[0], b[1], b[2]);
							switch (i) {
								case 0 -> {
									var noise2d = new PerlinNoise<Vector2D>(2, MurmurHash::createWithSeed, (b) -> SzudzikIntPair.pair(b[0], b[1]), x -> 1f, 6);
									PerlinNoise<Vector3D> noise = new PerlinNoise<>(3, MurmurHash::createWithSeed, pairer, x -> 1f, 6);
									ScalarField<Vector3D> scalarField;
									scalarField = vector -> {
										if (vector.y() < 0f) {
											return 0f;
										}
										float distanceSquared = vector.x() * vector.x() + vector.z() * vector.z();
										float cylinder = (float) (50.0 - Math.sqrt(distanceSquared));
										if (cylinder < -100f) {
											return cylinder;
										}
										float terrain = (float) (-Math.tanh(vector.y() / 100.0) * 100.0 +
												Math.pow(2f, noise2d.apply(vector.toXZVector().divide(300f))) * 5.0 +
												Math.pow(2.5f, noise.apply(vector.divide(500f))) * 2.5);
										return Math.min(cylinder, terrain);
									};
									start(new Game(scalarField, window.getWidth(), window.getHeight()));
								}
								case 1 -> {
									var noise2d = new PerlinNoise<Vector2D>(2, MurmurHash::createWithSeed, (b) -> SzudzikIntPair.pair(b[0], b[1]), x -> 1f, 4);
									PerlinNoise<Vector3D> noise = new PerlinNoise<>(3, MurmurHash::createWithSeed, pairer, x -> 1f, 7);
									ScalarField<Vector3D> scalarField;
									scalarField = vector -> {
										if (vector.y() < 0f) {
											return 0f;
										}
										float distanceSquared = vector.x() * vector.x() + vector.z() * vector.z();
										float cylinder = (float) (50.0 - Math.sqrt(distanceSquared));
										if (cylinder < -100f) {
											return cylinder;
										}
										float terrain = (float) (-Math.tanh(vector.y() / 100.0) * 20.0 +
												Math.pow(2.75f, noise2d.apply(vector.toXZVector().divide(300f))) * 3.0 +
												Math.pow(5.75f, noise.apply(vector.divide(500f))) * 3.5);
										return Math.min(cylinder, terrain);
									};
									start(new Game(scalarField, window.getWidth() / 2, window.getHeight() / 2));
								}
								case 2 -> start(FontTest.INSTANCE);
								default -> System.out.println(i);
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
			program.loadVector("color", (Vector3D.of(1f, 0f, 1f)));
			for (int i = 0; i < NUM_BUTTONS; ++i) {
				program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(Vector3D.of(0f - (buttonsText.get(drawnButtons + i).getText().length())*.02f,
						-.3f - (i * .2f), 0f)).multiply(buttonSize));
				buttonsText.get(drawnButtons + i).draw();
			}
		});
		GL11.glDisable(GL11.GL_BLEND);
	}

	public void start(Screen screen) {
		window.popAndPushScreen(screen);
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
