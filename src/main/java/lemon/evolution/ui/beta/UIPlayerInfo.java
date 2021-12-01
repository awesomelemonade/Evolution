package lemon.evolution.ui.beta;

import lemon.engine.font.CommonFonts;
import lemon.engine.math.Box2D;
import lemon.engine.math.Vector2D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Color;

import java.util.function.Supplier;

public class UIPlayerInfo extends AbstractUIComponent {
	private static final Color BACKGROUND_COLOR = Color.BLACK;
	private static final Color BACKGROUND_TEXT_COLOR = Color.fromHex("#515457");
	private static final Color BACKGROUND_TEXT_COLOR2 = Color.fromHex("#262626");
	private static final Color BACKGROUND_HEALTH_COLOR = Color.fromHex("#5f6163");
	private static final Color TEXT_COLOR = Color.WHITE;
	private static final float PADDING = 2;
	private final UIProgressBar progressBar;
	private final Box2D box;
	private final Box2D outerTextBox;
	private final Box2D innerTextBox;
	private final ColoredBox[] healthBarHighlights;


	public UIPlayerInfo(UIComponent parent, Box2D box, String name, String info, Color healthBarColor, Supplier<Float> healthGetter) {
		super(parent);
		this.box = box;
		var progressHeight = (box.height() - 3f * PADDING) * 2f / 5f;
		var textBoxHeight = (box.height() - 3f * PADDING) * 3f / 5f;

		var progressBox = new Box2D(box.x() + PADDING, box.y() + PADDING, box.width() - 2 * PADDING, progressHeight);
		var whiteHighlight = new Color(1f, 1f, 1f, 0.2f);
		var blackHighlight = new Color(0f, 0f, 0f, 0.2f);
		healthBarHighlights = new ColoredBox[] {
				new ColoredBox(createUpperBox(progressBox, 0.4f), whiteHighlight),
				new ColoredBox(createUpperBox(progressBox, 0.3f), whiteHighlight),
				new ColoredBox(createLowerBox(progressBox, 0.4f), blackHighlight),
				new ColoredBox(createLowerBox(progressBox, 0.3f), blackHighlight)
		};
		this.outerTextBox = new Box2D(box.x() + PADDING, box.y() + 2 * PADDING + progressHeight, box.width() - 2 * PADDING, textBoxHeight);
		this.innerTextBox = Box2D.ofInner(outerTextBox, PADDING);
		var textHeight = innerTextBox.height() - 2 * PADDING;
		this.children().add(UIText.ofHeight(this, CommonFonts.freeSansTightened(), name,
				Vector2D.of(innerTextBox.x(), innerTextBox.y() + PADDING), textHeight, TEXT_COLOR));
		this.children().add(UIText.ofHeightRightAligned(this, CommonFonts.freeSansTightened(), info,
				Vector2D.of(innerTextBox.x() + innerTextBox.width(), innerTextBox.y() + PADDING), textHeight, TEXT_COLOR));
		progressBar = new UIProgressBar(this, progressBox, healthGetter, UIProgressBar.ProgressDirection.RIGHT);
		progressBar.setColor(healthBarColor);
		progressBar.setBackgroundColor(BACKGROUND_HEALTH_COLOR);
		this.children().add(progressBar);
	}

	private Box2D createUpperBox(Box2D box, float percentage) {
		var height = box.height() * percentage;
		return new Box2D(box.x(), box.y() + (box.height() - height), box.width(), height);
	}

	private Box2D createLowerBox(Box2D box, float percentage) {
		var height = box.height() * percentage;
		return new Box2D(box.x(), box.y(), box.width(), height);
	}

	@Override
	public void render() {
		if (isVisible()) {
			CommonRenderables.renderQuad2D(box, BACKGROUND_COLOR);
			CommonRenderables.renderQuad2D(outerTextBox, BACKGROUND_TEXT_COLOR);
			CommonRenderables.renderQuad2D(innerTextBox, BACKGROUND_TEXT_COLOR2);
			children().forEach(Renderable::render);
			for (var box : healthBarHighlights) {
				CommonRenderables.renderTransparentQuad2D(box.box(), box.color());
			}
		}
	}

	public UIProgressBar progressBar() {
		return progressBar;
	}

	private static record ColoredBox(Box2D box, Color color) {}
}
