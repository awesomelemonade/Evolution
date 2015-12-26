package lemon.engine.terrain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import lemon.engine.game.OpenSimplexNoise;

public class TerrainGenerator {
	//OpenSimplexNoise noise = new OpenSimplexNoise(new Random().nextLong());
	//OpenSimplexNoise noise2 = new OpenSimplexNoise(new Random().nextLong());
	
	private final float TILE_SIZE;
	private static final int SEED = ThreadLocalRandom.current().nextInt();
	OpenSimplexNoise noise = new OpenSimplexNoise(0);
	OpenSimplexNoise noise2 = new OpenSimplexNoise(0);
	PerlinNoiseGenerator perlin = new PerlinNoiseGenerator();
	
	public TerrainGenerator(float TILE_SIZE){
		this.TILE_SIZE = TILE_SIZE;
	}
	
	/*public float generate(float x, float y){
		return Math.max((float)noise.eval(((double)x)/(12.0/TILE_SIZE), ((double)y)/(12.0/TILE_SIZE)), 0f)*3.2f+
				((float)noise2.eval(((double)x)/(30.0/TILE_SIZE), ((double)y)/(30.0/TILE_SIZE)))*8f;
	}
	public float generate(float x, float y){
		return perlin.noise2(x/10F, y/10F)*20f;
	}*/
	List<Float> generated = new ArrayList<Float>();
	Random r = new Random(0);
	public float generate(float x, float y){
		while(((int)x)>generated.size()-1){
			generated.add(r.nextFloat());
		}
		return generated.get((int)x);
		/*if(y!=320){
			return 0f;
		}
		return ThreadLocalRandom.current().nextFloat();*/
		//return ((float)PerlinNoise.hash32shift((int)x))/((float)Integer.MAX_VALUE);
		//return ((float)PerlinNoise.noise((int)x, (int)y, SEED))/((float)Integer.MAX_VALUE/4);
		//return hash((int)x, (int)y);
		//return ((float)hash32(toByteArray((int)x, hash32(toByteArray((int)y), 0)), 0))/((float)Integer.MAX_VALUE);
	}
	public byte[] toByteArray(int... values){
		byte[] array = new byte[values.length*4];
		/*return new byte[]{
				(byte)(value>>>24),
				(byte)(value>>>16),
				(byte)(value>>>8),
				(byte)(value)
		};*/
		for(int i=0;i<values.length;++i){
			array[i] = (byte)(values[i]>>>24);
			array[i+1] = (byte)(values[i]>>>16);
			array[i+2] = (byte)(values[i]>>>8);
			array[i+3] = (byte)(values[i]);
		}
		return array;
	}
	private float hash(int... a){
		int Q = 433494437;
		int result = 0;
		for(int n: a){
			result = result * Q + n * n;
		}
		result*=Q;
		return ((float)result)/((float)Integer.MIN_VALUE);
	}
	public int hash32(byte[] data, int seed) {
        int m = 0x5bd1e995;
        int r = 24;

        int h = seed ^ data.length;

        int len = data.length;
        int len_4 = len >> 2;

        for (int i = 0; i < len_4; i++) {
                int i_4 = i << 2;
                int k = data[i_4 + 3];
                k = k << 8;
                k = k | (data[i_4 + 2] & 0xff);
                k = k << 8;
                k = k | (data[i_4 + 1] & 0xff);
                k = k << 8;
                k = k | (data[i_4 + 0] & 0xff);
                k *= m;
                k ^= k >>> r;
                k *= m;
                h *= m;
                h ^= k;
        }

        int len_m = len_4 << 2;
        int left = len - len_m;

        if (left != 0) {
                if (left >= 3) {
                        h ^= (int) data[len - 3] << 16;
                }
                if (left >= 2) {
                        h ^= (int) data[len - 2] << 8;
                }
                if (left >= 1) {
                        h ^= (int) data[len - 1];
                }

                h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
}

}
