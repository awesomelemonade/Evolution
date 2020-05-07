package lemon.engine.model;

import lemon.engine.math.Vector;
import lemon.engine.toolbox.Color;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface ColoredModel extends Model {
	public Color[] getColors();
	public default <T> T map(BiFunction<Vector[][], int[], T> consumer) {
		return consumer.apply(new Vector[][] {this.getVertices(), this.getColors()}, this.getIndices());
	}
	public default void use(BiConsumer<Vector[][], int[]> consumer) {
		consumer.accept(new Vector[][] {this.getVertices(), this.getColors()}, this.getIndices());
	}
}
