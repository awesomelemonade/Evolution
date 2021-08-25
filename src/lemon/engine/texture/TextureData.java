package lemon.engine.texture;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import lemon.engine.toolbox.Toolbox;

public record TextureData(int width, int height, ByteBuffer data) {
	public TextureData(int width, int height, ByteBuffer data) {
		this.width = width;
		this.height = height;
		this.data = data;
	}
	public TextureData(BufferedImage image) {
		this(image.getWidth(), image.getHeight(), Toolbox.toByteBuffer(image));
	}
}
