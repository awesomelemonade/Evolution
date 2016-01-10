package lemon.engine.function;

public class SzudzikPair implements PairingFunction {
	@Override
	public Integer resolve(Integer x, Integer y) {
		if(x<0||y<0){
			throw new IllegalArgumentException("Out of Range: "+x+" "+y);
		}
		long z = (x>=y?x*x+x+y:x+y*y);
		if(z<Integer.MIN_VALUE||z>Integer.MAX_VALUE){
			throw new IllegalArgumentException("Out of Range: "+z);
		}
		return (int)z;
	}
}
