package lemon.engine.math;

public class GaussianElimination {
	// Solves A * x = b matrix equation
	// Based on https://introcs.cs.princeton.edu/java/95linear/GaussianElimination.java.html
	private static final float EPSILON = 1e-10f;
	public static Vector solve(Matrix a, Vector b) {
		// Copy Both
		a = new Matrix(a);
		b = new Vector(b);
		// Solve
		int n = b.getDimensions();
		for (int p = 0; p < n; ++p) {
			// find pivot row
			int max = p;
			for (int i = p + 1; i < n; ++i) {
				if (Math.abs(a.get(i, p)) > Math.abs(a.get(max, p))) {
					max = i;
				}
			}
			// swap
			if (p != max) {
				//a.swap(p, max);
				float temp = b.get(p);
				b.set(p, b.get(max));
				b.set(max, temp);
			}
			// singular or nearly singular
			if (Math.abs(a.get(p, p)) <= EPSILON) {
				throw new ArithmeticException("Matrix is singular or nearly singular");
			}
			// pivot within a and b
			for (int i = p + 1; i < n; ++i) {
				float alpha = a.get(i, p) / a.get(p, p);
				b.set(i, b.get(i) - alpha * b.get(p));
				for (int j = p; j < n; ++j) {
					a.set(i, j, a.get(i, j) - alpha * a.get(p, j));
				}
			}
		}
		// back substitution
		Vector x = new Vector(n);
		for (int i = n - 1; i >= 0; --i) {
			float sum = 0f;
			for (int j = i + 1; j < n; ++j) {
				sum += a.get(i, j) * x.get(j);
			}
			x.set(i, (b.get(i) - sum) / a.get(i, i));
		}
		return x;
	}
}
