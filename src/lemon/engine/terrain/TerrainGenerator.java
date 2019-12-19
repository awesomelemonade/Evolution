package lemon.engine.terrain;

import lemon.engine.function.MurmurHash;
import lemon.engine.function.PerlinNoise;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.Vector2D;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public class TerrainGenerator {
	private Function<Vector2D, Float> biomeNoise;
	private PerlinNoise<Vector2D> noise;
	private static final float goldenRatio = (float) ((1f + Math.sqrt(5)) / 2f);
	private static final float root2 = (float) (1f / Math.sqrt(2));

	public TerrainGenerator() {
		ToIntFunction<int[]> pairer = (b) -> SzudzikIntPair.INSTANCE.applyAsInt(b[0], b[1]);
		/*Function<Vector2D, Float> baseFunction =
				new PerlinNoise((s) -> MurmurHash.createWithSeed(s - 6),
				pairer, (x) -> 0f, 1);
		biomeNoise = ((Function<Vector2D, Vector2D>) ((x) -> x.divide(root2)))
				.andThen(baseFunction).andThen(x -> (x + 2.5f) * (goldenRatio - root2 - (root2 / 2f)));*/
		biomeNoise = (x) -> 1f;
		noise = new PerlinNoise((s) -> MurmurHash.createWithSeed(s), pairer, biomeNoise, 6);
	}
	public float generate(float x, float y) {
		return (noise.apply(new Vector2D(x, y).divide(1200f)) - 0.85f) * 35f;
	}
}
