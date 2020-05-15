package lemon.engine.draw;

import lemon.engine.math.Vector;
import lemon.engine.math.Vector2D;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Color;
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
			r(4, c(1f, 1f, 1f, 1f))
	}, GL11.GL_TRIANGLE_STRIP);

	public static final Drawable SKYBOX = new IndexedDrawable(new Vector[][] {
		split(-1f, -1f, -1f, -1f, -1f, 1f, -1f, 1f, -1f, -1f,
				1f, 1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f, 1f, -1f, 1f, 1f, 1f)
	}, new int[] {
			2, 0, 4, 4, 6, 2, 1, 0, 2, 2, 3, 1, 4, 5, 7, 7, 6, 4,
			1, 3, 7, 7, 5, 1, 2, 6, 7, 7, 3, 2, 0, 1, 4, 4, 1, 5
	}, GL11.GL_TRIANGLES);

	// Shortcuts
	private static Vector2D v(float x, float y) {
		return new Vector2D(x, y);
	}
	private static Vector3D v(float x, float y, float z) {
		return new Vector3D(x, y, z);
	}
	private static Color c(float r, float g, float b, float a) {
		return new Color(r, g, b, a);
	}
	// repeat w/ shallow copy
	private static Vector<?>[] r(int count, Vector<?> vector) {
		Vector<?>[] vectors = new Vector<?>[count];
		for (int i = 0; i < count; i++) {
			vectors[i] = vector;
		}
		return vectors;
	}
	private static Vector3D[] split(float... values) {
		if (values.length % 3 != 0) {
			throw new IllegalArgumentException("length of values must be divisible by 3");
		}
		Vector3D[] vectors = new Vector3D[values.length / 3];
		for (int i = 0; i < vectors.length; i++) {
			vectors[i] = new Vector3D(values[i * 3], values[i * 3 + 1], values[i * 3 + 2]);
		}
		return vectors;
	}
}
