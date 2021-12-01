package lemon.evolution.entity;

import lemon.engine.math.Vector3D;

public enum ExplodeType {
    MISSILE(0.2f, 3f),
    RAIN_DROPLET(0.1f, 1f),
    GRENADE(0.2f, 3f),
    MINI_MISSILE(0.15f, 2f),
    NUKE(0.3f, 20f);

    private final Vector3D scalar;
    private final float explosionRadius;

    private ExplodeType(float size, float explosionRadius) {
        this(Vector3D.of(size, size, size), explosionRadius);
    }

    private ExplodeType(Vector3D scalar, float explosionRadius) {
        this.scalar = scalar;
        this.explosionRadius = explosionRadius;
    }

    public Vector3D scalar() {
        return scalar;
    }

    public float explosionRadius() {
        return explosionRadius;
    }
}
