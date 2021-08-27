package lemon.engine.function;

import java.util.Optional;
import java.util.function.BiFunction;

import lemon.engine.math.MutableLine;
import lemon.engine.math.Sphere;
import lemon.engine.math.Vector3D;

public class RaySphereIntersection implements BiFunction<MutableLine, Sphere, Optional<Float>> {
	@Override
	public Optional<Float> apply(MutableLine ray, Sphere sphere) {
		float t0, t1;
		Vector3D l = sphere.center().subtract(ray.origin());
		float tca = l.dotProduct(ray.direction());
		if (tca < 0) {
			return Optional.empty();
		}
		float d2 = l.dotProduct(l) - tca * tca;
		if (d2 > sphere.radius()) {
			return Optional.empty();
		}
		float thc = (float) Math.sqrt(sphere.radius() - d2);
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
		return Optional.of(ray.direction().length() * t0);
	}
}
