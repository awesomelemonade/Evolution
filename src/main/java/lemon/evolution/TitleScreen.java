package lemon.evolution;

import lemon.engine.control.GLFWWindow;
import lemon.engine.draw.CommonDrawables;
import lemon.engine.font.CommonFonts;
import lemon.engine.font.Font;
import lemon.engine.function.MurmurHash;
import lemon.engine.function.PerlinNoise;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.*;
import lemon.engine.render.CommonRenderables;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.texture.TextureData;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.ui.beta.UIScreen;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.ToIntFunction;

public enum TitleScreen implements Screen {
    INSTANCE;
    private static final int BACKGROUND_LOOP_TIME = 128;

    private final Disposables disposables = new Disposables();
    private GLFWWindow window;
    private UIScreen screen;

    @Override
    public void onLoad(GLFWWindow window) {
        this.window = window;
        ToIntFunction<int[]> pairer = (b) -> Math.toIntExact(SzudzikIntPair.pair(b[0], b[1], b[2] * 32 + b[3]));
        PerlinNoise<Vector4D> noise = new PerlinNoise<>(4, MurmurHash::createWithSeed, pairer, x -> 1f, 3);
        var min = Float.MAX_VALUE;
        var max = Float.MIN_VALUE;
        var width = 32;
        var height = 32;
        var vector = MutableVector4D.ofZero();
        var buffers = new ByteBuffer[BACKGROUND_LOOP_TIME];
        for (int i = 0; i < buffers.length; i++) {
            buffers[i] = BufferUtils.createByteBuffer(width * height * 4);
        }
        for (int k = 0; k < BACKGROUND_LOOP_TIME; k++) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    var radius = BACKGROUND_LOOP_TIME / 8;
                    var angle = ((float) k) / BACKGROUND_LOOP_TIME * MathUtil.TAU;
                    var value = noise.apply(vector.set(i - width / 2f, j - height / 2f, (float) (radius * Math.cos(angle)), (float) (radius * Math.sin(angle)))
                            .divide(64f).asImmutable());
                    min = Math.min(min, value);
                    max = Math.max(max, value);
                    int intValue = Math.max(0, Math.min(255, (int) (256f * ((value / 5f) + 0.5f))));
                    buffers[k].put((byte) intValue);
                    buffers[k].put((byte) intValue);
                    buffers[k].put((byte) intValue);
                    buffers[k].put((byte) 0b11111111);
                }
            }
            buffers[k].flip();
        }
        var texture = disposables.add(new Texture());
        texture.load(Arrays.stream(buffers).map(buffer -> new TextureData(width, height, buffer)).toArray(TextureData[]::new));
        TextureBank.PERLIN_NOISE.bind(() -> {
            GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, texture.id());
        });
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
        var box = new Box2D(0, 0, window.getWidth(), window.getHeight());
        CommonPrograms2D.PERLIN.use(program -> {
            CommonRenderables.boxToMatrix(box, matrix -> {
                program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrix);
                program.loadInt("time", (int) ((System.currentTimeMillis() / 50.0) % BACKGROUND_LOOP_TIME));
                CommonDrawables.TEXTURED_QUAD.draw();
            });
        });
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
