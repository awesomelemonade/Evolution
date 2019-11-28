package lemon.evolution.setup;

import lemon.evolution.util.ShaderProgramHolder;
import org.lwjgl.opengl.GL20;

import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.TextureBank;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.util.CommonPrograms3D;

public class CommonProgramsSetup {
	public static void setup3D(Matrix projectionMatrix) {
		CommonPrograms3D.initAll();

		GL20.glUseProgram(CommonPrograms3D.COLOR.getShaderProgram().getId());
		loadMatrix(CommonPrograms3D.COLOR, MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		loadMatrix(CommonPrograms3D.COLOR, MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		loadMatrix(CommonPrograms3D.COLOR, MatrixType.PROJECTION_MATRIX, projectionMatrix);
		GL20.glUseProgram(0);

		GL20.glUseProgram(CommonPrograms3D.TEXTURE.getShaderProgram().getId());
		loadMatrix(CommonPrograms3D.TEXTURE, MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		loadMatrix(CommonPrograms3D.TEXTURE, MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		loadMatrix(CommonPrograms3D.TEXTURE, MatrixType.PROJECTION_MATRIX, projectionMatrix);
		CommonPrograms3D.TEXTURE.getShaderProgram().loadInt("textureSampler", TextureBank.REUSE.getId());
		GL20.glUseProgram(0);

		GL20.glUseProgram(CommonPrograms3D.CUBEMAP.getShaderProgram().getId());
		loadMatrix(CommonPrograms3D.CUBEMAP, MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		loadMatrix(CommonPrograms3D.CUBEMAP, MatrixType.PROJECTION_MATRIX, projectionMatrix);
		CommonPrograms3D.CUBEMAP.getShaderProgram().loadInt("cubemapSampler", TextureBank.SKYBOX.getId());
		GL20.glUseProgram(0);

		GL20.glUseProgram(CommonPrograms3D.POST_PROCESSING.getShaderProgram().getId());
		CommonPrograms3D.POST_PROCESSING.getShaderProgram().loadInt("colorSampler", TextureBank.COLOR.getId());
		CommonPrograms3D.POST_PROCESSING.getShaderProgram().loadInt("depthSampler", TextureBank.DEPTH.getId());
		GL20.glUseProgram(0);
	}
	public static void setup2D() {
		CommonPrograms2D.initAll();

		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		loadMatrix(CommonPrograms2D.COLOR, MatrixType.TRANSFORMATION_MATRIX, Matrix.IDENTITY_4);
		loadMatrix(CommonPrograms2D.COLOR, MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		GL20.glUseProgram(0);

		GL20.glUseProgram(CommonPrograms2D.LINE.getShaderProgram().getId());
		CommonPrograms2D.LINE.getShaderProgram().loadInt("index", 0);
		CommonPrograms2D.LINE.getShaderProgram().loadInt("total", 0);
		CommonPrograms2D.LINE.getShaderProgram().loadFloat("alpha", 1f);
		GL20.glUseProgram(0);

		GL20.glUseProgram(CommonPrograms2D.TEXT.getShaderProgram().getId());
		loadMatrix(CommonPrograms2D.TEXT, MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		loadMatrix(CommonPrograms2D.TEXT, MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		loadMatrix(CommonPrograms2D.TEXT, MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms2D.TEXT.getShaderProgram().loadVector("color", Vector3D.ZERO);
		CommonPrograms2D.TEXT.getShaderProgram().loadInt("textureSampler", TextureBank.REUSE.getId());
		GL20.glUseProgram(0);
	}
	private static void loadMatrix(ShaderProgramHolder holder, MatrixType matrixType, Matrix matrix) {
		holder.getShaderProgram().loadMatrix(matrixType, matrix);
	}
}
