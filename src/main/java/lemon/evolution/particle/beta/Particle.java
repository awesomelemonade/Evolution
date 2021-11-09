package lemon.evolution.particle.beta;

import lemon.engine.math.*;
import lemon.engine.toolbox.Color;

import java.time.Duration;
import java.time.Instant;

public class Particle {
	private final Duration fadeDuration = Duration.ofSeconds(1);
	private final Duration expandDuration = Duration.ofMillis(500);
	private final Duration changeColorDuration = Duration.ofSeconds(1);
	private final ParticleType type;
	private final MutableVector3D position;
	private final MutableVector3D velocity;
	private final Vector3D force;
	private final Instant creationTime;
	private final Instant expiryTime;
	private final MutableVector4D color;
	private final float maxSize;
	private float size;

	public Particle(ParticleType type, Vector3D position, Vector3D velocity, Vector3D force, Duration duration, Color color, float size) {
		this.type = type;
		this.position = MutableVector3D.of(position);
		this.velocity = MutableVector3D.of(velocity);
		this.force = force;
		this.creationTime = Instant.now();
		this.expiryTime = creationTime.plus(duration);
		this.color = MutableVector4D.of(color);
		this.maxSize = size;
		this.size = 0f;
	}

	public void update() {
		var now = Instant.now();
		var timeFromCreation = Duration.between(creationTime, now);
		var timeToExpiry = Duration.between(now, expiryTime);
		color.set(Color.RED.subtract(Color.YELLOW).multiply(MathUtil.clamp(((float) timeFromCreation.toMillis()) / ((float) changeColorDuration.toMillis()), 0f, 1f)).add(Color.YELLOW));
		color.setW(MathUtil.clamp(((float) timeToExpiry.toMillis()) / ((float) fadeDuration.toMillis()), 0f, 1f));
		size = MathUtil.clamp(((float) timeFromCreation.toMillis()) / ((float) expandDuration.toMillis()), 0f, 1f) * maxSize;
		velocity.multiply(0.85f);
		velocity.add(force);
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

	public Instant expiryTime() {
		return expiryTime;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public float size() {
		return size;
	}

	public Vector4D color() {
		return color.asImmutable();
	}
}
