package lemon.tutorial2;

import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.toolbox.Toolbox;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class HelloTriangle extends GLFWBase {
	static final float[] TRIANGLE_DATA = {
			-1f, -1f, 0f,
			1f, -1f, 0f,
			0f, 1f, 0f
	};
	ShaderProgram program;
	int vao;
	int vbo;
	@Override
	public void init() {
		program = ShaderProgram.of(new String[] {"position"}, program -> {},
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/tutorial/vertexShader").orElseThrow()),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/tutorial/fragmentShader").orElseThrow()));
		vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		vbo = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, TRIANGLE_DATA, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glEnableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}

	@Override
	public void loop() {
		GL20.glUseProgram(program.id());
		GL30.glBindVertexArray(vao);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
	}

	@Override
	public void dispose() {
		GL15.glDeleteBuffers(vbo);
		GL30.glDeleteVertexArrays(vao);
		program.dispose();
	}

	public static void main(String[] args) {
		new HelloTriangle().run();
	}
}