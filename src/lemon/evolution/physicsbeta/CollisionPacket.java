package lemon.evolution.physicsbeta;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Plane;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class CollisionPacket {
	private Vector3D eRadius; // ellipsoid radius
	private Vector3D r3Velocity;
	private Vector3D r3Position;
	private Vector3D velocity;
	private Vector3D normalizedVelocity;
	private Vector3D basePoint;

	private boolean foundCollision;
	private float nearestDistance;
	private Vector3D intersectionPoint;

	public static void checkTriangle(CollisionPacket packet, Triangle triangle) {
		Plane trianglePlane = new Plane(triangle);
		if (trianglePlane.isFrontFacingTo(packet.normalizedVelocity)) {
			float t0, t1;
			boolean embeddedInPlane = false;

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
					embeddedInPlane = true;
					t0 = 0.0f;
					t1 = 1.0f;
				}
			} else {
				// Calculate intersection interval
				t0 = (-1.0f - signedDistanceToTrianglePlane) / normalDotVelocity;
				t1 = ( 1.0f - signedDistanceToTrianglePlane) / normalDotVelocity;

				// Swap so t0 < t1
				if (t0 > t1) {
					float temp = t1;
					t1 = t0;
					t0 = temp;
				}

				// Check that at least one result is within range
				if (t0 > 1.0 || t1 < 0) {
					// [t0, t1] is outside of [0, 1]
					// No collisions possible
					return;
				}

				// Clamp to [0, 1]
				t0 = MathUtil.clamp(t0, 0f, 1f);
				t1 = MathUtil.clamp(t1, 0f, 1f);
			}

			Info info = new Info();

			if (!embeddedInPlane) {
				Vector3D planeIntersectionPoint = packet.basePoint.subtract(trianglePlane.getNormal())
						.add(packet.velocity.multiply(t0));
				if (triangle.isInside(planeIntersectionPoint)) {
					info.foundCollision = true;
					info.t = t0;
					info.collisionPoint = planeIntersectionPoint;
				}
			}
			if (!info.foundCollision) {
				Vector3D velocity = packet.velocity;
				Vector3D base = packet.basePoint;
				float velocitySquaredLength = velocity.getAbsoluteValueSquared();

				checkVertex(velocitySquaredLength, velocity, base, triangle.getVertex1(), info);
				checkVertex(velocitySquaredLength, velocity, base, triangle.getVertex2(), info);
				checkVertex(velocitySquaredLength, velocity, base, triangle.getVertex3(), info);

				// Check against edges
				checkEdge(velocitySquaredLength, velocity, base, triangle.getVertex1(), triangle.getVertex2(), info);
				checkEdge(velocitySquaredLength, velocity, base, triangle.getVertex2(), triangle.getVertex3(), info);
				checkEdge(velocitySquaredLength, velocity, base, triangle.getVertex3(), triangle.getVertex1(), info);
			}

			if (info.foundCollision) {
				float distToCollision = info.t * packet.velocity.getAbsoluteValue();
				if ((!packet.foundCollision) || distToCollision < packet.nearestDistance) {
					packet.nearestDistance = distToCollision;
					packet.intersectionPoint = info.collisionPoint;
					packet.foundCollision = true;
				}
			}
		}
	}
	private static void checkEdge(float velocitySquaredLength, Vector3D velocity, Vector3D base, Vector3D vertexA, Vector3D vertexB, Info info) {
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

		MathUtil.getLowestRoot(a, b, c, info.t).ifPresent(root -> {
			float f = (edgeDotVelocity * root - edgeDotBaseToVertex) / edgeSquaredLength;
			if (f >= 0.0f && f <= 1.0f) {
				info.t = root;
				info.foundCollision = true;
				info.collisionPoint = vertexA.add(edge.multiply(f));
			}
		});
	}
	private static void checkVertex(float velocitySquaredLength, Vector3D velocity, Vector3D base, Vector3D vertex, Info info) {
		// p1
		float b = 2.0f * velocity.dotProduct(base.subtract(vertex));
		float c = vertex.subtract(base).getAbsoluteValueSquared() - 1.0f;
		MathUtil.getLowestRoot(velocitySquaredLength, b, c, info.t).ifPresent(root -> {
			info.t = root;
			info.foundCollision = true;
			info.collisionPoint = vertex;
		});
	}
	private static class Info {
		Vector3D collisionPoint;
		boolean foundCollision = false;
		float t = 1.0f;
	}
	// response steps
	public static void collideAndSlide(Vector3D position, Vector3D velocity, Vector3D gravity) {
		// Do collision detection
		CollisionPacket packet = new CollisionPacket();
		packet.r3Position = position;
		packet.r3Velocity = velocity;
		packet.eRadius = new Vector3D(1f, 1f, 1f);

		// calculate position and velocity in eSpace
		Vector3D eSpacePosition = packet.r3Position.divide(packet.eRadius);
		Vector3D eSpaceVelocity = packet.r3Velocity.divide(packet.eRadius);

		// Iterate until we have our final position
		Vector3D finalPosition = collideWithWorld(eSpacePosition, eSpaceVelocity, packet, 0);

		// Add gravity pull
		// to remove gravity uncomment from here
		// Set the new r3 position (convert back from eSpace to r3)
		/*packet.r3Position = finalPosition.multiply(packet.eRadius);
		packet.r3Velocity = gravity;

		eSpaceVelocity = gravity.divide(packet.eRadius);

		finalPosition = collideWithWorld(finalPosition, eSpaceVelocity, packet, 0);*/
		// ... to here

		// Convert final result back to r3
		finalPosition = finalPosition.multiply(packet.eRadius);

		// Move the entity (application specific function)
		position.set(finalPosition);
	}
	public static Vector3D collideWithWorld(Vector3D position, Vector3D velocity, CollisionPacket packet, int collisionRecursionDepth) {
		final float unitsPerMeter = 100.0f; // Set this to match application scale
		float unitScale = unitsPerMeter / 100.0f;
		float veryCloseDistance = 0.005f * unitScale;

		// do we need to worry?
		if (collisionRecursionDepth > 5) {
			return position;
		}

		// Ok, we need to worry:
		packet.velocity = velocity;
		packet.normalizedVelocity = velocity.normalize();
		packet.basePoint = position;
		packet.foundCollision = false;

		// Check for collision (calls the collision routines)
		// Application specific
		checkCollision(packet);

		// if no collision we just move along the velocity
		if (!packet.foundCollision) {
			return position.add(velocity);
		}

		// Collision occurred

		Vector3D destinationPoint = position.add(velocity);
		Vector3D newBasePoint = position;

		if (packet.nearestDistance >= veryCloseDistance) {
			Vector3D v = velocity.scaleToLength(packet.nearestDistance - veryCloseDistance);
			newBasePoint = packet.basePoint.add(v);

			v.normalize();
			packet.intersectionPoint.selfSubtract(v.multiply(veryCloseDistance));
		}

		// Determine the sliding plane
		Vector3D slidePlaneOrigin = packet.intersectionPoint;
		Vector3D slidePlaneNormal = newBasePoint.subtract(packet.intersectionPoint);
		slidePlaneNormal.normalize();
		Plane slidingPlane = new Plane(slidePlaneOrigin, slidePlaneNormal);

		Vector3D newDestinationPoint = destinationPoint.subtract(slidePlaneNormal
				.multiply(slidingPlane.getSignedDistanceTo(destinationPoint)));

		// Generate the slide vector, which will become our new velocity vector for the next iteration
		Vector3D newVelocityVector = newDestinationPoint.subtract(packet.intersectionPoint);

		// Don't recurse if the new velocity is very small
		if (newVelocityVector.getLengthSquared() < veryCloseDistance * veryCloseDistance) {
			return newBasePoint;
		}

		// Recurse
		return collideWithWorld(newBasePoint, newVelocityVector, packet, collisionRecursionDepth + 1);
	}
	// Super temporary stuff below
	public static final List<Triangle> triangles = new ArrayList<Triangle>();
	public static void checkCollision(CollisionPacket packet) {
		for (Triangle triangle : triangles) {
			CollisionPacket.checkTriangle(packet, triangle);
		}
	}
}
