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

public enum TitleScreen implements Screen {
    INSTANCE;

    private final Disposables disposables = new Disposables();
    private GLFWWindow window;
    private UIScreen screen;

    @Override
    public void onLoad(GLFWWindow window) {
        this.window = window;
        disposables.add(window.input().mouseButtonEvent().add(event -> {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_1 && event.action() == GLFW.GLFW_RELEASE) {
                start();
            }
        }));
        disposables.add(window.input().keyEvent().add(event -> {
            if (event.action() == GLFW.GLFW_RELEASE &&
                    (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_SPACE)) {
                start();
            }
        }));
        var windowWidth = window.getWidth();
        var windowHeight = window.getHeight();
        Matrix orthoProjectionMatrix = MathUtil.getOrtho(windowWidth, windowHeight, -1, 1);
        CommonProgramsSetup.setup2D(orthoProjectionMatrix);
        screen = new UIScreen(window.input());
        screen.addCenteredText(Font.ofCopyWithAdditionalKerning(CommonFonts.freeSans(), -12),
                "EVOLUTION", Vector2D.of(windowWidth / 2f, windowHeight * 0.75f), 0.7f, new Color(0.9f));
    }

    @Override
    public void update(long deltaTime) {

    }

    @Override
    public void render() {
        screen.render();
    }

    public void start() {
        window.popAndPushScreen(Menu.INSTANCE);
    }

    @Override
    public void dispose() {
        disposables.dispose();
    }
}
