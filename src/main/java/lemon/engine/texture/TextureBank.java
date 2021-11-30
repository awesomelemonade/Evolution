package lemon.engine.texture;

import org.lwjgl.opengl.GL13;

public enum TextureBank {
	REUSE(0), COLOR(1), DEPTH(2), SKYBOX(3),
	GRASS(4), SLOPE(5), ROCK(6), BASE(7),
	MINIMAP_COLOR(8), MINIMAP_DEPTH(9),
	TERRAIN(10), VIEWMODEL_COLOR(11), VIEWMODEL_DEPTH(12),
	PERLIN_NOISE(13);
	private final int id;

	private TextureBank(int id) {
		this.id = id;
		if (id < 0 || getBind() > GL13.GL_TEXTURE31) {
			throw new IllegalArgumentException("id out of range");
		}
	}

	public int id() {
		return id;
	}

	public int getBind() {
		return GL13.GL_TEXTURE0 + id;
	}

	public void bind(Runnable runnable) {
		GL13.glActiveTexture(this.getBind());
		runnable.run();
		GL13.glActiveTexture(TextureBank.REUSE.getBind());
	}

	public static int getBind(int id) {
		return GL13.GL_TEXTURE0 + id;
	}
}
