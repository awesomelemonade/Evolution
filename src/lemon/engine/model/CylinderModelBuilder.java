package lemon.engine.model;

import lemon.engine.math.Vector3D;

public class CylinderModelBuilder {
	public static ModelBuilder build(ModelBuilder builder, int n, float radius, float height) {
		builder.addVertices(Vector3D.of(0, -height / 2, 0), Vector3D.of(0, height / 2, 0));
		for (int i = 0; i < n; ++i) {
			Vector3D x = Vector3D.of((float) (Math.cos(i * 2 * Math.PI / n) * radius), height / 2, (float) (Math.sin(i * 2 * Math.PI / n) * radius));
			builder.addVertices(x, Vector3D.of(x.x(), -x.y(), x.z()));
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
