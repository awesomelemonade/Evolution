package lemon.engine.terrain;

public class PerlinNoise {
	private HashFunction hasher;
	private float persistence;
	private int iterations;
	
	public PerlinNoise(HashFunction hasher, float persistence, int iterations){
		this.hasher = hasher;
		this.persistence = persistence;
		this.iterations = iterations;
	}
	public float noise(float x){
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
		float v1 = hash(intX);
		float v2 = hash(intX+1);
		return interpolate(v1, v2, fractionalX);
	}
	public float interpolate(float a, float b, float x){
		float ft = (float)(x*Math.PI);
		float f = (float) ((1.0-Math.cos(ft)) * 0.5f);
		return a*(1-f)+b*f;
	}
	public float hash(int x){
		return ((float)hasher.hash(x))/((float)(Integer.MAX_VALUE));
	}
}
