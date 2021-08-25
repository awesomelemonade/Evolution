package lemon.engine.render;

import lemon.engine.toolbox.Disposable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public record Shader(int id, int type) implements Disposable {
	/**
	 * Creates a shader
	 * @param type of shader; most commonly GL20.GL_VERTEX_SHADER or GL20.GL_FRAGMENT_SHADER
	 * @param sequence to parse the shader
	 */
	public Shader(int type, CharSequence sequence) {
		this(GL20.glCreateShader(type), type);
		GL20.glShaderSource(id, sequence);
		GL20.glCompileShader(id);
		if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			throw new IllegalStateException("Failed to Compile Shader: " + GL20.glGetShaderInfoLog(id));
		}
	}

	@Override
	public void dispose() {
		GL20.glDeleteShader(id);
	}
}
