package lemon.evolution.entity;

import lemon.engine.math.Vector3D;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.Location;

public class PuzzleBall extends AbstractEntity {
	public PuzzleBall(Location location, Vector3D velocity, Vector3D scalar) {
		super(location, velocity, scalar);
	}
}
