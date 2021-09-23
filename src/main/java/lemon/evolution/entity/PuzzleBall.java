package lemon.evolution.entity;

import lemon.engine.draw.Drawable;
import lemon.engine.draw.IndexedDrawable;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;
import lemon.engine.model.Model;
import lemon.engine.model.SphereModelBuilder;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Lazy;
import lemon.evolution.physics.beta.Collision;
import lemon.evolution.physics.beta.CollisionResponse;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms3D;
import lemon.evolution.world.Entity;
import lemon.evolution.world.Location;
import lemon.evolution.world.World;
import org.lwjgl.opengl.GL11;

public class PuzzleBall implements Entity, Renderable {
	private static final int RADIUS = 1;
	private static final int ITERATIONS = 5;
	private static final Lazy<Drawable> sphere = new Lazy<>(() -> {
		return SphereModelBuilder.of(RADIUS, ITERATIONS)
				.build((indices, vertices) -> {
					Color[] colors = new Color[vertices.length];
					for (int i = 0; i < colors.length; i++) {
						colors[i] = Color.randomOpaque();
					}
					return new Model(indices, vertices, colors);
				}).map(IndexedDrawable::new);
	});
	private final World world;
	private final MutableVector3D position;
	private final MutableVector3D velocity;
	private final MutableVector3D force;

	public PuzzleBall(Location location, Vector3D velocity) {
		this.world = location.world();
		this.position = MutableVector3D.of(location.position());
		this.velocity = MutableVector3D.of(velocity);
		this.force = MutableVector3D.ofZero();
	}

	@Override
	public void render() {
		PuzzleBall.render(position());
	}

	@Override
	public World world() {
		return world;
	}

	@Override
	public MutableVector3D mutablePosition() {
		return position;
	}

	@Override
	public MutableVector3D mutableVelocity() {
		return velocity;
	}

	@Override
	public MutableVector3D mutableForce() {
		return force;
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
