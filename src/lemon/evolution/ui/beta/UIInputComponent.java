package lemon.evolution.ui.beta;

import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;

public interface UIInputComponent extends UIComponent {
	public Disposable registerInputEvents(GLFWInput input);
}
