package lemon.engine.toolbox;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;

public class Toolbox {
	private Toolbox(){}
	
	public static StringBuilder getFile(String path){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
			StringBuilder builder = new StringBuilder();
			String line;
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
			reader.close();
			return builder;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static ByteBuffer toByteBuffer(BufferedImage image){
		int[] pixels = new int[image.getWidth()*image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth()*image.getHeight()*4); //4=RGBA 3=RGB
		for(int y=0;y<image.getHeight();++y){
			for(int x=0;x<image.getWidth();++x){
				int pixel = pixels[y*image.getWidth()+x];
				buffer.put((byte)((pixel>>16)&0xFF)); //Red
				buffer.put((byte)((pixel>>8)&0xFF)); //Green
				buffer.put((byte)(pixel&0xFF)); //Blue
				buffer.put((byte)((pixel>>24)&0xFF)); //Alpha
			}
		}
		buffer.flip();
		return buffer;
	}
	public static FloatBuffer toFloatBuffer(float... floats){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(floats.length);
		for(float f: floats){
			buffer.put(f);
		}
		buffer.flip();
		return buffer;
	}
	public static FloatBuffer toFloatBuffer(List<Float> floats){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(floats.size());
		for(float f: floats){
			buffer.put(f);
		}
		buffer.flip();
		return buffer;
	}
	public static IntBuffer toIntBuffer(int... ints){
		IntBuffer buffer = BufferUtils.createIntBuffer(ints.length);
		for(int i: ints){
			buffer.put(i);
		}
		buffer.flip();
		return buffer;
	}
	public static IntBuffer toIntBuffer(List<Integer> ints){
		IntBuffer buffer = BufferUtils.createIntBuffer(ints.size());
		for(int i: ints){
			buffer.put(i);
		}
		buffer.flip();
		return buffer;
	}
}
