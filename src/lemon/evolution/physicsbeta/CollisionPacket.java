package lemon.evolution.physicsbeta;

import lemon.engine.math.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CollisionPacket {
	public Vector3D velocity; // temp public
	private Vector3D normalizedVelocity;
	public Vector3D basePoint; // temp public

	private float t = Float.MAX_VALUE;
	private Vector3D intersectionPoint;

	public static void checkTriangle(CollisionPacket packet, Triangle triangle) {
		Plane trianglePlane = new Plane(triangle);
		if (trianglePlane.isFrontFacingTo(packet.normalizedVelocity)) {
			float signedDistanceToTrianglePlane = trianglePlane.getSignedDistanceTo(packet.basePoint);

			// cache this as we're going to use it a few times below
			float normalDotVelocity = trianglePlane.getNormal().dotProduct(packet.velocity);

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
				Vector3D planeIntersectionPoint = packet.basePoint.subtract(trianglePlane.getNormal())
						.add(packet.velocity.multiply(t0));
				if (triangle.isInside(planeIntersectionPoint)) {
					if (t0 < packet.t) {
						packet.t = t0;
						packet.intersectionPoint = planeIntersectionPoint;
					}
					return;
				}
			}
			Vector3D velocity = packet.velocity;
			Vector3D base = packet.basePoint;
			float velocitySquaredLength = velocity.getAbsoluteValueSquared();

			// Check vertices
			checkVertex(velocitySquaredLength, velocity, base, triangle.getVertex1(), packet);
			checkVertex(velocitySquaredLength, velocity, base, triangle.getVertex2(), packet);
			checkVertex(velocitySquaredLength, velocity, base, triangle.getVertex3(), packet);

			// Check against edges
			checkEdge(velocitySquaredLength, velocity, base, triangle.getVertex1(), triangle.getVertex2(), packet);
			checkEdge(velocitySquaredLength, velocity, base, triangle.getVertex2(), triangle.getVertex3(), packet);
			checkEdge(velocitySquaredLength, velocity, base, triangle.getVertex3(), triangle.getVertex1(), packet);
		}
	}
	private static void checkEdge(float velocitySquaredLength, Vector3D velocity, Vector3D base, Vector3D vertexA, Vector3D vertexB, CollisionPacket packet) {
		Vector3D edge = vertexB.subtract(vertexA);
		Vector3D baseToVertex = vertexA.subtract(base);
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
		if (t < packet.t) {
			float f = (edgeDotVelocity * t - edgeDotBaseToVertex) / edgeSquaredLength;
			if (f >= 0.0f && f <= 1.0f) {
				packet.t = t;
				packet.intersectionPoint = vertexA.add(edge.multiply(f));
			}
		}
	}
	private static void checkVertex(float velocitySquaredLength, Vector3D velocity, Vector3D base, Vector3D vertex, CollisionPacket packet) {
		// p1
		float b = 2.0f * velocity.dotProduct(base.subtract(vertex));
		float c = vertex.subtract(base).getAbsoluteValueSquared() - 1.0f;
		float t = getLowestRoot(velocitySquaredLength, b, c);
		if (t < packet.t) {
			packet.t = t;
			packet.intersectionPoint = vertex;
		}
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
	public static void collideAndSlide(Vector3D position, Vector3D velocity, Vector3D gravity) {
		// Do collision detection
		CollisionPacket packet = new CollisionPacket();
		Vector3D eRadius = new Vector3D(1f, 1f, 1f); // ellipsoid radius

		// calculate position and velocity in eSpace
		Vector3D eSpacePosition = position.divide(eRadius);
		Vector3D eSpaceVelocity = velocity.divide(eRadius);

		// Iterate until we have our final position
		VectorArray array = collideWithWorld(eSpacePosition, eSpaceVelocity, packet, 0, eSpaceVelocity);

		Vector3D finalPosition = array.get(0);
		Vector3D finalVelocity = array.get(1);

		// Convert final result back to r3
		finalPosition = finalPosition.multiply(eRadius);
		eSpaceVelocity = finalVelocity.multiply(eRadius);

		// Move the entity (application specific function)
		position.set(finalPosition);
		velocity.set(eSpaceVelocity);
	}
	public static VectorArray collideWithWorld(Vector3D position, Vector3D velocity, CollisionPacket packet, int collisionRecursionDepth, Vector3D remainingVelocity) {
		final float unitsPerMeter = 1.0f; // Set this to match application scale
		float unitScale = unitsPerMeter / 100.0f;
		float veryCloseDistance = 0.005f * unitScale;
		veryCloseDistance = 0.001f;

		// do we need to worry?
		if (collisionRecursionDepth > 5) {
			return new VectorArray(position, velocity);
		}

		// Ok, we need to worry:
		packet.velocity = remainingVelocity;
		packet.normalizedVelocity = remainingVelocity.normalize();
		packet.basePoint = position;
		packet.t = Float.MAX_VALUE;

		// Check for collision (calls the collision routines)
		// Application specific
		checkCollision(packet);

		// if no collision we just move along the velocity
		if (packet.t > 1) {
			return new VectorArray(position.add(remainingVelocity), velocity);
		}

		// Collision occurred

		Vector3D destinationPoint = position.add(remainingVelocity);
		Vector3D newBasePoint = position;
		float nearestDistance = packet.velocity.getAbsoluteValue() * packet.t;

		if (nearestDistance >= veryCloseDistance) {
			Vector3D v = remainingVelocity.scaleToLength(nearestDistance - veryCloseDistance);
			newBasePoint = packet.basePoint.add(v);

			v.normalize();
			packet.intersectionPoint.selfSubtract(v.multiply(veryCloseDistance));
		}

		// Determine the sliding plane
		Vector3D slidePlaneOrigin = packet.intersectionPoint;
		Vector3D slidePlaneNormal = newBasePoint.subtract(packet.intersectionPoint).normalize();
		Plane slidingPlane = new Plane(slidePlaneOrigin, slidePlaneNormal);

		Vector3D newDestinationPoint = destinationPoint.subtract(slidePlaneNormal
				.multiply(slidingPlane.getSignedDistanceTo(destinationPoint)));

		// Generate the slide vector, which will become our new velocity vector for the next iteration
		Vector3D newRemainingVelocity = newDestinationPoint.subtract(packet.intersectionPoint);

		Vector3D temp = destinationPoint.subtract(newDestinationPoint).normalize();
		Vector3D newVelocity = velocity.subtract(temp.multiply(velocity.dotProduct(temp)));

		// Don't recurse if the remaining velocity is very small
		if (newRemainingVelocity.getLengthSquared() < veryCloseDistance * veryCloseDistance) {
			return new VectorArray(newBasePoint, newVelocity);
		}

		// Recurse
		return collideWithWorld(newBasePoint, newVelocity, packet, collisionRecursionDepth + 1, newRemainingVelocity);
	}
	// Super temporary stuff below
	public static final List<Triangle> triangles = new ArrayList<Triangle>();
	public static final List<Consumer<CollisionPacket>> consumers = new ArrayList<Consumer<CollisionPacket>>();
	public static void checkCollision(CollisionPacket packet) {
		// transform packet.basePoint and packet.velocity from eSpace to r3 space?
		for (Consumer<CollisionPacket> consumer : consumers) {
			consumer.accept(packet);
		}
		for (Triangle triangle : triangles) {
			CollisionPacket.checkTriangle(packet, triangle);
		}
	}
}
