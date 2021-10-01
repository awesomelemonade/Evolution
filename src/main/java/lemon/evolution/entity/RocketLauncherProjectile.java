package lemon.evolution.entity;

import lemon.engine.draw.Drawable;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.physics.beta.CollisionResponse;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms3D;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.Location;
import org.lwjgl.opengl.GL11;

public class RocketLauncherProjectile extends AbstractEntity implements Disposable, Renderable {
	private final Disposables disposables = new Disposables();
	private final Drawable model;
	private int count = 0;

	public RocketLauncherProjectile(Location location, Vector3D velocity, Drawable model) {
		super(location, velocity);
		this.model = model;
		this.disposables.add(this.onCollide().add(collision -> {
			count++;
			var explosionPosition = collision.intersection();
			world().terrain().generateExplosion(explosionPosition, 3f);
			world().entities().forEach(entity -> {
				if (entity != this) {
					float strength = Math.min(2f, 10f / entity.position().distanceSquared(explosionPosition));
					var direction = entity.position().subtract(explosionPosition);
					if (direction.equals(Vector3D.ZERO)) {
						direction = Vector3D.ofRandomUnitVector();
					}
					entity.mutableVelocity().add(direction.scaleToLength(strength));
				}
			});
		}));
		this.disposables.add(this.onUpdate().add(() -> {
			if (position().y() < -200f) {
				world().entities().remove(this);
			}
		}));
	}

	@Override
	public Vector3D getEnvironmentalForce() {
		return count == 0 ? Vector3D.ZERO : super.getEnvironmentalForce();
	}

	@Override
	public CollisionResponse getCollisionResponse() {
		if (count >= 3) {
			world().entities().remove(this);
			return CollisionResponse.STOP;
		} else {
			return CollisionResponse.BOUNCE;
		}
	}

	@Override
	public void render() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		CommonPrograms3D.LIGHT.getShaderProgram().use(program -> {
			var sunlightDirection = Vector3D.of(0f, 1f, 0f);
			try (var translationMatrix = MatrixPool.ofTranslation(position());
				 var rotationMatrix = MatrixPool.ofLookAt(velocity());
				 var adjustedMatrix = MatrixPool.ofRotationY(MathUtil.PI / 2f);
				 var scalarMatrix = MatrixPool.ofScalar(0.2f, 0.2f, 0.2f)) {
				program.loadMatrix(MatrixType.MODEL_MATRIX, translationMatrix.multiply(rotationMatrix).multiply(adjustedMatrix).multiply(scalarMatrix));
				program.loadVector("sunlightDirection", sunlightDirection);
				model.draw();
			}
		});
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
