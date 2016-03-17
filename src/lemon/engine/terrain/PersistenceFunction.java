package lemon.engine.terrain;

import lemon.engine.function.Function2D;

public class PersistenceFunction implements Function2D<Float, Float> {
	private Function2D<Float, Float> baseFunction;
	private float amplifier = (float) (1f/Math.sqrt(2));
	private float sizeFactor = 1.8f;
	public PersistenceFunction(Function2D<Float, Float> baseFunction){
		this.baseFunction = baseFunction;
	}
	@Override
	public Float resolve(Float key, Float key2) {
		return Math.abs(baseFunction.resolve(key/sizeFactor, key2/sizeFactor)+0.5f)*amplifier;
	}
}
