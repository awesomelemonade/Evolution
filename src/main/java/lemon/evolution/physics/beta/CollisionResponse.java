package lemon.evolution.physics.beta;

import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;

public enum CollisionResponse {
	IGNORE {
		@Override
		public float execute(Collision collision, MutableVector3D mutablePosition, MutableVector3D mutableVelocity, MutableVector3D mutableForce,
							 Vector3D remainingVelocity, float remainingDt) {
			float length = remainingVelocity.length() - CollisionPacket.BUFFER_DISTANCE;
			if (length > 0) {
				mutablePosition.add(remainingVelocity.scaleToLength(length));
			}
			return 0f;
		}
	}, SLIDE {
		@Override
		public float execute(Collision collision, MutableVector3D mutablePosition, MutableVector3D mutableVelocity, MutableVector3D mutableForce,
							 Vector3D remainingVelocity, float remainingDt) {
			var position = mutablePosition.asImmutable();
			var velocity = mutableVelocity.asImmutable();
			var force = mutableForce.asImmutable();
			var usedVelocity = remainingVelocity.multiply(collision.getT());
			// update position
			float length = usedVelocity.length() - CollisionPacket.BUFFER_DISTANCE;
			if (length > 0) {
				mutablePosition.add(usedVelocity.scaleToLength(length));
			}
			var unhandledDt = (1f - collision.getT()) * remainingDt;
			var negSlidePlaneNormal = collision.getIntersection().subtract(position.add(usedVelocity)).normalize();
			// update velocity
			mutableVelocity.subtract(negSlidePlaneNormal.multiply(negSlidePlaneNormal.dotProduct(velocity)));
			// friction
			var normalForceMagnitude = negSlidePlaneNormal.dotProduct(force);
			if (normalForceMagnitude > 0) {
				var mu = 1f;
				var velocityLength = velocity.length();
				var frictionForceMagnitude = Math.min(mu * normalForceMagnitude * unhandledDt, velocityLength);
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
		public float execute(Collision collision, MutableVector3D mutablePosition, MutableVector3D mutableVelocity, MutableVector3D mutableForce,
									  Vector3D remainingVelocity, float remainingDt) {
			var usedVelocity = remainingVelocity.multiply(collision.getT());
			// update position
			float length = usedVelocity.length() - CollisionPacket.BUFFER_DISTANCE;
			if (length > 0) {
				mutablePosition.add(usedVelocity.scaleToLength(length));
			}
			return 0f;
		}
	};

	/**
	 * Returns unhandled dt
	 */
	public abstract float execute(Collision collision, MutableVector3D mutablePosition, MutableVector3D mutableVelocity, MutableVector3D mutableForce,
								  Vector3D remainingVelocity, float remainingDt);
}