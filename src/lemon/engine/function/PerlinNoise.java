package lemon.engine.function;

import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

public class PerlinNoise implements UnaryOperator<Float> {
	private IntUnaryOperator hasher;
	private float persistence;
	private int iterations;
	
	public PerlinNoise(IntUnaryOperator hasher, float persistence, int iterations){
		this.hasher = hasher;
		this.persistence = persistence;
		this.iterations = iterations;
	}
	@Override
	public Float apply(Float x){
		float output = 0;
		for(int i=0;i<iterations;++i){
			float frequency = (float)Math.pow(2, i);
			float amplitude = (float)Math.pow(persistence, i);
			output+=get(x*frequency)*amplitude;
		}
		return output;
	}
	public float get(float x){
		int intX = (int)x;
		float fractionalX = x-intX;
		float v1 = smoothHash(intX);
		float v2 = smoothHash(intX+1);
		return interpolate(v1, v2, fractionalX);
	}
	public float interpolate(float a, float b, float x){
		float ft = (float)(x*Math.PI);
		float f = (float) ((1.0-Math.cos(ft)) * 0.5f);
		return a*(1-f)+b*f;
	}
	public float smoothHash(int x){
		return hash(x)/2f+hash(x-1)/4f+hash(x+1)/4f;
	}
	public float hash(int x){
		return ((float)hasher.applyAsInt(x))/((float)(Integer.MAX_VALUE));
	}
}
