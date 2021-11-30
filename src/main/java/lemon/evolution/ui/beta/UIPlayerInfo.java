package lemon.evolution.ui.beta;

import lemon.engine.font.CommonFonts;
import lemon.engine.math.Box2D;
import lemon.engine.math.Vector2D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Color;

import java.util.function.Supplier;

public class UIPlayerInfo extends AbstractUIComponent {
	private static final Color BACKGROUND_COLOR = Color.fromHex("#515457");
	private static final Color BACKGROUND_TEXT_COLOR = Color.fromHex("#262626");
	private static final Color BACKGROUND_HEALTH_COLOR = Color.fromHex("#262626");
	private static final Color TEXT_COLOR = Color.WHITE;
	private static final Color HEALTH_BAR_COLOR = Color.RED;
	private static final float PADDING = 2;
	private final Box2D box;
	private final Box2D textBox;

	public UIPlayerInfo(UIComponent parent, Box2D box, Supplier<Float> healthGetter) {
		super(parent);
		String name = "Waffles";
		this.box = box;
		var innerHeight = (box.height() - 3f * PADDING) / 2f;
		this.textBox = new Box2D(box.x() + PADDING, box.y() + 2 * PADDING + innerHeight, box.width() - 2 * PADDING, innerHeight);
		var progressBox = new Box2D(box.x() + PADDING, box.y() + PADDING, box.width() - 2 * PADDING, innerHeight);
		this.children().add(UIText.ofHeight(this, CommonFonts.freeSansTightened(), name, Vector2D.of(textBox.x(), textBox.y() + PADDING), textBox.height() - 2 * PADDING, TEXT_COLOR));
		var progressBar = new UIProgressBar(this, progressBox, healthGetter, UIProgressBar.ProgressDirection.RIGHT);
		progressBar.setColor(HEALTH_BAR_COLOR);
		progressBar.setBackgroundColor(BACKGROUND_HEALTH_COLOR);
		this.children().add(progressBar);
	}

	@Override
	public void render() {
		if (isVisible()) {
			CommonRenderables.renderQuad2D(box, BACKGROUND_COLOR);
			CommonRenderables.renderQuad2D(textBox, BACKGROUND_TEXT_COLOR);
			children().forEach(Renderable::render);
		}
	}
}
