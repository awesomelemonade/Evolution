package lemon.evolution.puzzle;

import lemon.engine.draw.Drawable;
import lemon.engine.draw.IndexedDrawable;
import lemon.engine.model.AbstractColoredModel;
import lemon.engine.model.SphereModelBuilder;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Renderable;
import lemon.evolution.util.CommonPrograms3D;
import org.lwjgl.opengl.GL11;

public class PuzzleBall implements Renderable {
	private static final int RADIUS = 1;
	private static final int ITERATIONS = 5;
	private static Drawable sphere;
	private Vector3D position;
	private Vector3D velocity;
	public PuzzleBall(Vector3D position, Vector3D velocity) {
		this.position = position;
		this.velocity = velocity;
		if (sphere == null) {
			sphere = new SphereModelBuilder<>(AbstractColoredModel::new, RADIUS, ITERATIONS)
					.build().map(IndexedDrawable::new);
		}
	}

	@Override
	public void render() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		CommonPrograms3D.COLOR.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(position));
			sphere.draw();
		});
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}

	public Vector3D getPosition() {
		return position;
	}
	public Vector3D getVelocity() {
		return velocity;
	}
}
