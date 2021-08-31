package lemon.engine.toolbox;

import lemon.engine.math.FloatData;
import lemon.engine.math.Vector3D;

import java.nio.FloatBuffer;

public record Color(float red, float green, float blue, float alpha) implements FloatData {
	public static final int NUM_DIMENSIONS = 4;
	public static final Color WHITE = new Color(1f, 1f, 1f);
	public static final Color RED = new Color(1f, 0f, 0f);
	public static final Color GREEN = new Color(0f, 1f, 0f);
	public static final Color BLUE = new Color(0f, 0f, 1f);
	public static final Color YELLOW = new Color(1f, 1f, 0f);
	public static final Color CYAN = new Color(0f, 1f, 1f);
	public static final Color MAGENTA = new Color(1f, 0f, 1f);

	public Color() {
		this(1f);
	}

	public Color(float value) {
		this(value, value, value, 1f);
	}

	public Color(float red, float green, float blue) {
		this(red, green, blue, 1f);
	}

	public Color(Color color) {
		this(color.red, color.green, color.blue, color.alpha);
	}

	public Vector3D asRGBVector() {
		return Vector3D.of(this.red(), this.green(), this.blue());
	}

	public static Color randomOpaque() {
		return new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
	}

	public static Color random() {
		return new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random());
	}

	public static Color[] randomOpaque(int size) {
		Color[] colors = new Color[size];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = Color.randomOpaque();
		}
		return colors;
	}

	public static Color[] random(int size) {
		Color[] colors = new Color[size];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = Color.random();
		}
		return colors;
	}

	@Override
	public int numDimensions() {
		return NUM_DIMENSIONS;
	}

	@Override
	public void putInBuffer(FloatBuffer buffer) {
		buffer.put(red);
		buffer.put(green);
		buffer.put(blue);
		buffer.put(alpha);
	}

	@Override
	public void putInArray(float[] array) {
		array[0] = red;
		array[1] = green;
		array[2] = blue;
		array[3] = alpha;
	}
}
