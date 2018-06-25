package lemon.engine.texture;

import java.nio.ByteBuffer;

public class CubeMapData {
	private int width;
	private int height;
	private ByteBuffer[] data;

	public CubeMapData(int width, int height, ByteBuffer[] data) {
		if (data.length != 6) {
			throw new IllegalArgumentException("ByteBuffer array has to be 6 length");
		}
		this.width = width;
		this.height = height;
		this.data = data;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public ByteBuffer[] getData() {
		return data;
	}
}
