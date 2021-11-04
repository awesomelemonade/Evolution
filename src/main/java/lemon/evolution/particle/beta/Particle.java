package lemon.evolution.particle.beta;

import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;

import java.time.Duration;
import java.time.Instant;

public class Particle {
	private final MutableVector3D position;
	private final MutableVector3D velocity;
	private final Instant creationTime;

	public Particle(Vector3D position, Vector3D velocity) {
		this.position = MutableVector3D.of(position);
		this.velocity = MutableVector3D.of(velocity);
		this.creationTime = Instant.now();
	}

	public void update() {
		// TODO: no dt factor
		position.add(velocity.asImmutable());
	}

	public Duration getAge() {
		return Duration.between(creationTime, Instant.now());
	}

	public Vector3D position() {
		return position.asImmutable();
	}

	public Vector3D velocity() {
		return velocity.asImmutable();
	}

	public MutableVector3D mutablePosition() {
		return position;
	}

	public MutableVector3D mutableVelocity() {
		return velocity;
	}
}
