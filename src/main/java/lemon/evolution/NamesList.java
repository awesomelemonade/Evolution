package lemon.evolution;

import lemon.engine.math.MathUtil;
import lemon.engine.toolbox.Toolbox;

import java.util.Collections;
import java.util.List;

public class NamesList {
	private final List<String> names;
	public NamesList(String path) {
		names = Toolbox.getFileInLines(path).orElseThrow();
		Collections.shuffle(names);
	}

	public String random() {
		return MathUtil.randomChoice(names);
	}
}
