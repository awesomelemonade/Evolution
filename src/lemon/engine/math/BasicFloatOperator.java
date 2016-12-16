package lemon.engine.math;

import java.util.function.BinaryOperator;

public enum BasicFloatOperator implements BinaryOperator<Float> {
	ADDITION{
		@Override
		public Float apply(Float x, Float y) {
			return x+y;
		}
	}, SUBTRACTION{
		@Override
		public Float apply(Float x, Float y) {
			return x-y;
		}
	}, MULTIPLICATION{
		@Override
		public Float apply(Float x, Float y) {
			return x*y;
		}
	}, DIVISION{
		@Override
		public Float apply(Float x, Float y) {
			return x/y;
		}
	}, AVERAGE{
		@Override
		public Float apply(Float x, Float y) {
			return (x+y)/2;
		}
	};
	@Override
	public abstract Float apply(Float x, Float y);
}
