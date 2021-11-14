package lemon.engine.font;

import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureData;
import lemon.engine.toolbox.Disposable;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Font implements Disposable {
	private int lineHeight;
	private int base;
	private int scaleWidth;
	private int scaleHeight;
	private Texture texture;
	private Map<Integer, CharData> data;
	private Map<Integer, Map<Integer, Integer>> kernings;

	public Font(Path path) {
		texture = new Texture();
		data = new HashMap<>();
		kernings = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(Font.class.getResourceAsStream(path.toString().replaceAll("\\\\", "/"))));
			reader.readLine(); // String info = reader.readLine();
			String common = reader.readLine();
			String page = reader.readLine();
			String chars = reader.readLine();
			String filename = page.substring("page id=0 file=\"".length(), page.length() - "\"".length());
			processCommon(common);
			int charCount = Integer.parseInt(chars.substring("chars count=".length()));
			texture.load(new TextureData(ImageIO.read(Font.class.getResourceAsStream((path.getParent().toString() + "/" + filename).replaceAll("\\\\", "/"))), false));
			for (int i = 0; i < charCount; ++i) {
				processCharData(reader.readLine());
			}
			String kernings = reader.readLine();
			int kerningsCount = Integer.parseInt(kernings.substring("kernings count=".length()));
			for (int i = 0; i < kerningsCount; ++i) {
				processKerning(reader.readLine());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public CharData getCharData(char c) {
		return data.get((int) c);
	}

	public int getKerning(char a, char b) {
		int intA = (int) a;
		int intB = (int) b;
		if (!kernings.containsKey(intA)) {
			return 0;
		}
		if (!kernings.get(intA).containsKey(intB)) {
			return 0;
		}
		return kernings.get(intA).get(intB);
	}

	public void processCommon(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);
		tokenizer.nextToken(); // common
		lineHeight = getValue(tokenizer.nextToken());
		base = getValue(tokenizer.nextToken());
		scaleWidth = getValue(tokenizer.nextToken());
		scaleHeight = getValue(tokenizer.nextToken());
	}

	public void processCharData(String line) {
		String[] data = line.split("\\s+"); // Splits Any Whitespace
		this.data.put(getValue(data[1]), new CharData(getValue(data[2]), getValue(data[3]), getValue(data[4]),
				getValue(data[5]), getValue(data[6]), getValue(data[7]), getValue(data[8])));
	}

	public void processKerning(String line) {
		String[] data = line.split("\\s+");
		int a = getValue(data[1]);
		int b = getValue(data[2]);
		if (!kernings.containsKey(a)) {
			kernings.put(a, new HashMap<Integer, Integer>());
		}
		kernings.get(a).put(b, getValue(data[3]));
	}

	public static int getValue(String line) {
		return Integer.parseInt(line.substring(line.indexOf('=') + 1, line.length()));
	}

	public Texture getTexture() {
		return texture;
	}

	public int getLineHeight() {
		return lineHeight;
	}

	public int getBase() {
		return base;
	}

	public int getScaleWidth() {
		return scaleWidth;
	}

	public int getScaleHeight() {
		return scaleHeight;
	}

	@Override
	public void dispose() {
		texture.dispose();
	}
}
