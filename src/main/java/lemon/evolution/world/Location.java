package lemon.evolution.world;

import lemon.engine.math.Vector3D;

public record Location(World world, Vector3D position) {
	public Location add(Vector3D delta) {
		return new Location(world, position.add(delta));
	}
}
