package lemon.engine.function;

import lemon.engine.math.Vector;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;

public class PerlinNoise<T extends Vector> implements Function<T, Float> {
	private IntUnaryOperator abs = AbsoluteIntValue.HASHED;
	private IntUnaryOperator[] hashFunctions;
	private ToIntFunction<int[]> pairFunction;
	private Function<T, Float> persistence;
	private int iterations;

	public PerlinNoise(IntFunction<IntUnaryOperator> hashFunctions, ToIntFunction<int[]> pairFunction,
					   Function<T, Float> persistence, int iterations) {
		this.hashFunctions = new IntUnaryOperator[iterations];
		this.pairFunction = pairFunction;
		this.persistence = persistence;
		this.iterations = iterations;
		for (int i = 0; i < iterations; i++) {
			this.hashFunctions[i] = hashFunctions.apply(i);
		}
	}
	@Override
	public Float apply(T x) {
		float output = 0;
		for (int i = 0; i < iterations; ++i) {
			float frequency = (float) Math.pow(2, i);
			float amplitude = (float) Math.pow(persistence.apply(x), i);
			output += interpolatedNoise(x.copy().multiply(frequency), hashFunctions[i]) * amplitude;
		}
		return output;
	}
	public float interpolatedNoise(Vector x, IntUnaryOperator hashFunction) {
		int[] intX = new int[x.getDimensions()];
		float[] fractionalX = new float[x.getDimensions()];
		for (int i = 0; i < intX.length; i++) {
			intX[i] = (int) Math.floor(x.get(i));
			fractionalX[i] = x.get(i) - intX[i];
		}
		float[] values = new float[0b1 << x.getDimensions()]; // 2 ^ n
		for (int i = 0; i < values.length; i++) {
			int[] a = new int[x.getDimensions()];
			for (int j = 0; j < a.length; j++) {
				a[j] = abs.applyAsInt(intX[j] + ((i >>> j) & 0b1));
			}
			int paired = pairFunction.applyAsInt(a);
			values[i] = ((float) hashFunction.applyAsInt(paired)) / ((float) Integer.MAX_VALUE);
		}
		int size = values.length / 2;
		for (int i = 0; i < x.getDimensions(); i++) {
			for (int j = 0; j < size; j++) {
				values[j] = interpolate(values[2 * j], values[2 * j + 1], fractionalX[i]);
			}
			size /= 2;
		}
		return values[0];
	}
	public float interpolate(float a, float b, float x) {
		float ft = (float) (x * Math.PI);
		float f = (float) ((1.0 - Math.cos(ft)) * 0.5f);
		return a * (1 - f) + b * f;
	}
}
