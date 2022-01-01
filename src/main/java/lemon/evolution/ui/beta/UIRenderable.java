package lemon.evolution.ui.beta;

import lemon.engine.render.Renderable;

public class UIRenderable extends AbstractUIChildComponent {
	private final Renderable renderable;
	public UIRenderable(UIComponent parent, Renderable renderable) {
		super(parent);
		this.renderable = renderable;
	}

	@Override
	public void render() {
		renderable.render();
	}
}
