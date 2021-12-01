package lemon.evolution;

import lemon.engine.control.GLFWWindow;
import lemon.engine.font.CommonFonts;
import lemon.engine.font.Font;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector2D;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.ui.beta.UIScreen;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class TextScreen implements Screen {
	private final String title;
	private final List<String> text;
	private final Disposables disposables = new Disposables();
	private final Screen nextScreen;
	private UIScreen screen;

	public TextScreen(List<String> text, Screen nextScreen) {
		this.title = text.get(0);
		this.text = text.subList(1, text.size());
		this.nextScreen = nextScreen;
	}

	@Override
	public void onLoad(GLFWWindow window) {
		var windowWidth = window.getWidth();
		var windowHeight = window.getHeight();
		Matrix orthoProjectionMatrix = MathUtil.getOrtho(windowWidth, windowHeight, -1, 1);
		CommonProgramsSetup.setup2D(orthoProjectionMatrix);
		var font = Font.ofCopyWithAdditionalKerning(CommonFonts.freeSans(), -12);
		screen = disposables.add(new UIScreen(window.input()));
		var titleScale = 0.6f;
		var scale = 0.3f;
		var currentY = windowHeight * 0.75f;
		screen.addCenteredText(font, title, Vector2D.of(windowWidth / 2f, currentY), titleScale, Color.WHITE);
		currentY -= font.getLineHeight() * titleScale;
		for (String line : text) {
			screen.addCenteredText(font, line, Vector2D.of(windowWidth / 2f, currentY), scale, Color.WHITE);
			currentY -= font.getLineHeight() * scale;
		}

		disposables.add(window.input().mouseButtonEvent().add(event -> {
			if (event.button() == GLFW.GLFW_MOUSE_BUTTON_1 && event.action() == GLFW.GLFW_RELEASE) {
				window.popAndPushScreen(nextScreen);
			}
		}));
		disposables.add(window.input().keyEvent().add(event -> {
			if (event.action() == GLFW.GLFW_RELEASE &&
					(event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_SPACE)) {
				window.popAndPushScreen(nextScreen);
			}
		}));
	}

	@Override
	public void update(long deltaTime) {

	}

	@Override
	public void render() {
		screen.render();
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
