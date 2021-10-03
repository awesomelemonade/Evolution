package lemon.tutorial;

import lemon.engine.draw.UnindexedDrawable;
import lemon.engine.math.FloatData;
import lemon.engine.math.Vector3D;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.toolbox.Toolbox;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class HelloTriangle3 extends GLFWBase {
	static final FloatData[] TRIANGLE_VERTICES =
			{v(-1f, -1f, 0f), v(1f, -1f, 0f), v(0f, 1f, 0f)};
	private static Vector3D v(float x, float y, float z) {
		return Vector3D.of(x, y, z);
	}
	ShaderProgram program;
	UnindexedDrawable drawable;

	@Override
	public void init() {
		program = ShaderProgram.of(new String[] {"position"}, program -> {},
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/tutorial/vertexShader").orElseThrow()),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/tutorial/fragmentShader").orElseThrow()));
		drawable = new UnindexedDrawable(new FloatData[][] {TRIANGLE_VERTICES}, GL11.GL_TRIANGLES);
	}

	@Override
	public void loop() {
		program.use(program -> {
			drawable.draw();
		});
	}

	@Override
	public void dispose() {
		program.dispose();
	}

	public static void main(String[] args) {
		new HelloTriangle3().run();
	}
}