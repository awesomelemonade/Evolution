package lemon.engine.font;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import lemon.engine.game2d.Box2D;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureData;

public class Font {
	private int lineHeight;
	private int base;
	private int scaleWidth;
	private int scaleHeight;
	private Texture texture;
	private Map<Integer, CharData> data;
	public Font(File file){
		texture = new Texture();
		data = new HashMap<Integer, CharData>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			reader.readLine(); //String info = reader.readLine();
			String common = reader.readLine();
			String page = reader.readLine();
			String chars = reader.readLine();
			String filename = page.substring("page id=0 file=\"".length(), page.length()-"\"".length());
			processCommon(common);
			int charCount = Integer.parseInt(chars.substring("chars count=".length()));
			texture.load(new TextureData(ImageIO.read(new File(file.getParentFile(), filename))));
			for(int i=0;i<charCount;++i){
				processCharData(reader.readLine());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Box2D getCharBox(char c){
		CharData data = getCharData(c);
		return new Box2D(
				((float)data.getX())/((float)scaleWidth), ((float)data.getY())/((float)scaleHeight),
				((float)data.getWidth())/((float)scaleHeight), ((float)data.getHeight())/((float)scaleHeight)
		);
	}
	public CharData getCharData(char c){
		return data.get((int)c);
	}
	public void processCommon(String line){
		StringTokenizer tokenizer = new StringTokenizer(line);
		tokenizer.nextToken(); //common
		lineHeight = getValue(tokenizer.nextToken());
		base = getValue(tokenizer.nextToken());
		scaleWidth = getValue(tokenizer.nextToken());
		scaleHeight = getValue(tokenizer.nextToken());
	}
	public void processCharData(String line){
		String[] data = line.split("\\s+"); //Splits Any Whitespace
		this.data.put(getValue(data[1]), new CharData(
				getValue(data[2]), getValue(data[3]), getValue(data[4]), getValue(data[5]),
				getValue(data[6]), getValue(data[7]), getValue(data[8])
		));
	}
	public static int getValue(String line){
		return Integer.parseInt(line.substring(line.indexOf('=')+1, line.length()));
	}
	public int getLineHeight(){
		return lineHeight;
	}
	public int getBase(){
		return base;
	}
	public int getScaleWidth(){
		return scaleWidth;
	}
	public int getScaleHeight(){
		return scaleHeight;
	}
	class CharData{
		private final int x;
		private final int y;
		private final int width;
		private final int height;
		private final int xOffset;
		private final int yOffset;
		private final int xAdvance;
		public CharData(int x, int y, int width, int height, int xOffset, int yOffset, int xAdvance){
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.xAdvance = xAdvance;
		}
		public int getX(){
			return x;
		}
		public int getY(){
			return y;
		}
		public int getWidth(){
			return width;
		}
		public int getHeight(){
			return height;
		}
		public int getXOffset(){
			return xOffset;
		}
		public int getYOffset(){
			return yOffset;
		}
		public int getXAdvance(){
			return xAdvance;
		}
	}
}
