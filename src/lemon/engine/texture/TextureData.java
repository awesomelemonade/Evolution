package lemon.engine.texture;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import lemon.engine.toolbox.Toolbox;

public class TextureData {
	private int width;
	private int height;
	private ByteBuffer data;
	public TextureData(int width, int height, ByteBuffer data){
		this.width = width;
		this.height = height;
		this.data = data;
	}
	public TextureData(BufferedImage image){
		this(image.getWidth(), image.getHeight(), Toolbox.toByteBuffer(image));
	}
	public int getWidth(){
		return width;
	}
	public int getHeight(){
		return height;
	}
	public ByteBuffer getData(){
		return data;
	}
}
