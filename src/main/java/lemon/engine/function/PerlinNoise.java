package lemon.engine.function;

import lemon.engine.math.Vector;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public class PerlinNoise<T extends Vector<T>> implements Function<T, Float> {
	private IntUnaryOperator abs = AbsoluteIntValue.HASHED;
	private IntUnaryOperator[] hashFunctions;
	private ToIntFunction<int[]> pairFunction;
	private Function<T, Float> persistence;
	private int iterations;
	private int numDimensions;
	private ThreadLocal<int[]> intX;
	private ThreadLocal<float[]> fractionalX;
	private ThreadLocal<float[]> values;
	private ThreadLocal<int[]> a;

	public PerlinNoise(int numDimensions, IntFunction<IntUnaryOperator> hashFunctions, ToIntFunction<int[]> pairFunction,
					   Function<T, Float> persistence, int iterations) {
		this.numDimensions = numDimensions;
		this.hashFunctions = new IntUnaryOperator[iterations];
		this.pairFunction = pairFunction;
		this.persistence = persistence;
		this.iterations = iterations;
		for (int i = 0; i < iterations; i++) {
			this.hashFunctions[i] = hashFunctions.apply(i);
		}
		intX = ThreadLocal.withInitial(() -> new int[numDimensions]);
		fractionalX = ThreadLocal.withInitial(() -> new float[numDimensions]);
		values = ThreadLocal.withInitial(() -> new float[0b1 << numDimensions]); // 2 ^ n
		a = ThreadLocal.withInitial(() -> new int[numDimensions]);
	}

	@Override
	public Float apply(T x) {
		float[] xData = new float[numDimensions];
		x.putInArray(xData);
		float output = 0;
		int[] intX = this.intX.get();
		float[] fractionalX = this.fractionalX.get();
		float[] values = this.values.get();
		int[] a = this.a.get();
		for (int i = 0; i < iterations; ++i) {
			float amplitude = (float) Math.pow(persistence.apply(x), i);
			output += interpolatedNoise(xData, hashFunctions[i], intX, fractionalX, values, a) * amplitude;
			for (int j = 0; j < xData.length; j++) {
				xData[j] *= 2; // frequency
			}
		}
		return output;
	}

	public float interpolatedNoise(float[] x, IntUnaryOperator hashFunction, int[] intX, float[] fractionalX, float[] values, int[] a) {
		for (int i = 0; i < intX.length; i++) {
			intX[i] = (int) Math.floor(x[i]);
			fractionalX[i] = x[i] - intX[i];
		}
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < a.length; j++) {
				a[j] = abs.applyAsInt(intX[j] + ((i >>> j) & 0b1));
			}
			int paired = pairFunction.applyAsInt(a);
			values[i] = ((float) hashFunction.applyAsInt(paired)) / ((float) Integer.MAX_VALUE);
		}
		int size = values.length / 2;
		for (int i = 0; i < numDimensions; i++) {
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
