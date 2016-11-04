package lemon.engine.terrain;

import java.util.function.BinaryOperator;

public class PersistenceFunction implements BinaryOperator<Float> {
	private BinaryOperator<Float> baseFunction;
	private float amplifier;
	private float sizeFactor;
	public PersistenceFunction(BinaryOperator<Float> baseFunction, float amplifier, float sizeFactor){
		this.baseFunction = baseFunction;
		this.amplifier = amplifier;
		this.sizeFactor = sizeFactor;
	}
	@Override
	public Float apply(Float key, Float key2) {
		return Math.abs(baseFunction.apply(key/sizeFactor, key2/sizeFactor)+0.5f)*amplifier;
	}
}
