package lemon.evolution;

import lemon.engine.control.GLFWWindow;
import lemon.engine.font.CommonFonts;
import lemon.engine.math.Box2D;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector2D;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposables;
import lemon.engine.toolbox.Toolbox;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.ui.beta.UIButtonList;
import lemon.evolution.ui.beta.UIScreen;

import java.util.ArrayList;
import java.util.List;

public enum Menu implements Screen {
	INSTANCE;

	private static final int NUM_VISIBLE_BUTTONS = 3;
	private GLFWWindow window;
	private final Disposables disposables = new Disposables();
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
		var highResWidth = windowWidth;
		var highResHeight = windowHeight;
		var lowResWidth = windowWidth / 2;
		var lowResHeight = windowHeight / 2;
		for (var map : EvolutionMaps.values()) {
			menuButtons.add(new MenuButton(map.mapName(), () -> start(new Game(highResWidth, highResHeight, map))));
			menuButtons.add(new MenuButton(map.mapName() + "*", () -> start(new Game(lowResWidth, lowResHeight, map))));
		}
		menuButtons.add(new MenuButton("Splash", () -> start(TitleScreen.INSTANCE)));
		menuButtons.add(new MenuButton("Credits", this::showCredits));

		CommonProgramsSetup.setup2D(orthoProjectionMatrix);
		var projection = new Projection(MathUtil.toRadians(60f),
				((float) windowWidth) / ((float) windowHeight), 0.01f, 1000f);
		CommonProgramsSetup.setup3D(MathUtil.getPerspective(projection));

		var menuButtonWidth = 300f;
		var menuButtonHeight = 40f;
		var spacing = 40f;
		var menuButtonsBox = new Box2D(windowWidth / 2f - menuButtonWidth / 2f, 100f, menuButtonWidth, spacing * (NUM_VISIBLE_BUTTONS - 1) + menuButtonHeight * NUM_VISIBLE_BUTTONS);
		screen.addButtonList(menuButtons.stream().map(x -> new UIButtonList.ButtonInfo(x.text(), x.action())).toList(), menuButtonsBox, NUM_VISIBLE_BUTTONS, spacing);
	}

	@Override
	public void update(long deltaTime) {

	}

	@Override
	public void render() {
		background.render();
		screen.render();
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
