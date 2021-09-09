package lemon.evolution.world;

import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;

public interface Entity {
	public default Vector3D position() {
		return mutablePosition().asImmutable();
	}
	public default Vector3D velocity() {
		return mutableVelocity().asImmutable();
	}
	public MutableVector3D mutablePosition();
	public MutableVector3D mutableVelocity();
}
