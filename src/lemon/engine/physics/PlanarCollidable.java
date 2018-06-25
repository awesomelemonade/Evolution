package lemon.engine.physics;

import lemon.engine.math.Vector3D;

public class PlanarCollidable implements Collidable {
	private CollisionEffect currentEffect;

	@Override
	public float getIntersection(CollisionEffect effect) {
		this.currentEffect = effect;
		return 0;
	}
	@Override
	public void applyResponse() {
		currentEffect.getMotion().set(Vector3D.ZERO);
	}
}
