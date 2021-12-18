package lemon.evolution.ui.beta;

import lemon.engine.glfw.GLFWInput;

public interface UIChildComponent extends UIComponent {
	public UIComponent parent();
	public default GLFWInput input() {
		return parent().input();
	}
}
