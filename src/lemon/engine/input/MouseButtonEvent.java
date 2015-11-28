package lemon.engine.input;

public interface MouseButtonEvent extends MouseEvent, KeyMods, Action {
	public int getButton();
}
