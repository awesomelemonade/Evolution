package lemon.evolution.entity;

import lemon.engine.math.Vector3D;

public enum ExplosiveProjectileType {
    MISSILE(0.2f);

    private final Vector3D scalar;

    private ExplosiveProjectileType(float size) {
        this(Vector3D.of(size, size, size));
    }

    private ExplosiveProjectileType(Vector3D scalar) {
        this.scalar = scalar;
    }

    public Vector3D scalar() {
        return scalar;
    }
}
