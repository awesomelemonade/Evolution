package lemon.engine.toolbox;

import lemon.engine.texture.CubeMapData;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.StringTokenizer;

public class SkyboxLoader {
	private String directory;
	private String configFilename;

	public SkyboxLoader(String directory) {
		this(directory, findConfigFilename(directory));
	}

	private static String findConfigFilename(String directory) {
		try {
			var uri = SkyboxLoader.class.getResource(directory).toURI();
			if (uri.getScheme().equals("jar")) {
				try (var fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
					return findConfigFilename(fileSystem.getPath(directory));
				}
			} else {
				return findConfigFilename(Paths.get(uri));
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
	}

	private static String findConfigFilename(Path path) throws IOException {
		return Files.walk(path).map(p -> p.getFileName().toString())
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
