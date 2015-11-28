package lemon.engine.math;

public class MathUtil {
	
	private MathUtil() {}

	public static boolean[] convertMods(int bitset) {
		boolean[] mods = new boolean[4];
		if ((bitset & (1 << 0)) == 1) mods[0] = true;
		if ((bitset & (1 << 1)) == 1) mods[1] = true;
		if ((bitset & (1 << 2)) == 1) mods[2] = true;
		if ((bitset & (1 << 3)) == 1) mods[3] = true;
		return mods;
	}
	public static float toRadians(float degrees){
		return (float) Math.toRadians(degrees);
	}
	public static float toDegrees(float radians){
		return (float) Math.toDegrees(radians);
	}
}
