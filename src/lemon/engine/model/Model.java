package lemon.engine.model;

import lemon.engine.math.Vector;
import lemon.engine.toolbox.Color;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public record Model(Vector[][] vertices, int[] indices) {
	public Model(int[] indices, Vector[]... vertices) {
		this(vertices, indices);
	}

	public <T> T map(BiFunction<int[], Vector[][], T> consumer) {
		return consumer.apply(indices, vertices);
	}

	public void use(BiConsumer<int[], Vector[][]> consumer) {
		consumer.accept(indices, vertices);
	}

	public static Model ofColored(int[] indices, Vector[] vertices, Color color) {
		Color[] colors = new Color[vertices.length];
		Arrays.fill(colors, color);
		return new Model(indices, vertices, colors);
	}
}
