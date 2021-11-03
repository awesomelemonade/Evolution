package lemon.evolution.ui.beta;

import lemon.engine.event.Observable;
import lemon.engine.math.Box2D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.toolbox.Color;

public class UICheckbox extends AbstractUIComponent {
	private final Box2D box;
	private final Observable<Boolean> toggled = new Observable<>(false);

	public UICheckbox(UIComponent parent, Box2D box) {
		super(parent);
		this.box = box;
		// TODO - Mouse Input
	}

	@Override
	public void render() {
		if (isVisible()) {
			if (isToggled()) {
				CommonRenderables.renderQuad2D(box, Color.GREEN);
			} else {
				CommonRenderables.renderQuad2D(box, Color.RED);
			}
		}
	}

	public Observable<Boolean> toggled() {
		return toggled;
	}

	public boolean isToggled() {
		return toggled.getValue();
	}
}
