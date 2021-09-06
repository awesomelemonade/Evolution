package lemon.evolution.puzzle;

import lemon.engine.draw.Drawable;
import lemon.engine.draw.IndexedDrawable;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.model.Model;
import lemon.engine.model.ModelBuilder;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Color;
import lemon.evolution.util.CommonPrograms3D;
import org.lwjgl.opengl.GL11;

public class PuzzleGrid implements Renderable {
	private static final Vector3D[] VERTICES = new Vector3D[] {
			Vector3D.of(-1, -1, -1),
			Vector3D.of(-1, -1, 1),
			Vector3D.of(-1, 1, -1),
			Vector3D.of(-1, 1, 1),
			Vector3D.of(1, -1, -1),
			Vector3D.of(1, -1, 1),
			Vector3D.of(1, 1, -1),
			Vector3D.of(1, 1, 1),
	};
	private static final int[] INDICES = new int[] {
			0, 1, 2, 1, 2, 3, // -x face
			4, 5, 6, 5, 6, 7, // +x face
			0, 1, 4, 1, 4, 5, // -y face
			2, 3, 6, 3, 6, 7, // +y face
			0, 2, 4, 2, 4, 6, // -z face
			1, 3, 5, 3, 5, 7  // +z face
	};
	private Drawable drawable;

	public PuzzleGrid() {
		drawable = new ModelBuilder().addVertices(VERTICES).addIndices(INDICES)
				.build((indices, vertices) -> Model.ofColored(indices, vertices, Color.randomOpaque()))
				.map(IndexedDrawable::new);
	}

	@Override
	public void render() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		CommonPrograms3D.COLOR.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getScalar(Vector3D.of(10f, 1f, 10f)));
			drawable.draw();
		});
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
