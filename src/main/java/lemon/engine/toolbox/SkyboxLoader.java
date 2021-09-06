package lemon.engine.toolbox;

import lemon.engine.texture.CubeMapData;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

public class SkyboxLoader {
	private String directory;
	private String configFilename;

	public SkyboxLoader(String directory, String configFilename) {
		this.directory = directory;
		this.configFilename = configFilename;
	}

	public CubeMapData load() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					SkyboxLoader.class.getResourceAsStream(directory + "/" + configFilename)));
			StringTokenizer tokenizer = new StringTokenizer(reader.readLine());
			ByteBuffer[] data = new ByteBuffer[6];
			for (int i = 0; i < data.length; ++i) {
				InputStream stream = SkyboxLoader.class.getResourceAsStream(directory + "/" + reader.readLine());
				data[i] = Toolbox.toByteBuffer(ImageIO.read(stream));
			}
			reader.close();
			return new CubeMapData(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()),
					data);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(String.format("File Not Found: %s/%s", directory, configFilename));
		} catch (IOException e) {
			throw new IllegalStateException("IOException: " + e.getMessage());
		}
	}
}
