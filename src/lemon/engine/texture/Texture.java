package lemon.engine.texture;

import lemon.engine.toolbox.Disposable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

public class Texture implements Disposable {
	private final int id;

	public Texture() {
		id = GL11.glGenTextures();
	}

	public void load(TextureData data) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		// Wrap
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		// Scale
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		// Send to OpenGL
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, data.width(), data.height(), 0, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, data.data());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public void load(CubeMapData data) {
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, id);
		// Wrap
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		// Scale
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		for (int i = 0; i < data.data().length; ++i) {
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, data.width(),
					data.height(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.data()[i]);
		}
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0);
	}

	public int getId() {
		return id;
	}

	@Override
	public void dispose() {
		GL11.glDeleteTextures(id);
	}

	public void bind(int target, Runnable runnable) {
		GL11.glBindTexture(target, id);
		runnable.run();
		GL11.glBindTexture(target, 0);
	}
}
