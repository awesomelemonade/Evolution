package lemon.engine.texture;

import java.nio.ByteBuffer;

public record CubeMapData(int width, int height, ByteBuffer[] data) {
	public CubeMapData {
		if (data.length != 6) {
			throw new IllegalArgumentException("ByteBuffer array has to be 6 length");
		}
	}
	public int width() {
		return width;
	}
	public int height() {
		return height;
	}
	public ByteBuffer[] data() {
		return data;
	}
}
