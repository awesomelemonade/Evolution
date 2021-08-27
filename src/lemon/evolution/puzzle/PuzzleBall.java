package lemon.evolution.puzzle;

import lemon.engine.draw.Drawable;
import lemon.engine.draw.IndexedDrawable;
import lemon.engine.math.MutableVector3D;
import lemon.engine.model.Model;
import lemon.engine.model.ModelBuilder;
import lemon.engine.model.SphereModelBuilder;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Lazy;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms3D;
import org.lwjgl.opengl.GL11;

public class PuzzleBall implements Renderable {
	private static final int RADIUS = 1;
	private static final int ITERATIONS = 5;
	private static final Lazy<Drawable> sphere = Lazy.of(() -> {
		return SphereModelBuilder.build(new ModelBuilder(), RADIUS, ITERATIONS)
				.build((indices, vertices) -> {
					Color[] colors = new Color[vertices.length];
					for (int i = 0; i < colors.length; i++) {
						colors[i] = Color.randomOpaque();
					}
					return new Model(indices, vertices, colors);
				}).map(IndexedDrawable::new);
	});
	private final MutableVector3D position;
	private final MutableVector3D velocity;
	public PuzzleBall(Vector3D position, Vector3D velocity) {
		this.position = MutableVector3D.of(position);
		this.velocity = MutableVector3D.of(velocity);
	}

	@Override
	public void render() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		CommonPrograms3D.COLOR.getShaderProgram().use(program -> {
			try (var translationMatrix = MatrixPool.ofTranslation(position())) {
				program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix);
			}
			sphere.get().draw();
		});
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}

	public MutableVector3D mutablePosition() {
		return position;
	}
	public MutableVector3D mutableVelocity() {
		return velocity;
	}
	public Vector3D position() {
		return position.toImmutable();
	}
	public Vector3D velocity() {
		return velocity.toImmutable();
	}

	public static void render(Vector3D position) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		CommonPrograms3D.COLOR.getShaderProgram().use(program -> {
			try (var translationMatrix = MatrixPool.ofTranslation(position)) {
				program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix);
			}
			sphere.get().draw();
		});
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	public static void render(Vector3D position, Vector3D scalar) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		CommonPrograms3D.COLOR.getShaderProgram().use(program -> {
			try (var translationMatrix = MatrixPool.ofTranslation(position);
				 var scalarMatrix = MatrixPool.ofScalar(scalar);
				 var transformationMatrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
				program.loadMatrix(MatrixType.MODEL_MATRIX, transformationMatrix);
			}
			sphere.get().draw();
		});
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
