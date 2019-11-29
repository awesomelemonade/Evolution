package lemon.engine.function;

import java.util.Optional;
import java.util.function.BiFunction;

import lemon.engine.math.Line;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;

public class MollerTrumbore implements BiFunction<Triangle, Line, Optional<Float>> {
	private final float EPSILON;
	private final boolean culling;

	public MollerTrumbore() {
		this(0.000001f, false);
	}
	public MollerTrumbore(boolean culling) {
		this(0.000001f, culling);
	}
	public MollerTrumbore(float EPSILON, boolean culling) {
		this.EPSILON = EPSILON;
		this.culling = culling;
	}
	@Override
	public Optional<Float> apply(Triangle triangle, Line ray) {
		Vector3D edge, edge2;
		Vector3D p, q, distance;
		float determinant;
		float inverseDeterminant, u, v, t;

		edge = triangle.getVertex2().subtract(triangle.getVertex1());
		edge2 = triangle.getVertex3().subtract(triangle.getVertex1());

		p = ray.getDirection().crossProduct(edge2);

		determinant = edge.dotProduct(p);

		if (culling) {
			if (determinant < EPSILON) {
				return Optional.empty();
			}
		} else {
			if (determinant > -EPSILON && determinant < EPSILON) {
				return Optional.empty();
			}
		}
		inverseDeterminant = 1f / determinant;

		distance = ray.getOrigin().subtract(triangle.getVertex1());

		u = distance.dotProduct(p) * inverseDeterminant;

		if (u < 0f || u > 1f) {
			return Optional.empty();
		}

		q = distance.crossProduct(edge);

		v = ray.getDirection().dotProduct(q) * inverseDeterminant;

		if (v < 0f || u + v > 1f) {
			return Optional.empty();
		}

		t = edge2.dotProduct(q) * inverseDeterminant;

		if (t > EPSILON) {
			return Optional.of(t);
		}
		return Optional.empty();
	}
}
