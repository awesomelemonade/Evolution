package lemon.engine.input;

public interface MouseButtonEvent extends MouseEvent, KeyMods, InputAction {
	public int button();
}
