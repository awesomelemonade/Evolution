package lemon.engine.toolbox;

import lemon.engine.texture.CubeMapData;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.StringTokenizer;

public class SkyboxLoader {
	private String directory;
	private String configFilename;

	public SkyboxLoader(String directory) {
		this(directory, findConfigFilename(directory));
	}

	private static String findConfigFilename(String directory) {
		var file = new File(SkyboxLoader.class.getResource(directory).getPath());
		return Arrays.stream(file.list())
				.filter(filename -> filename.endsWith(".cfg")).findAny().orElseThrow();
	}

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
				data[i] = Toolbox.toByteBuffer(ImageIO.read(stream), false);
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
