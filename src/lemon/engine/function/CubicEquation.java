package lemon.engine.function;

import java.util.function.Function;

import lemon.engine.math.Vector;

public enum CubicEquation implements Function<Vector, Vector> {
	INSTANCE;
	@Override
	public Vector apply(Vector coefficients) {
		if(coefficients.getDimensions()!=4){
			throw new IllegalArgumentException("Arguments != 4");
		}
		float a = coefficients.get(0);
		float b = coefficients.get(1);
		float c = coefficients.get(2);
		float d = coefficients.get(3);
		if(a==0){
			return QuadraticEquation.INSTANCE.apply(new Vector(b, c, d));
		}
		if(d==0){
			return new Vector(QuadraticEquation.INSTANCE.apply(new Vector(a, b, c)), 0);
		}
		b/=a;
		c/=a;
		d/=a;
		float disc, q, q3, r, dum1, s, t, term1, r13;
		q = (3.0f*c-(b*b))/9.0f;
		r = -(27.0f*d)+b*(9.0f*c-2.0f*(b*b));
		r/=54.0f;
		q3 = q*q*q;
		disc=q3+r*r;
		term1 = b/3.0f;
		if(disc>0){ //one root real, two are complex
			float discrimsq = (float) Math.sqrt(disc);
			s = r+discrimsq;
			t = r-discrimsq;
			s = positiveCube(s);
			t = positiveCube(t);
			return new Vector(-term1+s+t);
		}else if(disc==0){ //real root+real double root
			r13 = positiveCube(r);
			return new Vector(-term1+r13*2.0f, -r13-term1);
		}else{ //all real roots
			q = -q;
			q3 = -q3;
			dum1 = (float) Math.acos(r/Math.sqrt(q3));
			r13 = (float) (2.0*Math.sqrt(q));
			return new Vector((float)(-term1+r13*Math.cos(dum1/3.0)), 
					(float)(-term1+r13*Math.cos((dum1+2.0*Math.PI)/3.0)),
					(float)(-term1+r13*Math.cos((dum1+4*Math.PI)/3)));
		}
	}
	private float positiveCube(float s){
		return (float) ((s<0)?-Math.pow(-s, 1.0/3.0):Math.pow(s, 1.0/3.0));
	}
}
