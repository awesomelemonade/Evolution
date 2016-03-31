package lemon.engine.math;

public class Percentage {
	private int part;
	private int whole;
	public Percentage(int whole){
		this(0, whole);
	}
	public Percentage(int part, int whole){
		this.part = part;
		this.whole = whole;
	}
	public void setPart(int part) {
		this.part = part;
	}
	public int getPart() {
		return part;
	}
	public void setWhole(int whole) {
		this.whole = whole;
	}
	public int getWhole() {
		return whole;
	}
	public float getPercentage(){
		return ((float)part)/((float)whole);
	}
}
