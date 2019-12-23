package lemon.engine.toolbox;

import lemon.engine.math.Vector;
import lemon.engine.math.Vector3D;

public class Color extends Vector {
	
	public static final Color RED = new Color(1f, 0f, 0f);
	public static final Color GREEN = new Color(0f, 1f, 0f);
	public static final Color BLUE = new Color(0f, 0f, 1f);

	private static final int RED_INDEX = 0;
	private static final int GREEN_INDEX = 1;
	private static final int BLUE_INDEX = 2;
	private static final int ALPHA_INDEX = 3;
	
	public Color() {
		this(1f);
	}
	public Color(float value) {
		this(value, value, value, 1f);
	}
	public Color(float red, float green, float blue) {
		this(red, green, blue, 1f);
	}
	public Color(float red, float green, float blue, float alpha) {
		super(red, green, blue, alpha);
	}
	public Color(Color color) {
		this(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
	public void setRed(float red) {
		super.set(RED_INDEX, red);
	}
	public float getRed() {
		return super.get(RED_INDEX);
	}
	public void setGreen(float green) {
		super.set(GREEN_INDEX, green);
	}
	public float getGreen() {
		return super.get(GREEN_INDEX);
	}
	public void setBlue(float blue) {
		super.set(BLUE_INDEX, blue);
	}
	public float getBlue() {
		return super.get(BLUE_INDEX);
	}
	public void setAlpha(float alpha) {
		super.set(ALPHA_INDEX, alpha);
	}
	public float getAlpha() {
		return super.get(ALPHA_INDEX);
	}
	public Vector3D toVector3D() {
		return new Vector3D(this.getRed(), this.getGreen(), this.getBlue());
	}
	@Override
	public String toString() {
		return String.format("Color[%f, %f, %f, %f]", this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha());
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
}
