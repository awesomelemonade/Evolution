package lemon.engine.math;

import java.util.function.BinaryOperator;

public class Box2D extends Vector {
	public Box2D(float x, float y, float width, float height){
		super(4);
		this.setX(x);
		this.setY(y);
		this.setWidth(width);
		this.setHeight(height);
	}
	public boolean intersect(float x, float y){
		return x>this.getX()&&x<this.getX()+this.getWidth()&&y>this.getY()&&y<this.getY()+this.getHeight();
	}
	public void setX(float x){
		super.set(0, x);
	}
	public float getX(){
		return super.get(0);
	}
	public void setY(float y){
		super.set(1, y);
	}
	public float getY(){
		return super.get(1);
	}
	public void setWidth(float width){
		super.set(2, width);
	}
	public float getWidth(){
		return super.get(2);
	}
	public void setHeight(float height){
		super.set(3, height);
	}
	public float getHeight(){
		return super.get(3);
	}
	public Box2D add(Box2D vector){
		return operate(vector, BasicFloatOperator.ADDITION);
	}
	public Box2D subtract(Box2D vector){
		return operate(vector, BasicFloatOperator.SUBTRACTION);
	}
	public Box2D multiply(Box2D vector){
		return operate(vector, BasicFloatOperator.MULTIPLICATION);
	}
	@Override
	public Box2D multiply(float scale){
		return operate(scale, BasicFloatOperator.MULTIPLICATION);
	}
	public Box2D divide(Box2D vector){
		return operate(vector, BasicFloatOperator.DIVISION);
	}
	@Override
	public Box2D divide(float scale){
		return operate(scale, BasicFloatOperator.DIVISION);
	}
	public Box2D average(Box2D vector){
		return operate(vector, BasicFloatOperator.AVERAGE);
	}
	public Box2D operate(float scale, BinaryOperator<Float> operator){
		return new Box2D(operator.apply(this.getX(), scale), operator.apply(this.getY(), scale),
				operator.apply(this.getWidth(), scale), operator.apply(this.getHeight(), scale));
	}
	public Box2D operate(Box2D vector, BinaryOperator<Float> operator){
		return new Box2D(operator.apply(this.getX(), vector.getX()), operator.apply(this.getY(), vector.getY()),
				operator.apply(this.getWidth(), vector.getWidth()), operator.apply(this.getHeight(), vector.getHeight()));
	}
}
