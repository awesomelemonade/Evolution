package lemon.engine.game2d;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector;
import lemon.engine.math.Vector3D;

public class Box2D extends Vector {
	public Box2D(Box2D box){
		this(box.getX(), box.getY(), box.getWidth(), box.getHeight());
	}
	public Box2D(float x, float y, float width, float height){
		super(x, y, width, height);
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
	public Matrix getTransformationMatrix(){
		return MathUtil.getTranslation(new Vector3D(this.getX()+(this.getWidth()/2f), this.getY()+(this.getHeight()/2f), 0f))
				.multiply(MathUtil.getScalar(new Vector3D(this.getWidth()/2f, this.getHeight()/2f, 1f)));
	}
	public void scale(float factor){
		this.setWidth(this.getWidth()*factor);
		this.setHeight(this.getHeight()*factor);
	}
	//Scales it so it can fit inside this box's width
	public void scaleWidth(Box2D box){
		float ratio = box.getWidth()/this.getWidth();
		this.setWidth(box.getWidth());
		this.setHeight(this.getHeight()*ratio);
	}
	@Override
	public String toString(){
		return String.format("Box2D[%f, %f, %f, %f]", this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}
}
