package lemon.engine.entity;

import lemon.engine.math.Vector3D;

import java.util.List;
import java.util.function.BiFunction;

public class CylinderModelBuilder<T extends Model> extends ModelBuilder<T> {
	public CylinderModelBuilder(BiFunction<Vector3D[], int[], T> constructor, int n, float radius, float height) {
		super(constructor);
		this.addVertices(new Vector3D(0, -height / 2, 0), new Vector3D(0, height / 2, 0));
		for (int i = 0; i < n; ++i) {
			Vector3D x = new Vector3D((float) Math.cos(i * 2 * Math.PI / n), -height/2, (float) Math.sin(i * 2 * Math.PI / n));
			this.addVertices(x, new Vector3D(x.getX(), -x.getY(), x.getZ()));
		}
		for (int i = 0; i < n; ++i) {
			// Base
			this.addIndices(0, 2 * i + 2, 2 * ((i + 1) % n) + 2, // Top Base
					1, 2 * i + 3, 2 * ((i + 1) % n) + 3); // Bottom Base
			// Lateral Surface
			this.addIndices(2 * i + 2, 2 * ((i + 1) % n) + 2, 2 * i + 3,
					2 * ((i + 1) % n) + 2, 2 * i + 3, 2 * ((i + 1) % n) + 3);
		}
	}
}
