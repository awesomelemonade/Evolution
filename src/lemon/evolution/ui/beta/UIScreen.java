package lemon.evolution.ui.beta;

import lemon.engine.event.EventManager;
import lemon.engine.render.Renderable;

import java.util.ArrayList;
import java.util.List;

public class UIScreen implements Renderable {
	private List<UIComponent> components;
	public UIScreen() {
		this.components = new ArrayList<>();
	}
	public void addComponent(UIInputComponent component) {
		EventManager.INSTANCE.registerListener(component);
		this.addComponent((UIComponent) component);
	}
	public void addComponent(UIComponent component) {
		components.add(component);
	}
	@Override
	public void render() {
		components.forEach(Renderable::render);
	}
}
