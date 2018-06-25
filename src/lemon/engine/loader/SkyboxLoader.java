package lemon.engine.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import lemon.engine.texture.CubeMapData;
import lemon.engine.toolbox.Toolbox;

public class SkyboxLoader {
	private File directory;
	private File config;

	public SkyboxLoader(File directory, File config) {
		this.directory = directory;
		this.config = config;
	}
	public CubeMapData load() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(config));
			StringTokenizer tokenizer = new StringTokenizer(reader.readLine());
			ByteBuffer[] data = new ByteBuffer[6];
			for (int i = 0; i < data.length; ++i) {
				data[i] = Toolbox.toByteBuffer(ImageIO.read(new File(directory, reader.readLine())));
			}
			reader.close();
			return new CubeMapData(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()),
					data);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("File Not Found: " + config.getAbsolutePath());
		} catch (IOException e) {
			throw new IllegalStateException("IOException: " + e.getMessage());
		}
	}
}
