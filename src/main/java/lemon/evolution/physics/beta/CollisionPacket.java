package lemon.evolution.physics.beta;

import lemon.engine.math.MathUtil;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;

import java.util.function.BiFunction;
import java.util.function.Function;

public class CollisionPacket {
	public static final float BUFFER_DISTANCE = 0.001f;
	private static final int MAX_RECURSION_DEPTH = 5;

	public static void checkTriangle(Vector3D position, Vector3D velocity, Triangle triangle, Collision collision) {
		float minDistanceSquared = Math.min(position.distanceSquared(triangle.a()),
				Math.min(position.distanceSquared(triangle.b()), position.distanceSquared(triangle.c())));
		float bufferedVelocityLength = velocity.length() + 5f; // TODO: temp
		if (minDistanceSquared > bufferedVelocityLength * bufferedVelocityLength) {
			return;
		}
		// isFrontFacingTo
		float normalDotVelocity = triangle.normal().dotProduct(velocity);
		if (normalDotVelocity <= 0.00001f) {
			// if sphere is travelling parallel to the plane
			if (Math.abs(normalDotVelocity) <= 0.0001f) {
				float signedDistanceToTrianglePlane = triangle.normal().dotProduct(position.subtract(triangle.a()));
				if (Math.abs(signedDistanceToTrianglePlane) >= 1.0f) {
					// Sphere is not embedded in plane
					// No collision possible
					return;
				} else {
					// Sphere is embedded in plane
					// It intersects the whole range [0 .. 1]
					// t0 = 0.0f;
				}
			} else {
				float x = triangle.normal().dotProduct(position.subtract(triangle.a())) - 1f;
				if (x >= -0.001f && x <= -normalDotVelocity) {
					float t = MathUtil.clamp(-x / normalDotVelocity, 0f, 1f);
					var planeIntersectionPoint = position.subtract(triangle.normal()).add(velocity.multiply(t));
					if (triangle.isInside(planeIntersectionPoint)) {
						collision.test(t, planeIntersectionPoint);
						return;
					}
				}
			}
			float velocitySquaredLength = velocity.lengthSquared();

			// Check vertices
			checkVertex(velocitySquaredLength, velocity, position, triangle.a(), collision);
			checkVertex(velocitySquaredLength, velocity, position, triangle.b(), collision);
			checkVertex(velocitySquaredLength, velocity, position, triangle.c(), collision);

			// Check against edges
			checkEdge(position, velocity, triangle.a(), triangle.b(), collision);
			checkEdge(position, velocity, triangle.b(), triangle.c(), collision);
			checkEdge(position, velocity, triangle.c(), triangle.a(), collision);
		}
	}

	private static void checkEdge(Vector3D position, Vector3D velocity, Vector3D vertexA, Vector3D vertexB, Collision collision) {
		// https://mrl.nyu.edu/~dzorin/rend05/lecture2.pdf
		var deltaP = position.subtract(vertexA);
		var edge = vertexB.subtract(vertexA).normalize();
		var scaledEdgeA = edge.multiply(edge.dotProduct(velocity));
		var scaledEdgeC = edge.multiply(edge.dotProduct(deltaP));
		var tempA = velocity.subtract(scaledEdgeA);
		var tempC = deltaP.subtract(scaledEdgeC);
		float a = tempA.lengthSquared();
		float b = 2f * tempA.dotProduct(tempC);
		float c = tempC.lengthSquared() - 1f;
		float det = b * b - 4f * a * c;
		if (det < 0f) {
			return;
		}
		float sqrtDet = (float) Math.sqrt(det);
		float temp = b >= 0 ? -b - sqrtDet : -b + sqrtDet;
		float root1Numerator = temp;
		float root1Denominator = 2f * a;
		float rightSideMultiplied1 = Math.min(1f, collision.t()) * root1Denominator;
		if (root1Numerator >= 0f && root1Numerator <= rightSideMultiplied1) {
			float root1 = MathUtil.clamp(root1Numerator / root1Denominator, 0f, 1f);
			var scaledVelocity = velocity.multiply(root1);
			var q = position.add(scaledVelocity);
			float conditionA = edge.dotProduct(q.subtract(vertexA));
			float conditionB = edge.dotProduct(q.subtract(vertexB));
			if (conditionA >= 0f && conditionB <= 0f) {
				float edgeT = conditionA / edge.lengthSquared();
				collision.set(root1, vertexA.add(edge.multiply(edgeT)));
			}
		}
		float root2Numerator = 2f * c;
		float root2Denominator = temp;
		float rightSideMultiplied2 = Math.min(1f, collision.t()) * root2Denominator;
		if (root2Numerator >= 0f && root2Numerator <= rightSideMultiplied2) {
			float root2 = MathUtil.clamp(root2Numerator / root2Denominator, 0f, 1f);
			var scaledVelocity = velocity.multiply(root2);
			var q = position.add(scaledVelocity);
			float conditionA = edge.dotProduct(q.subtract(vertexA));
			float conditionB = edge.dotProduct(q.subtract(vertexB));
			if (conditionA >= 0f && conditionB <= 0f) {
				float edgeT = conditionA / edge.lengthSquared();
				collision.set(root2, vertexA.add(edge.multiply(edgeT)));
			}
		}
	}

