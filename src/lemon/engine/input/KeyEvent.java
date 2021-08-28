package lemon.engine.input;

public interface KeyEvent extends KeyboardEvent, KeyMods, InputAction {
	public int key();

	public int scancode();
}
