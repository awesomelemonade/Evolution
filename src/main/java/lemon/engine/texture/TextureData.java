package lemon.engine.texture;

import lemon.engine.toolbox.Toolbox;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public record TextureData(int width, int height, ByteBuffer data) {
	public TextureData(int width, int height, ByteBuffer data) {
		this.width = width;
		this.height = height;
		this.data = data;
	}

	public TextureData(BufferedImage image, boolean inverted) {
		this(image.getWidth(), image.getHeight(), Toolbox.toByteBuffer(image, inverted));
	}
}
