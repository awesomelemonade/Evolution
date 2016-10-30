package lemon.engine.function;

import lemon.engine.math.Line;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector;

public class MollerTrumbore implements Function2D<Triangle, Line, Float> {
	private final float EPSILON;
	private final boolean culling;
	public MollerTrumbore(){
		this(0.000001f, false);
	}
	public MollerTrumbore(boolean culling){
		this(0.000001f, culling);
	}
	public MollerTrumbore(float EPSILON, boolean culling){
		this.EPSILON = EPSILON;
		this.culling = culling;
	}
	@Override
	public Float resolve(Triangle triangle, Line ray) {
		Vector edge, edge2;
		Vector p, q, distance;
		float determinant;
		float inverseDeterminant, u, v, t;
		
		edge = triangle.getVertex2().subtract(triangle.getVertex1());
		edge2 = triangle.getVertex3().subtract(triangle.getVertex1());
		
		p = ray.getDirection().crossProduct(edge2);
		
		determinant = edge.dotProduct(p);
		
		if(culling){
			if(determinant < EPSILON){
				return null;
			}
		}else{
			if(determinant > -EPSILON && determinant < EPSILON){
				return null;
			}
		}
		inverseDeterminant = 1f/determinant;
		
		distance = ray.getOrigin().subtract(triangle.getVertex1());
		
		u = distance.dotProduct(p) * inverseDeterminant;
		
		if(u<0f||u>1f){
			return null;
		}
		
		q = distance.crossProduct(edge);
		
		v = ray.getDirection().dotProduct(q)*inverseDeterminant;
		
		if(v<0f||u+v>1f){
			return null;
		}
		
		t = edge2.dotProduct(q)*inverseDeterminant;
		
		if(t>EPSILON){
			return t;
		}
		return null;
	}
}
