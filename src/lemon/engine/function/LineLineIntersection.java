package lemon.engine.function;

import java.util.function.BinaryOperator;

import lemon.engine.math.Line;
import lemon.engine.math.Vector;
import lemon.engine.math.Vector3D;

public enum LineLineIntersection implements BinaryOperator<Line> {
	INSTANCE;
	@Override
	public Line apply(Line line, Line line2) {
		System.out.println(line+" - "+line2);
		Vector3D n = line.getDirection().crossProduct(line2.getDirection());
		float nAbs = n.getAbsoluteValue();
		if(nAbs==0){
			//parallel
			
		}
		Vector n1 = line.getDirection().crossProduct(n);
		Vector n2 = line2.getDirection().crossProduct(n);
		Vector offset = line2.getOrigin().subtract(line.getOrigin());
		System.out.println("Distance: " + Math.abs(n.divide(nAbs).dotProduct(offset)));
		// Warning: Not updated for new Vector
		return new Line(line.getOrigin().add(line.getDirection().multiply(offset.dotProduct(n2)/line.getDirection().dotProduct(n2))), 
				line2.getOrigin().add(line.getDirection().multiply(offset.dotProduct(n1)/line2.getDirection().dotProduct(n1))));
	}
}
