package lemon.engine.model;

import lemon.engine.math.Vector3D;

import java.util.function.BiFunction;

public class CylinderModelBuilder {
	public static ModelBuilder build(ModelBuilder builder, int n, float radius, float height) {
		builder.addVertices(new Vector3D(0, -height / 2, 0), new Vector3D(0, height / 2, 0));
		for (int i = 0; i < n; ++i) {
			Vector3D x = new Vector3D((float) (Math.cos(i * 2 * Math.PI / n) * radius), height / 2, (float) (Math.sin(i * 2 * Math.PI / n) * radius));
			builder.addVertices(x, new Vector3D(x.x(), -x.y(), x.z()));
		}
		for (int i = 0; i < n; ++i) {
			// Base
			builder.addIndices(0, 2 * i + 2, 2 * ((i + 1) % n) + 2, // Top Base
					1, 2 * i + 3, 2 * ((i + 1) % n) + 3); // Bottom Base
			// Lateral Surface
			builder.addIndices(2 * i + 2, 2 * ((i + 1) % n) + 2, 2 * i + 3,
					2 * ((i + 1) % n) + 2, 2 * i + 3, 2 * ((i + 1) % n) + 3);
		}
		return builder;
	}
}
