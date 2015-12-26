package lemon.engine.terrain;

public class PerlinNoise {
	/*public float lerp(float a0, float a1, float w){
		return (1f-w)*a0+w*a1;
	}
	public float dotGridGradient(int ix, int iy, float x, float y){
		float dx = x-(float)ix;
		float dy = y-(float)iy;
		return (dx*gradient[iy][ix][0]+dy*gradient[iy][ix][1]);
	}
	public float perlin(float x, float y){
		int x0 = (x>0?(int)x:(int)x-1);
		int x1 = x0+1;
		int y0 = (y>0?(int)y:(int)y-1);
		int y1 = y0+1;
		float sx = x-(float)x0;
		float sy = y-(float)y0;
		float n0 = dotGridGradient(x0, y0, x, y);
		float n1 = dotGridGradient(x1, y0, x, y);
		float ix0 = lerp(n0, n1, sx);
		n0 = dotGridGradient(x0, y1, x, y);
		n1 = dotGridGradient(x1, y1, x, y);
		float ix1 = lerp(n0, n1, sx);
		return lerp(ix0, ix1, sy);
	}*/
	//hash functions
	public static int hash32shift(int key){
		key = ~key+(key<<15);
		key = key^(key>>>12);
		key = key+(key<<2);
		key = key^(key>>>4);
		key = key*2057;
		key = key^(key>>>16);
		return key;
	}
	public static int noise(int x, int y, int seed){
		return hash32shift(seed+hash32shift(x+hash32shift(y)));
	}
}
