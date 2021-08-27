package lemon.engine.input;

public interface KeyEvent extends KeyboardEvent, KeyMods, Action {
	public int getKey();

	public int getScancode();
}
