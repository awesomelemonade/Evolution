package lemon.engine.toolbox;

public class Color {
	private float red;
	private float green;
	private float blue;
	private float alpha;
	public Color(){
		this(1f);
	}
	public Color(float value){
		this(value, value, value, 1f);
	}
	public Color(float red, float green, float blue){
		this(red, green, blue, 1f);
	}
	public Color(float red, float green, float blue, float alpha){
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}
	public Color(Color color){
		this(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
	public void setRed(float red){
		this.red = red;
	}
	public float getRed(){
		return red;
	}
	public void setGreen(float green){
		this.green = green;
	}
	public float getGreen(){
		return green;
	}
	public void setBlue(float blue){
		this.blue = blue;
	}
	public float getBlue(){
		return blue;
	}
	public void setAlpha(float alpha){
		this.alpha = alpha;
	}
	public float getAlpha(){
		return alpha;
	}
	@Override
	public String toString(){
		return String.format("Color[%f, %f, %f, %f]", red, green, blue, alpha);
	}
}
