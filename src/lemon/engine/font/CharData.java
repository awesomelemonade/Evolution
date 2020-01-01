package lemon.engine.font;

public class CharData {
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final int xOffset;
	private final int yOffset;
	private final int xAdvance;

	public CharData(int x, int y, int width, int height, int xOffset, int yOffset, int xAdvance) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.xAdvance = xAdvance;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public int getXOffset() {
		return xOffset;
	}
	public int getYOffset() {
		return yOffset;
	}
	public int getXAdvance() {
		return xAdvance;
	}
}
