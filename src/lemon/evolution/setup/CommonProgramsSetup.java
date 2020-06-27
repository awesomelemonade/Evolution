package lemon.evolution.setup;

import lemon.engine.toolbox.Color;

import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.TextureBank;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.util.CommonPrograms3D;

public class CommonProgramsSetup {
	public static void setup3D(Matrix projectionMatrix) {
		CommonPrograms3D.initAll();

		CommonPrograms3D.COLOR.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
		});

		CommonPrograms3D.TEXTURE.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
			program.loadInt("textureSampler", TextureBank.REUSE.getId());
		});

		CommonPrograms3D.CUBEMAP.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
			program.loadInt("cubemapSampler", TextureBank.SKYBOX.getId());
		});

		CommonPrograms3D.POST_PROCESSING.getShaderProgram().use(program -> {
			program.loadInt("colorSampler", TextureBank.COLOR.getId());
			program.loadInt("depthSampler", TextureBank.DEPTH.getId());
		});

		CommonPrograms3D.PARTICLE.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
			program.loadColor4f(Color.WHITE);
		});

		CommonPrograms3D.LIGHT.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
		});
	}
	public static void setup2D(Matrix projectionMatrix) {
		CommonPrograms2D.initAll();

		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
			program.loadColor4f("filterColor", Color.WHITE);
		});

		CommonPrograms2D.LINE.getShaderProgram().use(program -> {
			program.loadInt("index", 0);
			program.loadInt("total", 0);
			program.loadFloat("alpha", 1f);
		});

		CommonPrograms2D.TEXT.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
			program.loadVector("color", Vector3D.ZERO);
			program.loadInt("textureSampler", TextureBank.REUSE.getId());
		});
	}
}
