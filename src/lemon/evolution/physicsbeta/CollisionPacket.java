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
		float normalDotVelocity = triangle.getNormal().dotProduct(velocity);
		if (normalDotVelocity <= 0.001f) {
			// if sphere is travelling parallel to the plane
			if (Math.abs(normalDotVelocity) <= 0.001f) {
				try (var temp = VectorPool.of(position, x -> x.subtract(triangle.getVertex1()))) {
					float signedDistanceToTrianglePlane =
							triangle.getNormal().dotProduct(temp);
					if (Math.abs(signedDistanceToTrianglePlane) >= 1.0f) {
						// Sphere is not embedded in plane
						// No collision possible
						return;
					} else {
						// Sphere is embedded in plane
						// It intersects the whole range [0 .. 1]
						// t0 = 0.0f;
					}
				}
			} else {
				try (var temp = VectorPool.of(position, x -> x.subtract(triangle.getVertex1()))) {
					float x = triangle.getNormal().dotProduct(temp) - 1f;
					if (x >= -0.001f && x <= -normalDotVelocity) {
						float t = MathUtil.clamp(-x / normalDotVelocity, 0f, 1f);
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
			checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex1(), collision);
			checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex2(), collision);
			checkVertex(velocitySquaredLength, velocity, position, triangle.getVertex3(), collision);

			// Check against edges
			checkEdge(position, velocity, triangle.getVertex1(), triangle.getVertex2(), collision);
			checkEdge(position, velocity, triangle.getVertex2(), triangle.getVertex3(), collision);
			checkEdge(position, velocity, triangle.getVertex3(), triangle.getVertex1(), collision);
		}
	}
	private static void checkEdge(Vector3D position, Vector3D velocity, Vector3D vertexA, Vector3D vertexB, Collision collision) {
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
			float temp = b >= 0 ? -b - sqrtDet : -b + sqrtDet;
			float root1Numerator = temp;
			float root1Denominator = 2f * a;
			float rightSideMultiplied1 = Math.min(1f, collision.getT()) * root1Denominator;
			if (root1Numerator >= 0f && root1Numerator <= rightSideMultiplied1) {
				float root1 = MathUtil.clamp(root1Numerator / root1Denominator, 0f, 1f);
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
			float root2Numerator = 2f * c;
			float root2Denominator = temp;
			float rightSideMultiplied2 = Math.min(1f, collision.getT()) * root2Denominator;
			if (root2Numerator >= 0f && root2Numerator <= rightSideMultiplied2) {
				float root2 = MathUtil.clamp(root2Numerator / root2Denominator, 0f, 1f);
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
	private static void checkVertex(float a, Vector3D velocity, Vector3D base, Vector3D vertex, Collision collision) {
		try (var base_vertex = VectorPool.of(base, v -> v.subtract(vertex))) {
			float b = 2.0f * velocity.dotProduct(base_vertex);
			float c = vertex.getDistanceSquared(base) - 1.0f;
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
	}
	// response steps
	public static void collideAndSlide(Vector3D position, Vector3D velocity) {
		collideAndSlide(position, velocity, MAX_RECURSION_DEPTH);
	}
	public static void collideAndSlide(Vector3D position, Vector3D velocity, int maxRecursionDepth) {
		// Vector3D eRadius = new Vector3D(1f, 1f, 1f); // ellipsoid radius

		// calculate position and velocity in eSpace
		// position.divide(eRadius);
		// velocity.divide(eRadius);

		// Iterate until we have our final position
		try (var remainingVelocity = VectorPool.of(velocity)) {
			collideWithWorld(position, velocity, 0, remainingVelocity, maxRecursionDepth);
		}

		// Convert final result back to r3
		// position.multiply(eRadius);
		// velocity.multiply(eRadius);
	}
	public static void collideWithWorld(Vector3D position, Vector3D velocity, int collisionRecursionDepth, Vector3D remainingVelocity, int maxRecursionDepth) {
		if (collisionRecursionDepth > maxRecursionDepth) {
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
			/*if (collisionRecursionDepth == 0) {
				velocity.multiply(0.995f);
				remainingVelocity.multiply(0.995f);
			}*/
			float length = usedVelocity.getLength() - BUFFER_DISTANCE;
			if (length > 0) {
				position.add(usedVelocity.scaleToLength(length));
			}
			collideWithWorld(position, velocity, collisionRecursionDepth + 1, remainingVelocity, maxRecursionDepth);
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








	// response steps
	public static boolean collideAndSlideIntersect(Vector3D position, Vector3D velocity) {
		try (var remainingVelocity = VectorPool.of(velocity)) {
			return collideWithWorldIntersect(position, velocity, 0, remainingVelocity);
		}
	}
	public static boolean collideWithWorldIntersect(Vector3D position, Vector3D velocity, int collisionRecursionDepth, Vector3D remainingVelocity) {
		Collision collision = checkCollision(position, remainingVelocity);
		if (collision.getT() >= 1f) {
			float length = remainingVelocity.getLength() - BUFFER_DISTANCE;
			if (length > 0) {
				position.add(remainingVelocity.scaleToLength(length));
			}
			return false;
		}
		try (var usedVelocity = VectorPool.of(remainingVelocity, x -> x.multiply(collision.getT()))) {
			float length = usedVelocity.getLength() - BUFFER_DISTANCE;
			if (length > 0) {
				position.add(usedVelocity.scaleToLength(length));
			}
			return true;
		}
	}

}
