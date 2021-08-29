package lemon.evolution.physics.beta;

import lemon.engine.math.MathUtil;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CollisionPacket {
	private static final float BUFFER_DISTANCE = 0.001f;
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
		float rightSideMultiplied1 = Math.min(1f, collision.getT()) * root1Denominator;
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
		float rightSideMultiplied2 = Math.min(1f, collision.getT()) * root2Denominator;
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
		float rightSideMultiplied1 = Math.min(1f, collision.getT()) * root1Denominator;
		if (root1Numerator >= 0f && root1Numerator <= rightSideMultiplied1) {
			float t = MathUtil.clamp(root1Numerator / root1Denominator, 0f, 1f);
			collision.set(t, vertex);
		}
		float root2Numerator = 2f * c;
		float root2Denominator = temp;
		float rightSideMultiplied2 = Math.min(1f, collision.getT()) * root2Denominator;
		if (root2Numerator >= 0f && root2Numerator <= rightSideMultiplied2) {
			float t = MathUtil.clamp(root2Numerator / root2Denominator, 0f, 1f);
			collision.set(t, vertex);
		}
	}

	// response steps
	public static void collideAndSlide(MutableVector3D position, MutableVector3D velocity) {
		collideAndSlide(position, velocity, MAX_RECURSION_DEPTH);
	}

	public static void collideAndSlide(MutableVector3D position, MutableVector3D velocity, int maxRecursionDepth) {
		// Vector3D eRadius = new Vector3D(1f, 1f, 1f); // ellipsoid radius

		// calculate position and velocity in eSpace
		// position.divide(eRadius);
		// velocity.divide(eRadius);

		// Iterate until we have our final position
		collideWithWorld(position, velocity, 0, velocity.toImmutable(), maxRecursionDepth);

		// Convert final result back to r3
		// position.multiply(eRadius);
		// velocity.multiply(eRadius);
	}

	public static void collideWithWorld(MutableVector3D mutablePosition, MutableVector3D mutableVelocity, int collisionRecursionDepth, Vector3D remainingVelocity, int maxRecursionDepth) {
		if (collisionRecursionDepth > maxRecursionDepth) {
			return;
		}
		if (remainingVelocity.lengthSquared() < BUFFER_DISTANCE * BUFFER_DISTANCE) {
			return;
		}
		var position = mutablePosition.toImmutable();
		var velocity = mutableVelocity.toImmutable();
		Collision collision = checkCollision(position, remainingVelocity);
		if (collision.getT() >= 1f) {
			float length = remainingVelocity.length() - BUFFER_DISTANCE;
			if (length > 0) {
				mutablePosition.add(remainingVelocity.scaleToLength(length));
			}
			return;
		}
		var usedVelocity = remainingVelocity.multiply(collision.getT());
		var collisionPosition = position.add(usedVelocity);
		var negSlidePlaneNormal = collision.getIntersection().subtract(collisionPosition).normalize();
		remainingVelocity = remainingVelocity.subtract(usedVelocity);
		remainingVelocity = remainingVelocity.subtract(negSlidePlaneNormal.multiply(negSlidePlaneNormal.dotProduct(remainingVelocity)));
		mutableVelocity.subtract(negSlidePlaneNormal.multiply(negSlidePlaneNormal.dotProduct(velocity)));
		/*if (collisionRecursionDepth == 0) {
			mutableVelocity.multiply(0.995f);
			remainingVelocity = remainingVelocity.multiply(0.995f);
		}*/
		float length = usedVelocity.length() - BUFFER_DISTANCE;
		if (length > 0) {
			mutablePosition.add(usedVelocity.scaleToLength(length));
		}
		collideWithWorld(mutablePosition, mutableVelocity, collisionRecursionDepth + 1, remainingVelocity, maxRecursionDepth);
	}

	// Super temporary stuff below
	public static final List<Triangle> triangles = new ArrayList<>();
	public static final List<BiFunction<Vector3D, Vector3D, Consumer<Collision>>> consumers = new ArrayList<>();

	public static Collision checkCollision(Vector3D position, Vector3D velocity) {
		Collision collision = new Collision(Float.MAX_VALUE, Vector3D.ZERO);
		// transform packet.basePoint and packet.velocity from eSpace to r3 space?
		for (BiFunction<Vector3D, Vector3D, Consumer<Collision>> consumer : consumers) {
			consumer.apply(position, velocity).accept(collision);
		}
		for (Triangle triangle : triangles) {
			CollisionPacket.checkTriangle(position, velocity, triangle, collision);
		}
		return collision;
	}


	// response steps
	public static boolean collideAndSlideIntersect(MutableVector3D position, MutableVector3D velocity) {
		return collideWithWorldIntersect(position, velocity.toImmutable());
	}

	public static boolean collideWithWorldIntersect(MutableVector3D mutablePosition, Vector3D remainingVelocity) {
		Collision collision = checkCollision(mutablePosition.toImmutable(), remainingVelocity);
		if (collision.getT() >= 1f) {
			float length = remainingVelocity.length() - BUFFER_DISTANCE;
			if (length > 0) {
				mutablePosition.add(remainingVelocity.scaleToLength(length));
			}
			return false;
		}
		var usedVelocity = remainingVelocity.multiply(collision.getT());
		float length = usedVelocity.length() - BUFFER_DISTANCE;
		if (length > 0) {
			mutablePosition.add(usedVelocity.scaleToLength(length));
		}
		return true;
	}

}
