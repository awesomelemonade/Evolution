package lemon.evolution.destructible.beta;

import lemon.engine.math.Vector;

public interface ScalarField<T extends Vector<T>> {
	public float get(T vector);
}
