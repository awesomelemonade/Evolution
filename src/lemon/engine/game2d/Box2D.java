package lemon.engine.game2d;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;

public class Box2D {
	private float x;
	private float y;
	private float width;
	private float height;
	public Box2D(float x, float y, float width, float height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	public boolean intersect(float x, float y){
		return x>this.x&&x<this.x+this.width&&y>this.y&&y<this.y+this.height;
	}
	public void setX(float x){
		this.x = x;
	}
	public float getX(){
		return x;
	}
	public void setY(float y){
		this.y = y;
	}
	public float getY(){
		return y;
	}
	public void setWidth(float width){
		this.width = width;
	}
	public float getWidth(){
		return width;
	}
	public void setHeight(float height){
		this.height = height;
	}
	public float getHeight(){
		return height;
	}
	public Matrix getTransformationMatrix(){
		return MathUtil.getTranslation(new Vector3D(x+(width/2f), y+(height/2f), 0f))
				.multiply(MathUtil.getScalar(new Vector3D(width/2f, height/2f, 1f)));
	}
	@Override
	public String toString(){
		return String.format("Box2D[%f, %f, %f, %f]", x, y, width, height);
	}
}
