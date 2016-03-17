package lemon.engine.function;

public class PerlinNoise2D implements Function2D<Float, Float> {
	private HashFunction hasher;
	private PairingFunction pairer;
	private AbsoluteValue abs;
	private Function2D<Float, Float> persistence;
	private int iterations;

	public PerlinNoise2D(HashFunction hasher, PairingFunction pairer, float persistence, int iterations){
		this(hasher, pairer, new ConstantFunction2D<Float, Float>(persistence), iterations);
	}
	public PerlinNoise2D(HashFunction hasher, PairingFunction pairer, Function2D<Float, Float> persistence, int iterations){
		this.hasher = hasher;
		this.pairer = pairer;
		abs = new AbsoluteValue();
		this.persistence = persistence;
		this.iterations = iterations;
	}
	@Override
	public Float resolve(Float x, Float y){
		float output = 0;
		for(int i=0;i<iterations;++i){
			float frequency = (float)Math.pow(2, i);
			float amplitude = (float)Math.pow(persistence.resolve(x, y), i);
			output+=interpolatedNoise(x*frequency, y*frequency)*amplitude;
		}
		return output;
	}
	public float interpolatedNoise(float x, float y){
		int intX = (int)x;
		float fractionalX = x-intX;
		int intY = (int)y;
		float fractionalY = y-intY;
		
		float v1 = smoothHash2D(intX, intY);
		float v2 = smoothHash2D(intX+1, intY);
		float v3 = smoothHash2D(intX, intY+1);
		float v4 = smoothHash2D(intX+1, intY+1);
		
		float f1 = interpolate(v1, v2, fractionalX);
		float f2 = interpolate(v3, v4, fractionalX);
		
		return interpolate(f1, f2, fractionalY);
	}
	public float interpolate(float a, float b, float x){
		float ft = (float)(x*Math.PI);
		float f = (float) ((1.0-Math.cos(ft)) * 0.5f);
		return a*(1-f)+b*f;
	}
	public float smoothHash2D(int x, int y){
		return (hash2D(x-1, y-1)+hash2D(x-1, y+1)+hash2D(x+1, y-1)+hash2D(x+1, y+1))/16+
				(hash2D(x-1, y)+hash2D(x+1, y)+hash2D(x, y-1)+hash2D(x, y+1))/8+
				hash2D(x, y)/4;
	}
	public float hash2D(int x, int y){
		return hash(pairer.resolve(abs.resolve(x), abs.resolve(y)));
	}
	public float hash(int x){
		return ((float)hasher.resolve(x))/((float)(Integer.MAX_VALUE));
	}
}
