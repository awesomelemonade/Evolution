package lemon.evolution.setup;

import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.TextureBank;
import lemon.engine.toolbox.Color;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.util.CommonPrograms3D;

public class CommonProgramsSetup {
	public static void setup3D(Matrix projectionMatrix) {
		CommonPrograms3D.initAll();
		CommonPrograms3D.setMatrices(MatrixType.PROJECTION_MATRIX, projectionMatrix);
	}

	public static void setup2D(Matrix projectionMatrix) {
		CommonPrograms2D.initAll();
		CommonPrograms2D.setMatrices(MatrixType.PROJECTION_MATRIX, projectionMatrix);
	}
}
