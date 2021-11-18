package lemon.evolution.world;

import lemon.engine.math.MathUtil;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;

public interface ControllableEntity extends Entity {
	@Override
	public default Vector3D vectorDirection() {
		return MathUtil.getVectorDirection(rotation());
	}

	public default Vector3D vectorDirectionFromYaw() {
		return MathUtil.getVectorDirectionFromYaw(rotation().y());
	}

	public default Vector3D rotation() {
		return mutableRotation().asImmutable();
	}

	public MutableVector3D mutableRotation();
}
