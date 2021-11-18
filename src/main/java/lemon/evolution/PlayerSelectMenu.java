package lemon.evolution;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableList;
import lemon.engine.control.GLFWWindow;
import lemon.engine.draw.TextModel;
import lemon.engine.font.Font;
import lemon.engine.game.Player;
import lemon.engine.game.Team;
import lemon.engine.game2d.Quad2D;
import lemon.engine.game2d.Triangle2D;
import lemon.engine.math.*;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.destructible.beta.ScalarField;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.world.Location;
import org.checkerframework.checker.units.qual.A;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class PlayerSelectMenu implements Screen {
    private ScalarField<Vector3D> scalarField;
    private GLFWWindow window;
    private int resolutionWidth, resolutionHeight;
    private Font font;
    private ArrayList<Triangle2D> buttons = new ArrayList<>();
    private boolean typing = false;
    private boolean nextCap = false;
    private ArrayList<String> typingTeam = new ArrayList<>();
    private int position;
    private String typeBox;
    private Quad2D finishButton;
    private Quad2D blueHeader;
    private Quad2D redHeader;

    private final Disposables disposables = new Disposables();

    private ArrayList<Quad2D> blueBoxes = new ArrayList<>();
    private ArrayList<String> bluePlayers = new ArrayList<>();
    private ArrayList<Quad2D> redBoxes = new ArrayList<>();
    private ArrayList<String> redPlayers = new ArrayList<>();
    private ArrayList<Vector3D> blueTextPos = new ArrayList<>();
    private ArrayList<Vector3D> redTextPos = new ArrayList<>();
    private final Matrix boxSize = new Matrix(MathUtil.getScalar(Vector3D.of(.00095f, .0013f, .001f)));

    public PlayerSelectMenu (ScalarField<Vector3D> scalarfield, int resolutionWidth, int resolutionHeight) {
        this.scalarField = scalarfield;
        this.resolutionWidth = resolutionWidth;
        this.resolutionHeight = resolutionHeight;
    }

    public void onLoad(GLFWWindow window) {
        this.window = window;
        font = disposables.add(new Font(Paths.get("/res/fonts/FreeSans.fnt")));
        CommonProgramsSetup.setup2D(Matrix.IDENTITY_4);
        blueHeader = new Quad2D(new Box2D(-.55f, .75f, .4f, .1f), Color.WHITE);
        redHeader = new Quad2D(new Box2D(.15f, .75f, .4f, .1f), Color.WHITE);
        buttons.add(new Triangle2D(Vector3D.of(-.575f, .85f, 0), Vector3D.of(-.575f, .75f, 0), Vector3D.of(-.675f, .8f, 0), Color.WHITE));
        buttons.add(new Triangle2D(Vector3D.of(-.125f, .85f, 0), Vector3D.of(-.125f, .75f, 0), Vector3D.of(.025f, .8f, 0), Color.WHITE));
        buttons.add(new Triangle2D(Vector3D.of(.125f, .85f, 0), Vector3D.of(.125f, .75f, 0), Vector3D.of(.025f, .8f, 0), Color.WHITE));
        buttons.add(new Triangle2D(Vector3D.of(.575f, .85f, 0), Vector3D.of(.575f, .75f, 0), Vector3D.of(.675f, .8f, 0), Color.WHITE));
        finishButton = new Quad2D(new Box2D(-.25f, -.6f, .5f, .15f), Color.WHITE);
        blueBoxes.add(new Quad2D(new Box2D(-.5f, .5f, .3f, .1f), Color.WHITE));
        bluePlayers.add("Player1");
        redBoxes.add(new Quad2D(new Box2D(.2f, .5f, .3f, .1f), Color.WHITE));
        redPlayers.add("Player1");
        disposables.add(window.input().mouseButtonEvent().add(event -> {
            if (event.action() == GLFW.GLFW_RELEASE) {
                event.glfwWindow().pollMouse((rawMouseX, rawMouseY) -> {
                    float mouseX = (2f * rawMouseX / event.glfwWindow().getWidth()) - 1f;
                    float mouseY = (2f * rawMouseY / event.glfwWindow().getHeight()) - 1f;
                    for (int i = 0; i < buttons.size(); ++i) {
                        if (buttons.get(i).getTriangle().isInside(Vector3D.of(mouseX, mouseY, 0f))) {
                            switch (i) {
                                case 0:
                                    if (!(bluePlayers.size() <= 1)) {
                                        blueBoxes.remove(blueBoxes.size() - 1);
                                        bluePlayers.remove(bluePlayers.size() - 1);
                                    }
                                    break;
                                case 1:
                                    if (!(bluePlayers.size() >= 5)) {
                                        blueBoxes.add(new Quad2D(new Box2D(-.5f, .5f - blueBoxes.size() *.2f, .3f, .1f), Color.WHITE));
                                        bluePlayers.add("Player" + (bluePlayers.size() + 1));
                                    }
                                    break;
                                case 2:
                                    if (!(redPlayers.size() <= 1)) {
                                        redBoxes.remove(redBoxes.size() - 1);
                                        redPlayers.remove(redPlayers.size() - 1);
                                    }
                                    break;
                                case 3:
                                    if (!(redPlayers.size() >= 5)) {
                                        redBoxes.add(new Quad2D(new Box2D(.2f, .5f - redBoxes.size() * .2f, .3f, .1f), Color.WHITE));
                                        redPlayers.add("Player" + (redPlayers.size() + 1));
                                    }
                                    break;
                            }
                        }
                    }
                    if (finishButton.getBox2D().intersect(mouseX, mouseY)) {
                        start(new Game(scalarField, resolutionWidth, resolutionHeight, redPlayers, bluePlayers));
                    }
                    for (int i = 0; i < blueBoxes.size(); ++i) {
                        if (blueBoxes.get(i).getBox2D().intersect(mouseX, mouseY)) {
                            typing = true;
                            bluePlayers.set(i, "|");
                            typingTeam = bluePlayers;
                            position = i;
                            typeBox = "|";
                        }
                    }
                    for (int i = 0; i < redBoxes.size(); ++i) {
                        if (redBoxes.get(i).getBox2D().intersect(mouseX, mouseY)) {
                            typing = true;
                            redPlayers.set(i, "|");
                            typingTeam = redPlayers;
                            position = i;
                            typeBox = "|";
                        }
                    }
                });
            }
        }));
        disposables.add(window.input().keyEvent().add(event -> {
            if (event.action() == GLFW_PRESS) {
                if (typing) {
                    if (typeBox.equals("|")) {
                        typeBox = "";
                    }
                    if (event.key() == GLFW_KEY_ENTER) {
                        typing = false;
                    } else if (event.key() == GLFW_KEY_SPACE) {
                        typeBox += " ";
                    } else if (event.key() >= 65 && event.key() <= 90 && nextCap) {
                        typeBox += (char) event.key();
                        nextCap = false;
                    } else if (event.key() == GLFW_KEY_LEFT_SHIFT || event.key() == GLFW_KEY_RIGHT_SHIFT) {
                        nextCap = true;
                    } else if (event.key() == GLFW_KEY_BACKSPACE) {
                        if (typeBox.length() > 0) {
                            if (typeBox.length() == 1) {
                                typeBox = "|";
                            } else {
                                typeBox = typeBox.substring(0, typeBox.length() - 1);
                            }
                        }
                    } else if (event.key() >= 65 && event.key() <= 90) {
                        typeBox += (char) (event.key() + 32);
                    } else if (event.key() == GLFW_KEY_2 && nextCap) {
                        typeBox += "@";
                        nextCap = false;
                    } else if (event.key() == GLFW_KEY_6 && nextCap) {
                        typeBox += "^";
                        nextCap = false;
                    } else if (event.key() == GLFW_KEY_7 && nextCap) {
                        typeBox += "&";
                        nextCap = false;
                    } else if (event.key() == GLFW_KEY_8 && nextCap) {
                        typeBox += "*";
                        nextCap = false;
                    } else if (event.key() == GLFW_KEY_9 && nextCap) {
                        typeBox += "(";
                        nextCap = false;
                    } else if (event.key() >= GLFW_KEY_1 && event.key() <= GLFW_KEY_5 && nextCap) {
                        typeBox += (char) (event.key() - 16);
                        nextCap = false;
                    } else if (event.key() >= GLFW_KEY_0 && event.key() <= GLFW_KEY_9) {
                        typeBox += (char) event.key();
                    }
                    if (typeBox.equals("")) {
                        typeBox = "|";
                    }
                    typingTeam.set(position, typeBox);
                }
            }
        }));
    }

    public void start(Screen screen) {
        window.popAndPushScreen(screen);
    }

    public void render() {
        CommonPrograms2D.COLOR.use(program -> {
            for (int i = 0; i < buttons.size(); ++i) {
                buttons.get(i).draw();
            }
            for (int i = 0; i < redBoxes.size(); ++i) {
                redBoxes.get(i).draw();
            }
            for (int i = 0; i < blueBoxes.size(); ++i) {
                blueBoxes.get(i).draw();
            }
            finishButton.draw();
            redHeader.draw();
            blueHeader.draw();
        });
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        CommonPrograms2D.TEXT.use(program -> {
            program.loadVector("color", (Vector3D.of(1f, 0f, 1f)));
            for (int i = 0; i < bluePlayers.size(); ++i) {
                program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(Vector3D.of(-.5f, .5f - i * .2f, 0)).multiply(boxSize));
                new TextModel(font, bluePlayers.get(i)).draw();
            }
            for (int i = 0; i < redPlayers.size(); ++i) {
                program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(Vector3D.of(.2f, .5f - i * .2f, 0)).multiply(boxSize));
                new TextModel(font, redPlayers.get(i)).draw();
            }
            program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(Vector3D.of(-.225f, -.575f, 0)).multiply(boxSize));
            new TextModel(font, "Start Game").draw();
            program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(Vector3D.of(-.55f, .75f, 0)).multiply(boxSize));
            new TextModel(font, "Blue Team").draw();
            program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(Vector3D.of(.15f, .75f, 0)).multiply(boxSize));
            new TextModel(font, "Red Team").draw();
        });
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void update(long deltaTime) {

    }

    @Override
    public void dispose() {
        disposables.dispose();
    }
}
