package lemon.evolution.world;

import lemon.engine.math.MutableQuaternion;
import lemon.engine.math.Vector3D;

public class AbstractControllableEntity extends AbstractEntity implements ControllableEntity {
	private final MutableQuaternion rotation = MutableQuaternion.ofZero();

	public AbstractControllableEntity(Location location, Vector3D velocity) {
		super(location, velocity);
	}

	@Override
	public MutableQuaternion mutableRotation() {
		return rotation;
	}
}
