package lemon.evolution.ui.beta;

import lemon.engine.event.Observable;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Disposable;

import java.util.Optional;
import java.util.Set;

public interface UIComponent extends Renderable, Disposable {
	public Observable<Boolean> visible();
	public default boolean isVisible() {
		return parent().map(UIComponent::isVisible).orElse(true) && visible().getValue();
	}
	public Optional<UIComponent> parent();
	public Set<UIComponent> children();
	public default GLFWInput input() {
		return parent().orElseThrow().input();
	}
}
