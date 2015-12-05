package lemon.engine.toolbox;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;

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
}
