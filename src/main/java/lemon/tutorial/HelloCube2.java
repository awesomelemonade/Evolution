package lemon.tutorial;

import lemon.engine.draw.UnindexedDrawable;
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

public class HelloCube2 extends GLFWBase {
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
			v(-1f, 1f, 1f),
			v(1f, 1f, 1f),
			v(-1f, -1f, 1f),
			v(1f, -1f, 1f),
			v(1f, 1f, 1f),

			v(-1f, -1f, -1f),
			v(-1f, 1f, -1f),
			v(1f, 1f, -1f),
			v(-1f, -1f, -1f),
			v(1f, -1f, -1f),
			v(1f, 1f, -1f),

			v(-1f, 1f, -1f),
			v(-1f, 1f, 1f),
			v(1f, 1f, 1f),
			v(-1f, 1f, -1f),
			v(1f, 1f, -1f),
			v(1f, 1f, 1f),

			v(-1f, -1f, -1f),
			v(-1f, -1f, 1f),
			v(1f, -1f, 1f),
			v(-1f, -1f, -1f),
			v(1f, -1f, -1f),
			v(1f, -1f, 1f),
	};
	static final FloatData[] CUBE_TEXTURE_COORDS = {
			v(0f, 0f),
			v(1f, 0f),
			v(1f, 1f),
			v(0f, 0f),
			v(0f, 1f),
			v(1f, 1f),

			v(0f, 0f),
			v(1f, 0f),
			v(1f, 1f),
			v(0f, 0f),
			v(0f, 1f),
			v(1f, 1f),

			v(0f, 0f),
			v(1f, 0f),
			v(1f, 1f),
			v(0f, 0f),
			v(0f, 1f),
			v(1f, 1f),

			v(0f, 0f),
			v(1f, 0f),
			v(1f, 1f),
			v(0f, 0f),
			v(0f, 1f),
			v(1f, 1f),

			v(0f, 0f),
			v(1f, 0f),
			v(1f, 1f),
			v(0f, 0f),
			v(0f, 1f),
			v(1f, 1f),

			v(0f, 0f),
			v(1f, 0f),
			v(1f, 1f),
			v(0f, 0f),
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
	UnindexedDrawable drawable;

	@Override
	public void init() {
		program = ShaderProgram.of(
				new String[] {"position", "textureCoords"},
				program -> {
					program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(Vector3D.of(0f, 0f, 2f)));
					program.loadMatrix(MatrixType.VIEW_MATRIX, MathUtil.lookAt(Vector3D.of(0, 0, 2f)));
					float aspectRatio = ((float) getWindowWidth()) / ((float) getWindowHeight());
					program.loadMatrix(MatrixType.PROJECTION_MATRIX,
							MathUtil.getPerspective(new Projection(60f / 360f * MathUtil.TAU, aspectRatio, 0.01f, 1000f)));
					program.loadInt("textureSampler", TextureBank.REUSE.getId());
				},
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/tutorial/textureVertexShader").orElseThrow()),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/tutorial/textureFragmentShader").orElseThrow())
		);
		drawable = new UnindexedDrawable(new FloatData[][] {CUBE_VERTICES, CUBE_TEXTURE_COORDS}, GL11.GL_TRIANGLES);
		TextureBank.REUSE.bind(() -> {
			var texture = new Texture();
			texture.load(new TextureData(Toolbox.readImage("/res/grass.png").orElseThrow()));
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
		});
	}

	@Override
	public void loop() {
		long time = System.currentTimeMillis();
		double revolutionTime = 5000.0;
		float angle = (float) ((time % revolutionTime) / revolutionTime * MathUtil.TAU);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		program.use(program -> {
			program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(Vector3D.of(0f, 0f, 10f))
					.multiply(MathUtil.getRotationY(angle).multiply(MathUtil.getRotationX(angle))));
			drawable.draw();
		});
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	@Override
	public void dispose() {
		program.dispose();
	}

	public static void main(String[] args) {
		new HelloCube2().run();
	}
}