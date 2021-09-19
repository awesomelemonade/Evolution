package lemon.evolution.entity;

import lemon.engine.draw.Drawable;
import lemon.engine.math.MathUtil;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Renderable;
import lemon.evolution.physics.beta.Collision;
import lemon.evolution.physics.beta.CollisionResponse;
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
	private final MutableVector3D force;
	private final Drawable model;

	public RocketLauncherProjectile(Location location, Vector3D velocity, Drawable model) {
		this.world = location.world();
		this.position = MutableVector3D.of(location.position());
		this.velocity = MutableVector3D.of(velocity);
		this.force = MutableVector3D.ofZero();
		this.model = model;
	}

	@Override
	public CollisionResponse onCollide(Collision collision) {
		var explosionPosition = collision.getIntersection();
		world.terrain().generateExplosion(explosionPosition, 8f);
		world.entities().forEach(entity -> {
			float strength = Math.min(10f, 50f / entity.position().distanceSquared(explosionPosition));
			var direction = entity.position().subtract(explosionPosition);
			if (direction.equals(Vector3D.ZERO)) {
				direction = Vector3D.ofRandomUnitVector();
			}
			entity.mutableVelocity().add(direction.scaleToLength(strength));
		});
		world.entities().remove(this);
		return CollisionResponse.STOP;
	}

	@Override
	public void render() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		CommonPrograms3D.LIGHT.getShaderProgram().use(program -> {
			var sunlightDirection = Vector3D.of(0f, 1f, 0f);
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
}
