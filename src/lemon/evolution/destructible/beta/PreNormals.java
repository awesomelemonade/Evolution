package lemon.evolution.destructible.beta;

import lemon.engine.math.Vector3D;

import java.util.HashMap;
import java.util.Map;

public class PreNormals {
	private final Map<Integer, Vector3D> map = new HashMap<>();

	public void addNormal(int hash, Vector3D vector) {
		map.merge(hash, vector, Vector3D::add);
	}

	public Vector3D getNormal(int hash) {
		return map.getOrDefault(hash, Vector3D.ZERO);
	}

	public void addNormal(int x, int y, int z, int w, Vector3D vector) {
		this.addNormal(hash(x, y, z, w), vector);
	}

	public Vector3D getNormal(int x, int y, int z, int w) {
		return getNormal(hash(x, y, z, w));
	}

	private int hash(int x, int y, int z, int w) {
		return (x << 24) | (y << 16) | (z << 8) | w;
	}
}