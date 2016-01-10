package lemon.engine.function;

public class AbsoluteValue implements Function<Integer, Integer> {
	@Override
	public Integer resolve(Integer key) {
		return key>0?(key-1)*2+1:key*-2;
	}
}
