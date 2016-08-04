package lemon.engine.game2d;

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
}
