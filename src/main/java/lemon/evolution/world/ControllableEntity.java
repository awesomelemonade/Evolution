package lemon.evolution.world;

import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;

public interface ControllableEntity extends Entity {
	public default Vector3D rotation() {
		return mutableRotation().asImmutable();
	}
	public MutableVector3D mutableRotation();
}
