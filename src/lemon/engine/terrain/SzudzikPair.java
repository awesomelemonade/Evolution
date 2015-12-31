package lemon.engine.terrain;

public class SzudzikPair implements PairingFunction {
	@Override
	public int pair(int x, int y) {
		long a = (x>=0?2*(long)x:-2*(long)x-1);
		long b = (y>=0?2*(long)y:-2*(long)y-1);
		long c = ((a>=b?a*a+a+b:a+b*b)/2);
		long l = x<0&&y<0||x>=0&&y>=0?c:-c-1;
		if(l<Integer.MIN_VALUE||l>Integer.MAX_VALUE){
			throw new IllegalArgumentException("Out of Range: "+l);
		}
		return (int)l;
	}
}
