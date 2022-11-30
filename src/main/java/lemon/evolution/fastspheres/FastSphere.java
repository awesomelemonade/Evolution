package lemon.evolution.fastspheres;

import lemon.engine.math.*;
import lemon.engine.toolbox.Color;

public record FastSphere(Vector3D position, float size, Color color) {
    public FastSphere(Vector3D position, Color color) {
        this(position, 1f, color);
    }
}
