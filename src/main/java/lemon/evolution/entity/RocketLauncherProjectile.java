package lemon.evolution.entity;

import lemon.engine.draw.Drawable;
import lemon.engine.math.MathUtil;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Renderable;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms3D;
import lemon.evolution.world.Entity;
import lemon.evolution.world.Location;
import lemon.evolution.world.World;
import org.lwjgl.opengl.GL11;

import java.util.function.Predicate;

public class RocketLauncherProjectile implements Entity, Renderable {
	private final World world;
	private final MutableVector3D position;
	private final MutableVector3D velocity;
	private final Drawable model;

	public RocketLauncherProjectile(Location location, Vector3D velocity, Drawable model) {
		this.world = location.world();
		this.position = MutableVector3D.of(location.position());
		this.velocity = MutableVector3D.of(velocity);
		this.model = model;
	}

	@Override
	public Predicate<Float> manualUpdater() {
		return dt -> {
			boolean collided = world.collisionContext().collideAndCheck(position, velocity, Vector3D.ZERO, dt);
			if (collided) {
				world.terrain().generateExplosion(position.asImmutable(), 8f);
			}
			return collided;
		};
	}

	@Override
	public void render() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		CommonPrograms3D.LIGHT.getShaderProgram().use(program -> {
			var sunlightDirection = Vector3D.of(0f, -1f, 0f);
			try (var translationMatrix = MatrixPool.ofTranslation(position.asImmutable());
				 var rotationMatrix = MatrixPool.ofLookAt(velocity.asImmutable());
				 var adjustedMatrix = MatrixPool.ofRotationY(MathUtil.PI / 2f)) {
				program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(rotationMatrix).multiply(adjustedMatrix));
				program.loadVector("sunlightDirection", sunlightDirection);
				model.draw();
			}
		});
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	@Override
	public MutableVector3D mutablePosition() {
		return position;
	}

	@Override
	public MutableVector3D mutableVelocity() {
		return velocity;
	}
}
