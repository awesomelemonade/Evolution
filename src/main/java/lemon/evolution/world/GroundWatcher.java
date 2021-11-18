package lemon.evolution.world;

import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;

import java.util.Optional;

public class GroundWatcher implements Disposable {
	private final Disposable disposable;
	private boolean onGround = false;
	private Vector3D groundNormal;
	private Vector3D groundParallel;

	public GroundWatcher(Entity entity) {
		disposable = Disposable.of(
				entity.onUpdate().add(() -> {
					onGround = false;
				}),
				entity.onCollide().add((intersection, negSlidePlaneNormal) -> {
					if (negSlidePlaneNormal.dotProduct(Vector3D.of(0f, -1f, 0f)) > 0) {
						onGround = true;
						groundNormal = negSlidePlaneNormal.invert();
						// TODO: Needs to make use of entity rotation (when it gets added)
						var crossProduct = negSlidePlaneNormal.crossProduct(entity.vectorDirection()).crossProduct(negSlidePlaneNormal);
						if (crossProduct.lengthSquared() > 0.01f) {
							groundParallel = crossProduct.normalize();
						}
					}
				})
		);
	}

	public boolean isOnGround() {
		return onGround;
	}

	public Optional<Vector3D> groundNormal() {
		return onGround ? Optional.ofNullable(groundNormal) : Optional.empty();
	}

	public Optional<Vector3D> groundParallel() {
		return onGround ? Optional.ofNullable(groundParallel) : Optional.empty();
	}

	@Override
	public void dispose() {
		disposable.dispose();
	}
}
