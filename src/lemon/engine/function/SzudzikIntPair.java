package lemon.engine.function;

import java.util.function.IntBinaryOperator;

public enum SzudzikIntPair implements IntBinaryOperator {
	INSTANCE;
	@Override
	public int applyAsInt(int x, int y) {
		if(x<0||y<0){
			throw new IllegalArgumentException("Out of Range: "+x+" "+y);
		}
		int a = x>=0?2*x:-2*x-1;
		int b = y>=0?2*y:-2*y-1;
		int c = (a>=b?a*a+a+b:a+b*b)/2;
		return x<0&&b<0||a>=0&&b>=0?c:-c-1;
		/*
		long z = (x>=y?x*x+x+y:x+y*y);
		if(z<Integer.MIN_VALUE||z>Integer.MAX_VALUE){
			throw new IllegalArgumentException("Out of Range: "+z);
		}
		return (int)z;*/
	}
}
