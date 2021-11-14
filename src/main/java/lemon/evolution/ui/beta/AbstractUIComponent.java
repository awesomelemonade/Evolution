package lemon.evolution.ui.beta;

import lemon.engine.event.Observable;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractUIComponent implements UIComponent {
	private final Observable<Boolean> visible = new Observable<>(true);
	private final GLFWInput input;
	private final UIComponent parent;
	private final Set<UIComponent> children = new LinkedHashSet<>();
	protected final Disposables disposables = new Disposables();

	public AbstractUIComponent(GLFWInput input) {
		this.parent = null;
		this.input = input;
	}

	public AbstractUIComponent(UIComponent parent) {
		this.parent = parent;
		this.input = null;
	}

	@Override
	public Observable<Boolean> visible() {
		return visible;
	}

	@Override
	public Optional<UIComponent> parent() {
		return Optional.ofNullable(parent);
	}

	@Override
	public Set<UIComponent> children() {
		return children;
	}

	@Override
	public void dispose() {
		disposables.dispose();
		List.copyOf(children).forEach(Disposable::dispose);
		if (parent != null) {
			parent.children().remove(this);
		}
	}

	@Override
	public GLFWInput input() {
		return input == null ? UIComponent.super.input() : input;
	}
}
