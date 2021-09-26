package lemon.tutorial;

import lemon.engine.draw.UnindexedDrawable;
import lemon.engine.math.FloatData;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Toolbox;
import lemon.evolution.util.CommonPrograms3D;
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
		program = new ShaderProgram(new int[] {0}, new String[] {"position"},
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/tutorial/cubeVertexShader").orElseThrow()),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/tutorial/cubeFragmentShader").orElseThrow()));
		drawable = new UnindexedDrawable(new FloatData[][] {CUBE_VERTICES, CUBE_COLORS}, GL11.GL_TRIANGLES);
		program.use(program -> {
			program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(Vector3D.of(0f, 0f, 2f)));
			program.loadMatrix(MatrixType.VIEW_MATRIX, MathUtil.lookAt(Vector3D.of(0, 0, 2f)));
			float aspectRatio = ((float) getWindowWidth()) / ((float) getWindowHeight());
			program.loadMatrix(MatrixType.PROJECTION_MATRIX,
					MathUtil.getPerspective(new Projection(60f / 360f * MathUtil.TAU, aspectRatio, 0.01f, 1000f)));
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
		new HelloCube().run();
	}
}
