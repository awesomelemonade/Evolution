package lemon.evolution.physicsbeta;

import lemon.engine.math.*;
import lemon.evolution.pool.VectorPool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CollisionPacket {
	private static final float BUFFER_DISTANCE = 0.001f;
	private static final int MAX_RECURSION_DEPTH = 5;

	public static void checkTriangle(Vector3D position, Vector3D velocity, Triangle triangle, Collision collision) {
		float minDistanceSquared = Math.min(position.getDistanceSquared(triangle.getVertex1()),
				Math.min(position.getDistanceSquared(triangle.getVertex2()), position.getDistanceSquared(triangle.getVertex3())));
		float bufferedVelocityLength = velocity.getLength() + 5f; // TODO: temp
		if (minDistanceSquared > bufferedVelocityLength * bufferedVelocityLength) {
			return;
		}
		// isFrontFacingTo
		try (var normalizedVelocity = VectorPool.of(velocity, Vector::normalize)) {
			if (triangle.getNormal().dotProduct(normalizedVelocity) <= 0f) {
				float normalDotVelocity = triangle.getNormal().dotProduct(velocity);
				// if sphere is travelling parallel to the plane
				if (Math.abs(normalDotVelocity) <= 0.00001f) {
					float signedDistanceToTrianglePlane =
							position.dotProduct(triangle.getNormal()) - triangle.getNormal()
									.dotProduct(triangle.getVertex1());
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
					try (var temp = VectorPool.of(position, x -> x.subtract(triangle.getVertex1()).subtract(triangle.getNormal()))) {
						float x = triangle.getNormal().dotProduct(temp);
						if (x >= -0.001f && x <= -normalDotVelocity) {
							float t = Math.max(0f, Math.min(1f, -x / normalDotVelocity));
							try (var scaledVelocity = VectorPool.of(velocity, v -> v.multiply(t));
								 var planeIntersectionPoint = VectorPool.of(position,
										 v -> v.subtract(triangle.getNormal()).add(scaledVelocity))) {
								if (triangle.isInside(planeIntersectionPoint)) {
									collision.test(t, planeIntersectionPoint);
									return;
								}
							}
						}
					}
				}
				float velocitySquaredLength = velocity.getAbsoluteValueSquared();

				// Check vertices
				checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex1(), collision, triangle);
				checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex2(), collision, triangle);
				checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex3(), collision, triangle);

				// Check against edges
				checkEdge(position, velocity, triangle.getVertex1(), triangle.getVertex2(), collision, triangle);
				checkEdge(position, velocity, triangle.getVertex2(), triangle.getVertex3(), collision, triangle);
				checkEdge(position, velocity, triangle.getVertex3(), triangle.getVertex1(), collision, triangle);
			}
		}
	}
	private static void checkEdge(Vector3D position, Vector3D velocity, Vector3D vertexA, Vector3D vertexB, Collision collision, Triangle triangle) {
		// https://mrl.nyu.edu/~dzorin/rend05/lecture2.pdf
		try (var deltaP = VectorPool.of(position, x -> x.subtract(vertexA));
			 var edge = VectorPool.of(vertexB, x -> x.subtract(vertexA).normalize());
			 var scaledEdgeA = VectorPool.of(edge, x -> x.multiply(x.dotProduct(velocity)));
			 var scaledEdgeC = VectorPool.of(edge, x -> x.multiply(x.dotProduct(deltaP)));
			 var tempA = VectorPool.of(velocity, x -> x.subtract(scaledEdgeA));
			 var tempC = VectorPool.of(deltaP, x -> x.subtract(scaledEdgeC))) {
			float a = tempA.getLengthSquared();
			float b = 2f * tempA.dotProduct(tempC);
			float c = tempC.getLengthSquared() - 1f;
			float det = b * b - 4f * a * c;
			if (det < 0f) {
				return;
			}
			float sqrtDet = (float) Math.sqrt(det);
			float root1 = (-b - sqrtDet) / (2f * a);
			float root2 = (-b + sqrtDet) / (2f * a);
			if (root1 >= 0f && root1 <= 1f && root1 <= collision.getT()) {
				try (var scaledVelocity = VectorPool.of(velocity, x -> x.multiply(root1));
					 var q = VectorPool.of(position, x -> x.add(scaledVelocity));
					 var q_vertexA = VectorPool.of(q, x -> x.subtract(vertexA));
					 var q_vertexB = VectorPool.of(q, x -> x.subtract(vertexB))) {
					float conditionA = edge.dotProduct(q_vertexA);
					float conditionB = edge.dotProduct(q_vertexB);
					if (conditionA >= 0f && conditionB <= 0f) {
						float edgeT = conditionA / edge.getLengthSquared();
						try (var scaledEdge = VectorPool.of(edge, x -> x.multiply(edgeT));
							 var intersectionPoint = VectorPool.of(vertexA, x -> x.add(scaledEdge))) {
							collision.set(root1, intersectionPoint);
						}
					}
				}
			}
			if (root2 >= 0f && root2 <= 1f && root2 <= collision.getT()) {
				try (var scaledVelocity = VectorPool.of(velocity, x -> x.multiply(root2));
					 var q = VectorPool.of(position, x -> x.add(scaledVelocity));
					 var q_vertexA = VectorPool.of(q, x -> x.subtract(vertexA));
					 var q_vertexB = VectorPool.of(q, x -> x.subtract(vertexB))) {
					float conditionA = edge.dotProduct(q_vertexA);
					float conditionB = edge.dotProduct(q_vertexB);
					if (conditionA >= 0f && conditionB <= 0f) {
						float edgeT = conditionA / edge.getLengthSquared();
						try (var scaledEdge = VectorPool.of(edge, x -> x.multiply(edgeT));
							 var intersectionPoint = VectorPool.of(vertexA, x -> x.add(scaledEdge))) {
							collision.set(root2, intersectionPoint);
						}
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
			collision.test(t, vertex);
		}
	}
	public static float getLowestRoot(float a, float b, float c) {
		float determinant = b * b - 4.0f * a * c;

		if (determinant < 0f) {
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

		if (root1 > 0f) {
			return root1;
		}
		if (root2 > 0f) {
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
			collideWithWorld(position, velocity, 0, remainingVelocity);
		}

		// Convert final result back to r3
		// position.multiply(eRadius);
		// velocity.multiply(eRadius);
	}
	public static void collideWithWorld(Vector3D position, Vector3D velocity, int collisionRecursionDepth, Vector3D remainingVelocity) {
		if (collisionRecursionDepth > MAX_RECURSION_DEPTH) {
			return;
		}
		if (remainingVelocity.getLengthSquared() < BUFFER_DISTANCE * BUFFER_DISTANCE) {
			return;
		}
		Collision collision = checkCollision(position, remainingVelocity);
		if (collision.getT() >= 1f) {
			float length = remainingVelocity.getLength() - BUFFER_DISTANCE;
			if (length > 0) {
				position.add(remainingVelocity.scaleToLength(length));
			}
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
			float length = usedVelocity.getLength() - BUFFER_DISTANCE;
			if (length > 0) {
				position.add(usedVelocity.scaleToLength(length));
			}
			// TODO: Incorporate friction & elasticity
			collideWithWorld(position, velocity, collisionRecursionDepth + 1, remainingVelocity);
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
