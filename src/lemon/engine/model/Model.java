package lemon.engine.model;

import lemon.engine.math.VectorData;
import lemon.engine.toolbox.Color;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public record Model(VectorData[][] vertices, int[] indices) {
	public Model(int[] indices, VectorData[]... vertices) {
		this(vertices, indices);
	}
	public <T> T map(BiFunction<int[], VectorData[][], T> consumer) {
		return consumer.apply(indices, vertices);
	}
	public void use(BiConsumer<int[], VectorData[][]> consumer) {
		consumer.accept(indices, vertices);
	}
	public static Model ofColored(int[] indices, VectorData[] vertices, Color color) {
		Color[] colors = new Color[vertices.length];
		Arrays.fill(colors, color);
		return new Model(indices, vertices, colors);
	}
}
