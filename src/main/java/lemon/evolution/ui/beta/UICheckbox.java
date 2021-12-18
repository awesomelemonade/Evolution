package lemon.evolution.ui.beta;

import lemon.futility.FObservable;
import lemon.engine.math.Box2D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.toolbox.Color;

public class UICheckbox extends AbstractUIChildComponent {
	private final Box2D box;
	private final FObservable<Boolean> toggled = new FObservable<>(false);

	public UICheckbox(UIComponent parent, Box2D box) {
		super(parent);
		this.box = box;
		// TODO - Mouse Input
	}

	@Override
	public void render() {
		if (isToggled()) {
			CommonRenderables.renderQuad2D(box, Color.GREEN);
		} else {
			CommonRenderables.renderQuad2D(box, Color.RED);
		}
	}

	public FObservable<Boolean> toggled() {
		return toggled;
	}

	public boolean isToggled() {
		return toggled.getValue();
	}
}
