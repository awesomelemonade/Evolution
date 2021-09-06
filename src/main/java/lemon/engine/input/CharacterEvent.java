package lemon.engine.input;

public interface CharacterEvent extends KeyboardEvent, KeyMods {
	public int codepoint();
}
