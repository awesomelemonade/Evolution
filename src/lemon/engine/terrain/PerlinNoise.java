package lemon.engine.terrain;

public class PerlinNoise {
	private HashFunction hasher;
	private PairingFunction pairer;
	private float persistence;
	private int iterations;
	
	public PerlinNoise(HashFunction hasher, PairingFunction pairer, float persistence, int iterations){
		this.hasher = hasher;
		this.pairer = pairer;
		this.persistence = persistence;
		this.iterations = iterations;
	}
	public float noise(float x){
		float output = 0;
		for(int i=0;i<iterations;++i){
			float frequency = (float)Math.pow(2, i); //pow too slow?
			float amplitude = (float)Math.pow(persistence, i); //pow too slow?
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
	public float interpolate(float a, float b, float x){ //linear
		return a*(1-x)+b*x;
	}
	/**
	 * 
	 * @param x
	 * @return [-1 -> 1]
	 */
	public float hash(int x){
		return ((float)hasher.hash(x))/((float)(Integer.MAX_VALUE));
	}
}
