package lemon.evolution.particle.beta;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;

import java.time.Duration;
import java.time.Instant;

public class Particle {
	private final Vector3D translation;
	private final Vector3D translationalVelocity;
	private final Vector3D rotation;
	private final Vector3D rotationalVelocity;
	private final Instant creationTime;
	public Particle(Vector3D translation, Vector3D translationalVelocity, Vector3D rotation, Vector3D rotationalVelocity) {
		this.translation = translation;
		this.translationalVelocity = translationalVelocity;
		this.rotation = rotation;
		this.rotationalVelocity = rotationalVelocity;
		this.creationTime = Instant.now();
	}
	public void update() {
		translation.add(translationalVelocity);
		rotation.add(rotationalVelocity);
	}
	public Matrix getTransformationMatrix() {
		return MathUtil.getTranslation(translation).multiply(MathUtil.getRotation(rotation));
	}
	public Duration getAge() {
		return Duration.between(creationTime, Instant.now());
	}
	public Vector3D getTranslation() {
		return translation;
	}
	public Vector3D getTranslationalVelocity() {
		return translationalVelocity;
	}
}
