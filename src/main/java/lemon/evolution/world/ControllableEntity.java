package lemon.evolution.world;

import lemon.engine.math.MathUtil;
import lemon.engine.math.MutableQuaternion;
import lemon.engine.math.Quaternion;
import lemon.engine.math.Vector3D;

public interface ControllableEntity extends Entity {
	@Override
	public default Vector3D vectorDirection() {
		return MathUtil.getVectorDirection(rotation().toEulerAngles());
	}

	public default Vector3D vectorDirectionFromYaw() {
		return MathUtil.getVectorDirectionFromYaw(rotation().toEulerAngles().yaw());
	}

	public default Quaternion rotation() {
		return mutableRotation().asImmutable();
	}

	public MutableQuaternion mutableRotation();
}
