package lemon.evolution.entity;

import lemon.engine.math.Vector3D;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.Location;

public class StaticEntity extends AbstractEntity {
	public StaticEntity(Location location, Type type) {
		super(location, Vector3D.ZERO, type.scalar());
		setType(type);
	}

	public static enum Type {
		ROCKET_LAUNCHER(1.0f), CRATE(1.0f), PARACHUTE(1.0f);
		private final Vector3D scalar;

		private Type(float scalar) {
			this(Vector3D.of(scalar, scalar, scalar));
		}
		private Type(Vector3D scalar) {
			this.scalar = scalar;
		}

		public Vector3D scalar() {
			return scalar;
		}
	}
}
