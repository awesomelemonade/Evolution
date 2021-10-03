package lemon.tutorial2;

import lemon.engine.draw.IndexedDrawable;
import lemon.engine.math.FloatData;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector2D;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.texture.TextureData;
import lemon.engine.toolbox.Toolbox;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class HelloCube3 extends GLFWBase {
	static final int[] CUBE_INDICES = {
			0, 1, 3, 0, 2, 3,
			4, 5, 7, 4, 6, 7,
			8, 9, 11, 8, 10, 11,
			12, 13, 15, 12, 14, 15,
			16, 17, 19, 16, 18, 19,
			20, 21, 23, 20, 22, 23
	};
	static final FloatData[] CUBE_VERTICES = {
			v(-1f, -1f, -1f),
			v(-1f, -1f, 1f),
			v(-1f, 1f, -1f),
			v(-1f, 1f, 1f),

			v(1f, -1f, -1f),
			v(1f, -1f, 1f),
			v(1f, 1f, -1f),
			v(1f, 1f, 1f),

			v(-1f, -1f, 1f),
			v(-1f, 1f, 1f),
			v(1f, -1f, 1f),
			v(1f, 1f, 1f),

			v(-1f, -1f, -1f),
			v(-1f, 1f, -1f),
			v(1f, -1f, -1f),
			v(1f, 1f, -1f),

			v(-1f, 1f, -1f),
			v(-1f, 1f, 1f),
			v(1f, 1f, -1f),
			v(1f, 1f, 1f),

			v(-1f, -1f, -1f),
			v(-1f, -1f, 1f),
			v(1f, -1f, -1f),
			v(1f, -1f, 1f),
	};
	static final FloatData[] CUBE_TEXTURE_COORDS = {
			v(0f, 0f),
			v(1f, 0f),
			v(0f, 1f),
			v(1f, 1f),

			v(0f, 0f),
			v(1f, 0f),
			v(0f, 1f),
			v(1f, 1f),

			v(0f, 0f),
			v(1f, 0f),
			v(0f, 1f),
			v(1f, 1f),

			v(0f, 0f),
			v(1f, 0f),
			v(0f, 1f),
			v(1f, 1f),

			v(0f, 0f),
			v(1f, 0f),
			v(0f, 1f),
			v(1f, 1f),

			v(0f, 0f),
			v(1f, 0f),
			v(0f, 1f),
			v(1f, 1f),
	};

	private static Vector3D v(float x, float y, float z) {
		return Vector3D.of(x, y, z);
	}

	private static Vector2D v(float x, float y) {
		return Vector2D.of(x, y);
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
	IndexedDrawable drawable;

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
		new HelloCube3().run();
	}
}