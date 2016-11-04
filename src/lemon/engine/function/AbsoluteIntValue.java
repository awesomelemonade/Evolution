package lemon.engine.function;

import java.util.function.IntUnaryOperator;

public enum AbsoluteIntValue implements IntUnaryOperator {
	MATH{
		@Override
		public int applyAsInt(int operand) {
			return Math.abs(operand);
		}
	}, HASHED{
		@Override
		public int applyAsInt(int operand) {
			return operand>0?(operand-1)*2+1:operand*-2;
		}
	};
}
