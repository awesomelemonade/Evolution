package lemon.evolution.ui.beta;

import lemon.engine.math.Box2D;
import lemon.engine.math.MathUtil;
import lemon.engine.render.CommonRenderables;
import lemon.engine.toolbox.Color;

import java.util.function.Supplier;

public class UIProgressBar extends AbstractUIComponent {
	private final Box2D box;
	private final Supplier<Float> progressGetter;

	public UIProgressBar(UIComponent parent, Box2D box, Supplier<Float> progressGetter) {
		super(parent);
		this.box = box;
		this.progressGetter = progressGetter;
	}

	@Override
	public void render() {
		CommonRenderables.renderQuad2D(box, Color.WHITE);
		CommonRenderables.renderQuad2D(new Box2D(box.x(), box.y(), box.width() * MathUtil.saturate(progressGetter.get()), box.height()), Color.BLUE);
	}
}