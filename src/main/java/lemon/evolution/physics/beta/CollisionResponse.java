package lemon.evolution.physics.beta;

import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;

public enum CollisionResponse {
	IGNORE {
		@Override
		public float execute(Collision collision,
							 MutableVector3D mutablePosition, MutableVector3D mutableVelocity, MutableVector3D mutableForce,
							 Vector3D remainingVelocity, Vector3D remainingForce, float remainingDt,
							 Vector3D usedVelocity, Vector3D usedForce, float usedDt) {
			float length = remainingVelocity.length() - CollisionPacket.BUFFER_DISTANCE;
			if (length > 0) {
				mutablePosition.add(remainingVelocity.scaleToLength(length));
			}
			mutableVelocity.add(remainingForce);
			return 0f;
		}
	}, SLIDE {
		@Override
		public float execute(Collision collision,
							 MutableVector3D mutablePosition, MutableVector3D mutableVelocity, MutableVector3D mutableForce,
							 Vector3D remainingVelocity, Vector3D remainingForce, float remainingDt,
							 Vector3D usedVelocity, Vector3D usedForce, float usedDt) {
			var velocity = mutableVelocity.asImmutable();
			var force = mutableForce.asImmutable();
			var negSlidePlaneNormal = collision.negSlidePlaneNormal();
			// update position
			float length = usedVelocity.length() - CollisionPacket.BUFFER_DISTANCE;
			if (length > 0) {
				mutablePosition.add(usedVelocity.scaleToLength(length));
			}
			var unhandledDt = remainingDt - usedDt;
			// update velocity
			mutableVelocity.subtract(negSlidePlaneNormal.multiply(negSlidePlaneNormal.dotProduct(velocity)));
			// friction
			// TODO: Maybe it should be porportional to negSlidePlaneNormal.dotProduct(velocity)? remainingVelocity?
			var normalForceMagnitude = negSlidePlaneNormal.dotProduct(force);
			if (normalForceMagnitude > 0) {
				//var mu = velocity.lengthSquared() < 0.075f * 0.075f ? 1.0f : 0.7f;
				var mu = 0.005f;
				var velocityLength = velocity.length();
				var frictionForceMagnitude = Math.min(mu * normalForceMagnitude, velocityLength); // TODO (use unhandledDt?)
				if (frictionForceMagnitude > 0) {
					var frictionForce = velocity.scaleToLength(frictionForceMagnitude);
					mutableVelocity.subtract(frictionForce);
				}
				mutableForce.subtract(negSlidePlaneNormal.multiply(normalForceMagnitude));
			}
			return unhandledDt;
		}
	}, STOP {
		@Override
		public float execute(Collision collision,
							 MutableVector3D mutablePosition, MutableVector3D mutableVelocity, MutableVector3D mutableForce,
							 Vector3D remainingVelocity, Vector3D remainingForce, float remainingDt,
							 Vector3D usedVelocity, Vector3D usedForce, float usedDt) {
			// update position
			float length = usedVelocity.length() - CollisionPacket.BUFFER_DISTANCE;
			if (length > 0) {
				mutablePosition.add(usedVelocity.scaleToLength(length));
			}
			return 0f;
		}
	}, BOUNCE {
		@Override
		public float execute(Collision collision,
							 MutableVector3D mutablePosition, MutableVector3D mutableVelocity, MutableVector3D mutableForce,
							 Vector3D remainingVelocity, Vector3D remainingForce, float remainingDt,
							 Vector3D usedVelocity, Vector3D usedForce, float usedDt) {
			var velocity = mutableVelocity.asImmutable();
			var negSlidePlaneNormal = collision.negSlidePlaneNormal();
			// update position
			float length = usedVelocity.length() - CollisionPacket.BUFFER_DISTANCE;
			if (length > 0) {
				mutablePosition.add(usedVelocity.scaleToLength(length));
			}
			// update velocity (TODO: Update force instead?)
			mutableVelocity.subtract(negSlidePlaneNormal.multiply(2f * negSlidePlaneNormal.dotProduct(velocity)));
			return remainingDt - usedDt;
		}
	};

	/**
	 * Returns unhandled dt
	 */
	public abstract float execute(Collision collision,
								  MutableVector3D mutablePosition, MutableVector3D mutableVelocity, MutableVector3D mutableForce,
								  Vector3D remainingVelocity, Vector3D remainingForce, float remainingDt,
								  Vector3D usedVelocity, Vector3D usedForce, float usedDt);
}