package lemon.engine.function;

import java.util.function.Function;

import lemon.engine.math.Vector;

public enum QuadraticEquation implements Function<Vector, Vector> {
	INSTANCE;
	@Override
	public Vector apply(Vector coefficients) {
		float a = coefficients.get(0);
		float b = coefficients.get(1);
		float c = coefficients.get(2);
		float d = b*b-4*a*c;
		if(d<0){
			return Vector.EMPTY_VECTOR;
		}else if(d==0){
			return new Vector((float)((-b+Math.sqrt(d))/(2*a)));
		}else{
			float sqrt = (float) Math.sqrt(d);
			return new Vector((-b+sqrt)/(2*a), (-b-sqrt)/(2*a));
		}
	}
}
