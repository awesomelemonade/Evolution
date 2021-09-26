package lemon.tutorial;

import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import lemon.engine.toolbox.Toolbox;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class HelloTriangle2 extends GLFWBase {
	static final float[] TRIANGLE_DATA = {
			-1f, -1f, 0f,
			1f, -1f, 0f,
			0f, 1f, 0f
	};
	ShaderProgram program;
	VertexArray vao;
	VertexBuffer vbo;

	@Override
	public void init() {
		program = new ShaderProgram(new int[] {0}, new String[] {"position"},
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/tutorial/vertexShader").orElseThrow()),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/tutorial/fragmentShader").orElseThrow()));
		vao = new VertexArray();
		vao.bind(vao -> {
			vbo = new VertexBuffer();
			vbo.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, Toolbox.toFloatBuffer(TRIANGLE_DATA), GL15.GL_STATIC_DRAW);
				GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
			});
			GL20.glEnableVertexAttribArray(0);
		});
	}

	@Override
	public void loop() {
		program.use(program -> {
			vao.bind(vao -> {
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
			});
		});
	}

	@Override
	public void dispose() {
		vao.dispose();
		vbo.dispose();
		program.dispose();
	}

	public static void main(String[] args) {
		new HelloTriangle2().run();
	}
}