	private static void checkVertex(float a, Vector3D velocity, Vector3D base, Vector3D vertex, Collision collision) {
		var base_vertex = base.subtract(vertex);
		float b = 2.0f * velocity.dotProduct(base_vertex);
		float c = vertex.distanceSquared(base) - 1.0f;
		float det = b * b - 4.0f * a * c;
		if (det < 0f) {
			return;
		}
		float sqrtDet = (float) Math.sqrt(det);
		float temp = b >= 0 ? -b - sqrtDet : -b + sqrtDet;
		float root1Numerator = temp;
		float root1Denominator = 2f * a;
		float rightSideMultiplied1 = Math.min(1f, collision.t()) * root1Denominator;
		if (root1Numerator >= 0f && root1Numerator <= rightSideMultiplied1) {
			float t = MathUtil.clamp(root1Numerator / root1Denominator, 0f, 1f);
			collision.set(t, vertex);
		}
		float root2Numerator = 2f * c;
		float root2Denominator = temp;
		float rightSideMultiplied2 = Math.min(1f, collision.t()) * root2Denominator;
		if (root2Numerator >= 0f && root2Numerator <= rightSideMultiplied2) {
			float t = MathUtil.clamp(root2Numerator / root2Denominator, 0f, 1f);
			collision.set(t, vertex);
		}
	}

	// response steps
	public static void collideWithWorld(BiFunction<Vector3D, Vector3D, Collision> collisionChecker,
									   MutableVector3D position, MutableVector3D velocity, MutableVector3D force,
									   float dt, Function<Collision, CollisionResponse> responder) {
		//force.multiply(dt);
		//velocity.add(force.asImmutable()); // Maybe this needs to be done in unhandledDt?
		collideWithWorld(collisionChecker, position, velocity, force, dt, responder, 0, MAX_RECURSION_DEPTH);
	}

	public static void collideWithWorld(BiFunction<Vector3D, Vector3D, Collision> collisionChecker,
										MutableVector3D mutablePosition, MutableVector3D mutableVelocity, MutableVector3D mutableForce,
										float remainingDt, Function<Collision, CollisionResponse> responder,
										int collisionRecursionDepth, int maxRecursionDepth) {
		if (remainingDt <= 0f || collisionRecursionDepth > maxRecursionDepth) {
			return;
		}
		var position = mutablePosition.asImmutable();
		var velocity = mutableVelocity.asImmutable();
		var force = mutableForce.asImmutable();
		var remainingForce = force.multiply(remainingDt);
		// force has to be applied equally throughout time
		// At time t = 0, velocity remains the same
		// At time t = remainingDt, velocity += remainingForce
		// remainingVelocity = integrate t = 0, remainingDt = average at t = 0 & t = remainingDt = remainingForce / 2.0f
		var remainingVelocity = velocity.add(remainingForce.divide(2.0f)).multiply(remainingDt);
		if (remainingVelocity.lengthSquared() < BUFFER_DISTANCE * BUFFER_DISTANCE) {
			return;
		}
		var collision = collisionChecker.apply(position, remainingVelocity);
		if (collision.t() >= 1f) {
			float length = remainingVelocity.length() - BUFFER_DISTANCE;
			if (length > 0) {
				mutablePosition.add(remainingVelocity.scaleToLength(length));
			}
			mutableVelocity.add(remainingForce);
			return;
		}
		var usedDt = remainingDt * collision.t(); // Simplifying assumption
		var usedVelocity = remainingVelocity.multiply(collision.t());
		var usedForce = remainingForce.multiply(collision.t()); // Simplifying assumption
		var negSlidePlaneNormal = collision.intersection().subtract(position.add(usedVelocity)).normalize();
		collision.setNegSlidePlaneNormal(negSlidePlaneNormal); // Needed to apply responder for a response
		var response = responder.apply(collision);
		var unhandledDt = response.execute(
				collision,
				mutablePosition,
				mutableVelocity,
				mutableForce,
				remainingVelocity,
				remainingForce,
				remainingDt,
				usedVelocity,
				usedForce,
				usedDt
		);
		// recursive
		collideWithWorld(collisionChecker, mutablePosition, mutableVelocity, mutableForce, unhandledDt, responder, collisionRecursionDepth + 1, maxRecursionDepth);
	}
}
