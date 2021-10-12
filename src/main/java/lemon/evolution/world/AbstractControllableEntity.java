package lemon.evolution.world;

import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;

public class AbstractControllableEntity extends AbstractEntity implements ControllableEntity {
	private final MutableVector3D rotation = MutableVector3D.ofZero();

	public AbstractControllableEntity(Location location, Vector3D velocity) {
		super(location, velocity);
	}

	@Override
	public MutableVector3D mutableRotation() {
		return rotation;
	}
}
