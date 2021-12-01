package lemon.evolution.ui.beta;

import lemon.engine.math.Box2D;
import lemon.engine.math.MathUtil;
import lemon.engine.render.CommonRenderables;
import lemon.engine.toolbox.Color;

import java.util.function.Supplier;

public class UIProgressBar extends AbstractUIComponent {
	private final Box2D box;
	private final Supplier<Float> progressGetter;
	private final ProgressDirection progressDirection;
	private Color backgroundColor = Color.WHITE;
	private Color color = Color.BLUE;

	public UIProgressBar(UIComponent parent, Box2D box, Supplier<Float> progressGetter) {
		this(parent, box, progressGetter, ProgressDirection.RIGHT);
	}

	public UIProgressBar(UIComponent parent, Box2D box, Supplier<Float> progressGetter, ProgressDirection progressDirection) {
		super(parent);
		this.box = box;
		this.progressGetter = progressGetter;
		this.progressDirection = progressDirection;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color color() {
		return color;
	}

	public void setBackgroundColor(Color color) {
		this.backgroundColor = color;
	}

	public Color backgroundColor() {
		return backgroundColor;
	}

	@Override
	public void render() {
		if (isVisible()) {
			CommonRenderables.renderQuad2D(box, backgroundColor);
			float progress = MathUtil.saturate(progressGetter.get());
			progressDirection.renderPart(box, progress, color);
		}
	}

	public enum ProgressDirection {
		RIGHT {
			@Override
			public void renderPart(Box2D box, float progress, Color color) {
				CommonRenderables.renderQuad2D(new Box2D(box.x(), box.y(), box.width() * progress, box.height()), color);
			}
		}, UP {
			@Override
			public void renderPart(Box2D box, float progress, Color color) {
				CommonRenderables.renderQuad2D(new Box2D(box.x(), box.y(), box.width(), box.height() * progress), color);
			}
		};
		public abstract void renderPart(Box2D box, float progress, Color color);
	}
}