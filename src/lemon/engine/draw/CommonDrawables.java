package lemon.engine.draw;

import lemon.engine.math.Vector;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

public class CommonDrawables {
	private static final Vector[] QUAD_VERTICES =
			{v(-1f, 1f, 0f), v(-1f, -1f, 0f), v(1f, 1f, 0f), v(1f, -1f, 0f)};
	public static final Drawable TEXTURED_QUAD = new UnindexedDrawable(new Vector[][] {
			// Positions
			QUAD_VERTICES,
			// Texture Coordinates
			{v(0f, 1f), v(0f, 0f), v(1f, 1f), v(1f, 0f)}
	}, GL11.GL_TRIANGLE_STRIP);

	public static final Drawable COLORED_QUAD = new UnindexedDrawable(new Vector[][] {
			// Positions
			QUAD_VERTICES,
			// Colors
			r(4, 1f, 1f, 1f, 1f)
	}, GL11.GL_TRIANGLE_STRIP);

	public static final Drawable SKYBOX = new IndexedDrawable(new Vector[][] {
		split(3, -1f, -1f, -1f, -1f, -1f, 1f, -1f, 1f, -1f, -1f,
				1f, 1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f, 1f, -1f, 1f, 1f, 1f)
	}, new int[] {
			2, 0, 4, 4, 6, 2, 1, 0, 2, 2, 3, 1, 4, 5, 7, 7, 6, 4,
			1, 3, 7, 7, 5, 1, 2, 6, 7, 7, 3, 2, 0, 1, 4, 4, 1, 5
	}, GL11.GL_TRIANGLES);

	// Shortcuts
	private static Vector v(float... values) {
		return new Vector(values);
	}
	private static Vector[] r(int count, float... values) {
		Vector[] vectors = new Vector[count];
		for (int i = 0; i < count; i++) {
			vectors[i] = v(values);
		}
		return vectors;
	}
	private static Vector[] split(int dimensions, float... values) {
		if (values.length % dimensions != 0) {
			throw new IllegalArgumentException("length of values must be divisible by dimensions");
		}
		Vector[] vectors = new Vector[values.length / dimensions];
		for (int i = 0; i < vectors.length; i++) {
			vectors[i] = new Vector(Arrays.copyOfRange(values, i * dimensions, i * dimensions + dimensions));
		}
		return vectors;
	}
}
