package lemon.engine.toolbox;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Histogram {
	private final Map<Integer, Integer> counter = Collections.synchronizedMap(new TreeMap<>());
	private final float size;
	public Histogram(float size) {
		this.size = size;
	}

	public void add(float value) {
		counter.merge((int) Math.floor(value / size), 1, Integer::sum);
	}

	public void print() {
		counter.forEach((key, value) -> System.out.println((key * size) + ", " + value));
	}
}
