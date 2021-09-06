package lemon.evolution.ui.beta;

import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.Box2D;
import lemon.engine.toolbox.Disposable;

public class UIToggleButton implements UIInputComponent {
	private Box2D box;
	private boolean toggled;

	@Override
	public Disposable registerInputEvents(GLFWInput input) {
		return null;
	}

	@Override
	public void render() {

	}

	public boolean isToggled() {
		return toggled;
	}
}
