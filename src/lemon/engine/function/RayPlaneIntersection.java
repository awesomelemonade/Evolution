package lemon.engine.function;

import java.util.function.BiFunction;

import lemon.engine.math.Line;
import lemon.engine.math.Plane;

public class RayPlaneIntersection implements BiFunction<Line, Plane, Float> {
	//http://stackoverflow.com/questions/23975555/how-to-do-ray-plane-intersection
	@Override
	public Float apply(Line ray, Plane plane) {
		float denom = plane.getNormal().dotProduct(ray.getDirection());
		
		return null;
	}
}
