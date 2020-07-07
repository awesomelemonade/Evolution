package lemon.evolution.physicsbeta;

import lemon.engine.math.*;
import lemon.evolution.Game;
import lemon.evolution.pool.VectorPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CollisionPacket {
	private static final float BUFFER_DISTANCE = 0.001f;
	private static final int MAX_RECURSION_DEPTH = 5;

	public static void checkTriangle(Vector3D position, Vector3D velocity, Triangle triangle, Collision collision) {
		// isFrontFacingTo
		try (var normalizedVelocity = VectorPool.of(velocity, Vector::normalize)) {
			if (triangle.getNormal().dotProduct(normalizedVelocity) <= 0f) {
				// trianglePlane.getSignedDistanceTo(position)
				float signedDistanceToTrianglePlane =
						position.dotProduct(triangle.getNormal()) - triangle.getNormal().dotProduct(triangle.getVertex1());

				// cache this as we're going to use it a few times below
				float normalDotVelocity = triangle.getNormal().dotProduct(velocity);

				// if sphere is travelling parallel to the plane
				if (Math.abs(normalDotVelocity) <= 0.00001f) {
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
					//float t0 = Math.max(0f, Math.min((-1.0f - signedDistanceToTrianglePlane) / normalDotVelocity,
					//		(1.0f - signedDistanceToTrianglePlane) / normalDotVelocity));
					float a = (-1.0f - signedDistanceToTrianglePlane) / normalDotVelocity;
					float b = (1.0f - signedDistanceToTrianglePlane) / normalDotVelocity;
					if (a > b) {
						// swap
						float temp = a;
						a = b;
						b = temp;
					}
					if (a > 1.0f || b < 0f) {
						return;
					}
					a = MathUtil.clamp(a, 0f, 1f);
					/*if (t0 > 1.0) {
						// [t0, t1] is outside of [0, 1]
						// No collisions possible
						return;
					}*/
					float finalA = a;
					try (var scaledVelocity = VectorPool.of(velocity, v -> v.multiply(finalA));
						 var planeIntersectionPoint = VectorPool.of(position,
								 v -> v.subtract(triangle.getNormal()).add(scaledVelocity))) {
						if (triangle.isInside(planeIntersectionPoint)) {
							collision.test(a, planeIntersectionPoint, triangle, "Face");
							return;
						}
					}
				}
				float velocitySquaredLength = velocity.getAbsoluteValueSquared();

				// Check vertices
				checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex1(), collision, triangle);
				checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex2(), collision, triangle);
				checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex3(), collision, triangle);

				// Check against edges
				checkEdge(velocitySquaredLength, velocity, position, triangle.getVertex1(), triangle.getVertex2(), collision, triangle);
				checkEdge(velocitySquaredLength, velocity, position, triangle.getVertex2(), triangle.getVertex3(), collision, triangle);
				checkEdge(velocitySquaredLength, velocity, position, triangle.getVertex3(), triangle.getVertex1(), collision, triangle);
			}
		}
	}
	private static void checkEdge(float velocitySquaredLength, Vector3D velocity, Vector3D base, Vector3D vertexA, Vector3D vertexB, Collision collision, Triangle triangle) {
		try (var edge = VectorPool.of(vertexB, v -> v.subtract(vertexA));
			 var baseToVertex = VectorPool.of(vertexA, v -> v.subtract(base))) {
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
					try (var intersection = VectorPool.of(vertexA, v -> v.add(edge.multiply(f)))) {
						collision.set(t, intersection, triangle, "Edge");
					}
				}
			}
		}
	}
	private static void checkVertex(float velocitySquaredLength, Vector3D velocity, Vector3D base, Vector3D vertex, Collision collision, Triangle triangle) {
		try (var temp = VectorPool.of(base, v -> v.subtract(vertex))) {
			float b = 2.0f * velocity.dotProduct(temp);
			float c = vertex.getDistanceSquared(base) - 1.0f;
			float t = getLowestRoot(velocitySquaredLength, b, c);
			collision.test(t, vertex, triangle, "Vertex");
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
	public static void collideAndSlide(Vector3D position, Vector3D velocity) {
		// Vector3D eRadius = new Vector3D(1f, 1f, 1f); // ellipsoid radius

		// calculate position and velocity in eSpace
		// position.divide(eRadius);
		// velocity.divide(eRadius);

		// Iterate until we have our final position
		try (var remainingVelocity = VectorPool.of(velocity)) {
			collideWithWorld(position, velocity, 0, remainingVelocity, new StringBuilder());
		}

		// Convert final result back to r3
		// position.multiply(eRadius);
		// velocity.multiply(eRadius);
	}
	public static void collideWithWorld(Vector3D position, Vector3D velocity, int collisionRecursionDepth, Vector3D remainingVelocity, StringBuilder builder) {
		if (collisionRecursionDepth > MAX_RECURSION_DEPTH) {
			System.out.println(builder.toString());
			return;
		}
		Collision collision = checkCollision(position, remainingVelocity);
		String info = String.format("depth=%d, pos=%s, vel=%s, rem=%s, t=%f, intersection=%s, tri=%s\n",
				collisionRecursionDepth, position, velocity, remainingVelocity, collision.getT(), collision.getIntersection(), collision.getTriangle());
		builder.append(info);
		if (collision.getT() < 1f) {
			String info2 = String.format("triNormal=%s, dot=%f, dist=%f, reason=%s\n",
					collision.getTriangle().getNormal(), remainingVelocity.dotProduct(collision.getTriangle().getNormal()), collision.getIntersection().getDistance(position), collision.reason);
			builder.append(info2);
		}
		if (collision.getTriangle() != null) {
			Game.INSTANCE.debug.add(collision.getTriangle().getVertex1());
			Game.INSTANCE.debug.add(collision.getTriangle().getVertex2());
			Game.INSTANCE.debug.add(collision.getTriangle().getVertex3());
			Game.INSTANCE.debug.add(collision.getIntersection().copy());
		}
		if (collision.getT() >= 1f) {
			position.add(remainingVelocity);
			return;
		}
		try (var usedVelocity = VectorPool.of(remainingVelocity, x -> x.multiply(collision.getT()));
			 var collisionPosition = VectorPool.of(position, x -> x.add(usedVelocity));
			 var negSlidePlaneNormal = VectorPool.of(collision.getIntersection(), x -> x.subtract(collisionPosition).normalize())) {
			remainingVelocity.subtract(usedVelocity);
			try (var scaled = VectorPool.of(negSlidePlaneNormal, x -> x.multiply(x.dotProduct(remainingVelocity)))) {
				remainingVelocity.subtract(scaled);
			}
			try (var scaled = VectorPool.of(negSlidePlaneNormal, x -> x.multiply(x.dotProduct(velocity)))) {
				velocity.subtract(scaled);
			}
			position.add(usedVelocity);
			// TODO: Incorporate friction & elasticity
			if (remainingVelocity.getLengthSquared() < BUFFER_DISTANCE * BUFFER_DISTANCE) {
				return;
			}
			collideWithWorld(position, velocity, collisionRecursionDepth + 1, remainingVelocity, builder);
		}
	}
	// Super temporary stuff below
	public static final List<Triangle> triangles = new ArrayList<>();
	public static final List<BiFunction<Vector3D, Vector3D, Consumer<Collision>>> consumers = new ArrayList<>();
	public static Collision checkCollision(Vector3D position, Vector3D velocity) {
		Collision collision = new Collision(Float.MAX_VALUE, new Vector3D());
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
