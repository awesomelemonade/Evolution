package lemon.evolution.physicsbeta;

import lemon.engine.math.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CollisionPacket {
	private static final float BUFFER_DISTANCE = 0.001f;
	private static final int MAX_RECURSION_DEPTH = 5;

	public static void checkTriangle(Vector3D position, Vector3D velocity, Triangle triangle, Collision collision) {
		Plane trianglePlane = new Plane(triangle);
		if (trianglePlane.isFrontFacingTo(velocity.copy().normalize())) {
			float signedDistanceToTrianglePlane = trianglePlane.getSignedDistanceTo(position);

			// cache this as we're going to use it a few times below
			float normalDotVelocity = trianglePlane.getNormal().dotProduct(velocity);

			// if sphere is travelling parallel to the plane
			if (normalDotVelocity == 0f) {
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
				// Calculate intersection interval
				float t0 = Math.min((-1.0f - signedDistanceToTrianglePlane) / normalDotVelocity,
						( 1.0f - signedDistanceToTrianglePlane) / normalDotVelocity);

				if (t0 > 1.0) {
					// [t0, t1] is outside of [0, 1]
					// No collisions possible
					return;
				}
				if (t0 < 0) {
					t0 = 0f;
				}
				// TODO: Convert to stream-like to prevent unnecessary allocation
				Vector3D planeIntersectionPoint = position.copy().subtract(trianglePlane.getNormal())
						.add(velocity.copy().multiply(t0));
				if (triangle.isInside(planeIntersectionPoint)) {
					collision.test(t0, planeIntersectionPoint);
					return;
				}
			}
			float velocitySquaredLength = velocity.getAbsoluteValueSquared();

			// Check vertices
			checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex1(), collision);
			checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex2(), collision);
			checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex3(), collision);

			// Check against edges
			checkEdge(velocitySquaredLength, velocity, position, triangle.getVertex1(), triangle.getVertex2(), collision);
			checkEdge(velocitySquaredLength, velocity, position, triangle.getVertex2(), triangle.getVertex3(), collision);
			checkEdge(velocitySquaredLength, velocity, position, triangle.getVertex3(), triangle.getVertex1(), collision);
		}
	}
	private static void checkEdge(float velocitySquaredLength, Vector3D velocity, Vector3D base, Vector3D vertexA, Vector3D vertexB, Collision collision) {
		Vector3D edge = vertexB.copy().subtract(vertexA);
		Vector3D baseToVertex = vertexA.copy().subtract(base);
		float edgeSquaredLength = edge.getAbsoluteValueSquared();
		float edgeDotVelocity = edge.dotProduct(velocity);
		float edgeDotBaseToVertex = edge.dotProduct(baseToVertex);

		float a = edgeSquaredLength * (-velocitySquaredLength) +
				edgeDotVelocity * edgeDotVelocity;
		float b = edgeSquaredLength * (2.0f * velocity.dotProduct(baseToVertex)) -
				2.0f * edgeDotVelocity * edgeDotBaseToVertex;
		float c = edgeSquaredLength * (1.0f - baseToVertex.getAbsoluteValueSquared()) +
				edgeDotBaseToVertex * edgeDotBaseToVertex;

		float t = getLowestRoot(a, b, c);
		if (t < collision.getT()) {
			float f = (edgeDotVelocity * t - edgeDotBaseToVertex) / edgeSquaredLength;
			if (f >= 0.0f && f <= 1.0f) {
				collision.setT(t);
				collision.setIntersection(vertexA.copy().add(edge.copy().multiply(f)));
			}
		}
	}
	private static void checkVertex(float velocitySquaredLength, Vector3D velocity, Vector3D base, Vector3D vertex, Collision collision) {
		// p1
		float b = 2.0f * velocity.dotProduct(base.copy().subtract(vertex));
		float c = vertex.getDistanceSquared(base) - 1.0f;
		float t = getLowestRoot(velocitySquaredLength, b, c);
		collision.test(t, vertex);
	}
	public static float getLowestRoot(float a, float b, float c) {
		float determinant = b * b - 4.0f * a * c;

		if (determinant < 0.0f) {
			return Float.MAX_VALUE;
		}

		float sqrtD = (float) Math.sqrt(determinant);
		float root1 = (-b - sqrtD) / (2 * a);
		float root2 = (-b + sqrtD) / (2 * a);

		// Swap so root1 <= root2
		if (root1 > root2) {
			float temp = root2;
			root2 = root1;
			root1 = temp;
		}

		if (root1 > 0) {
			return root1;
		}
		if (root2 > 0) {
			return root2;
		}
		return Float.MAX_VALUE;
	}
	// response steps
	public static void collideAndSlide(Vector3D position, Vector3D velocity) {
		Vector3D eRadius = new Vector3D(1f, 1f, 1f); // ellipsoid radius

		// calculate position and velocity in eSpace
		position.divide(eRadius);
		velocity.divide(eRadius);

		// Iterate until we have our final position
		collideWithWorld(position, velocity, 0, velocity);

		// Convert final result back to r3
		position.multiply(eRadius);
		velocity.multiply(eRadius);
	}
	public static void collideWithWorld(Vector3D position, Vector3D velocity, int collisionRecursionDepth, Vector3D remainingVelocity) {
		// Exceed recursion depth
		if (collisionRecursionDepth > MAX_RECURSION_DEPTH) {
			// Remaining velocity should be removed from velocity
			velocity.subtract(remainingVelocity.copy().multiply(remainingVelocity.dotProduct(velocity) / remainingVelocity.getLength()));
			return;
		}

		// Check for collision (calls the collision routines)
		// Application specific
		Collision collision = checkCollision(position, remainingVelocity);

		// if no collision we just move along the velocity
		if (collision.getT() > 1) {
			position.add(remainingVelocity);
			return;
		}

		// Collision occurred

		Vector3D destinationPoint = position.copy().add(remainingVelocity);
		float nearestDistance = remainingVelocity.getAbsoluteValue() * collision.getT();

		if (nearestDistance > BUFFER_DISTANCE) {
			Vector3D v = remainingVelocity.copy().scaleToLength(nearestDistance - BUFFER_DISTANCE);
			position.add(v);
			collision.getIntersection().subtract(v.scaleToLength(BUFFER_DISTANCE));
		} else {
			float dist = BUFFER_DISTANCE - nearestDistance;
			Vector3D v = remainingVelocity.copy().scaleToLength(dist);
			position.subtract(v);
		}

		// Determine the sliding plane
		Vector3D slidePlaneOrigin = position;
		Vector3D slidePlaneNormal = position.copy().subtract(collision.getIntersection()).normalize();
		Plane slidingPlane = new Plane(slidePlaneOrigin, slidePlaneNormal);

		Vector3D newDestinationPoint = destinationPoint.copy().subtract(slidePlaneNormal
				.copy().multiply(slidingPlane.getSignedDistanceTo(destinationPoint)));

		// Generate the slide vector, which will become our new velocity vector for the next iteration
		Vector3D newRemainingVelocity = newDestinationPoint.copy().subtract(position);
		velocity.subtract(slidePlaneNormal.copy().multiply(velocity.dotProduct(slidePlaneNormal)));

		// Don't recurse if the remaining velocity is very small
		if (newRemainingVelocity.getLengthSquared() < BUFFER_DISTANCE * BUFFER_DISTANCE) {
			return;
		}

		// Recurse
		collideWithWorld(position, velocity, collisionRecursionDepth + 1, newRemainingVelocity);
	}
	// Super temporary stuff below
	public static final List<Triangle> triangles = new ArrayList<Triangle>();
	public static final List<BiFunction<Vector3D, Vector3D, Consumer<Collision>>> consumers = new ArrayList<>();
	public static Collision checkCollision(Vector3D position, Vector3D velocity) {
		Collision collision = new Collision(Float.MAX_VALUE, null);
		// transform packet.basePoint and packet.velocity from eSpace to r3 space?
		for (BiFunction<Vector3D, Vector3D, Consumer<Collision>> consumer : consumers) {
			consumer.apply(position, velocity).accept(collision);
		}
		for (Triangle triangle : triangles) {
			CollisionPacket.checkTriangle(position, velocity, triangle, collision);
		}
		return collision;
	}
}
