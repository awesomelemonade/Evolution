package lemon.evolution.ui.beta;

public interface UIBaseComponent extends UIComponent {
	public default void render() {
		if (visible()) {
			for (var child : children()) {
				render(child);
			}
		}
	}

	private static void render(UIComponent component) {
		if (component.enabled()) {
			component.render();
			for (var child : component.children()) {
				render(child);
			}
			component.renderOverlay();
		}
	}
}
