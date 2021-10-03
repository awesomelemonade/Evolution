package lemon.tutorial2;

import lemon.engine.draw.UnindexedDrawable;
import lemon.engine.math.FloatData;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Toolbox;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class HelloCube extends GLFWBase {
	static final FloatData[] CUBE_VERTICES = {
			v(-1f, -1f, -1f),
			v(-1f, -1f, 1f),
			v(-1f, 1f, 1f),
			v(-1f, -1f, -1f),
			v(-1f, 1f, -1f),
			v(-1f, 1f, 1f),

			v(1f, -1f, -1f),
			v(1f, -1f, 1f),
			v(1f, 1f, 1f),
			v(1f, -1f, -1f),
			v(1f, 1f, -1f),
			v(1f, 1f, 1f),

			v(-1f, -1f, 1f),
			v(-1f,  1f, 1f),
			v( 1f,  1f, 1f),
			v(-1f, -1f, 1f),
			v( 1f, -1f, 1f),
			v( 1f,  1f, 1f),

			v(-1f, -1f, -1f),
			v(-1f,  1f, -1f),
			v( 1f,  1f, -1f),
			v(-1f, -1f, -1f),
			v( 1f, -1f, -1f),
			v( 1f,  1f, -1f),

			v(-1f, 1f, -1f),
			v(-1f, 1f, 1f),
			v( 1f, 1f, 1f),
			v(-1f, 1f, -1f),
			v( 1f, 1f, -1f),
			v( 1f, 1f,  1f),

			v(-1f, -1f, -1f),
			v(-1f, -1f, 1f),
			v( 1f, -1f, 1f),
			v(-1f, -1f, -1f),
			v( 1f, -1f, -1f),
			v( 1f, -1f,  1f),
	};
	static final FloatData[] CUBE_COLORS = {
			c(1f, 0f, 0f, 1f),
			c(1f, 0f, 0f, 1f),
			c(1f, 0f, 0f, 1f),
			c(1f, 0f, 0f, 1f),
			c(1f, 0f, 0f, 1f),
			c(1f, 0f, 0f, 1f),

			c(0f, 1f, 0f, 1f),
			c(0f, 1f, 0f, 1f),
			c(0f, 1f, 0f, 1f),
			c(0f, 1f, 0f, 1f),
			c(0f, 1f, 0f, 1f),
			c(0f, 1f, 0f, 1f),

			c(0f, 0f, 1f, 1f),
			c(0f, 0f, 1f, 1f),
			c(0f, 0f, 1f, 1f),
			c(0f, 0f, 1f, 1f),
			c(0f, 0f, 1f, 1f),
			c(0f, 0f, 1f, 1f),

			c(1f, 0f, 1f, 1f),
			c(1f, 0f, 1f, 1f),
			c(1f, 0f, 1f, 1f),
			c(1f, 0f, 1f, 1f),
			c(1f, 0f, 1f, 1f),
			c(1f, 0f, 1f, 1f),

			c(0f, 1f, 1f, 1f),
			c(0f, 1f, 1f, 1f),
			c(0f, 1f, 1f, 1f),
			c(0f, 1f, 1f, 1f),
			c(0f, 1f, 1f, 1f),
			c(0f, 1f, 1f, 1f),

			c(1f, 1f, 0f, 1f),
			c(1f, 1f, 0f, 1f),
			c(1f, 1f, 0f, 1f),
			c(1f, 1f, 0f, 1f),
			c(1f, 1f, 0f, 1f),
			c(1f, 1f, 0f, 1f),
	};
	private static Vector3D v(float x, float y, float z) {
		return Vector3D.of(x, y, z);
	}
	private static Color c(float r, float g, float b, float a) {
		return new Color(r, g, b, a);
	}
	// repeat w/ shallow copy
	private static FloatData[] r(int count, FloatData vector) {
		var vectors = new FloatData[count];
		for (int i = 0; i < count; i++) {
			vectors[i] = vector;
		}
		return vectors;
	}
	ShaderProgram program;
	UnindexedDrawable drawable;

	@Override
	public void init() {

	}

	@Override
	public void loop() {
	}

	@Override
	public void dispose() {
		program.dispose();
	}

	public static void main(String[] args) {
		new HelloCube().run();
	}
}