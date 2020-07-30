package lemon.evolution.ui.beta;

import lemon.engine.math.Box2D;

public class UIToggleButton implements UIComponent {
	private Box2D box;
	private boolean toggled;

	@Override
	public void render() {

	}
	public boolean isToggled() {
		return toggled;
	}
}
