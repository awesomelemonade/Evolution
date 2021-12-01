package lemon.engine.toolbox;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Toolbox {
	private Toolbox() {
	}

	public static Optional<BufferedImage> readImage(String path) {
		return Optional.ofNullable(Toolbox.class.getResourceAsStream(path)).map(stream -> {
			try {
				return ImageIO.read(stream);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		});
	}

	public static Optional<StringBuilder> getFile(String path) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(Toolbox.class.getResourceAsStream(path)));
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line).append("\n");
			}
			reader.close();
			return Optional.of(builder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public static Optional<List<String>> getFileInLines(String path) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(Toolbox.class.getResourceAsStream(path)));
			List<String> lines = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
			return Optional.of(lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public static int getNumLines(String path) {
		try {
			BufferedReader lineCountReader = new BufferedReader(new InputStreamReader(
					Toolbox.class.getResourceAsStream(path)));
			int lines = 0;
			while (lineCountReader.readLine() != null) {
				lines++;
			}
			return lines;
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new IllegalStateException(ex);
		}
	}

	public static ByteBuffer toByteBuffer(BufferedImage image, boolean inverted) {
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4); // 4=RGBA 3=RGB
		if (inverted) {
			for (int y = image.getHeight() - 1; y >= 0; y--) {
				for (int x = 0; x < image.getWidth(); x++) {
					int pixel = pixels[y * image.getWidth() + x];
					buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
					buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green
					buffer.put((byte) (pixel & 0xFF)); // Blue
					buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
				}
			}
		} else {
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int pixel = pixels[y * image.getWidth() + x];
					buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
					buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green
					buffer.put((byte) (pixel & 0xFF)); // Blue
					buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
				}
			}
		}
		buffer.flip();
		return buffer;
	}

	public static FloatBuffer toFloatBuffer(float... floats) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(floats.length);
		for (float f : floats) {
			buffer.put(f);
		}
		buffer.flip();
		return buffer;
	}

	public static FloatBuffer toFloatBuffer(List<Float> floats) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(floats.size());
		for (float f : floats) {
			buffer.put(f);
		}
		buffer.flip();
		return buffer;
	}

	public static IntBuffer toIntBuffer(int... ints) {
		IntBuffer buffer = BufferUtils.createIntBuffer(ints.length);
		for (int i : ints) {
			buffer.put(i);
		}
		buffer.flip();
		return buffer;
	}

	public static IntBuffer toIntBuffer(List<Integer> ints) {
		IntBuffer buffer = BufferUtils.createIntBuffer(ints.size());
		for (int i : ints) {
			buffer.put(i);
		}
		buffer.flip();
		return buffer;
	}
}
