package lemon.engine.function;

import java.util.Optional;
import java.util.function.BiFunction;

import lemon.engine.math.Line;
import lemon.engine.math.Sphere;
import lemon.engine.math.Vector3D;

public class RaySphereIntersection implements BiFunction<Line, Sphere, Optional<Float>> {
	@Override
	public Optional<Float> apply(Line ray, Sphere sphere) {
		float t0, t1;
		Vector3D l = sphere.getCenter().subtract(ray.getOrigin());
		float tca = l.dotProduct(ray.getDirection());
		if (tca < 0) {
			return Optional.empty();
		}
		float d2 = l.dotProduct(l) - tca * tca;
		if (d2 > sphere.getRadius()) {
			return Optional.empty();
		}
		float thc = (float) Math.sqrt(sphere.getRadius() - d2);
		t0 = tca - thc;
		t1 = tca + thc;
		if (t0 > t1) {
			// swaps t0 and t1
			float temp = t0;
			t0 = t1;
			t1 = temp;
		}
		if (t0 < 0) { // if t0 is negative
			t0 = t1; // set t0 to t1
			if (t0 < 0) { // if t0 is negative
				return Optional.empty();
			}
		}
		return Optional.of(ray.getDirection().multiply(t0).getAbsoluteValue());
	}
}
