package lemon.evolution.screen.beta;

import lemon.engine.control.GLFWWindow;
import lemon.engine.toolbox.Disposable;

public interface Screen extends Disposable {
	public void onLoad(GLFWWindow window);

	public void update();

	public void render();
}
