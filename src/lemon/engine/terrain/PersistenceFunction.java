package lemon.engine.terrain;

import lemon.engine.function.Function2D;

public class PersistenceFunction implements Function2D<Float, Float> {
	private Function2D<Float, Float> baseFunction;
	private float amplifier;
	private float sizeFactor;
	public PersistenceFunction(Function2D<Float, Float> baseFunction, float amplifier, float sizeFactor){
		this.baseFunction = baseFunction;
		this.amplifier = amplifier;
		this.sizeFactor = sizeFactor;
	}
	@Override
	public Float resolve(Float key, Float key2) {
		return Math.abs(baseFunction.resolve(key/sizeFactor, key2/sizeFactor)+0.5f)*amplifier;
	}
}
