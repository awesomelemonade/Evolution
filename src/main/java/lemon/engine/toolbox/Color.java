package lemon.engine.toolbox;

import lemon.engine.math.Vector3D;
import lemon.engine.math.Vector4D;

public record Color(float red, float green, float blue, float alpha) implements Vector4D {
	public static final Color WHITE = new Color(1f, 1f, 1f);
	public static final Color RED = new Color(1f, 0f, 0f);
	public static final Color GREEN = new Color(0f, 1f, 0f);
	public static final Color BLUE = new Color(0f, 0f, 1f);
	public static final Color YELLOW = new Color(1f, 1f, 0f);
	public static final Color CYAN = new Color(0f, 1f, 1f);
	public static final Color MAGENTA = new Color(1f, 0f, 1f);
	public static final Color BLACK = new Color(0f, 0f, 0f);
	public static final Color GRAY = new Color(0.5f, 0.5f, 0.5f);
	public static final Color ORANGE = new Color(1f, 0.5f, 0f);
	public static final Color PURPLE = new Color(0.5f, 0f, 1f);
	public static final Color BROWN = Color.fromHex("#4E3524");
	public static final Color CLEAR = new Color(0f, 0f, 0f, 0f);

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

	public Color(String red, String green, String blue) {
		this(Float.parseFloat(red), Float.parseFloat(green), Float.parseFloat(blue));
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
	public float x() {
		return red;
	}

	@Override
	public float y() {
		return green;
	}

	@Override
	public float z() {
		return blue;
	}

	@Override
	public float w() {
		return alpha;
	}

	public boolean isClear() {
		return alpha == 0f;
	}

	public float r() {
		return red;
	}

	public float g() {
		return green;
	}

	public float b() {
		return blue;
	}

	public float a() {
		return alpha;
	}

	public Color brighter() {
		return new Color(Math.min(red * 1.4f, 1f), Math.min(green * 1.4f, 1f), Math.min(blue * 1.4f, 1f), alpha);
	}

	public static Color fromHex(String hex) {
		if (hex.charAt(0) == '#') {
			return new Color(
					Integer.valueOf(hex.substring(1, 3), 16) / 255f,
					Integer.valueOf(hex.substring(3, 5), 16) / 255f,
					Integer.valueOf(hex.substring(5, 7), 16) / 255f
			);
		} else {
			return new Color(
					Integer.valueOf(hex.substring(0, 2), 16) / 255f,
					Integer.valueOf(hex.substring(2, 4), 16) / 255f,
					Integer.valueOf(hex.substring(4, 6), 16) / 255f
			);
		}
	}
}
