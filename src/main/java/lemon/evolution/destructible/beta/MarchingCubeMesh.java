package lemon.evolution.destructible.beta;

import lemon.engine.math.Vector3D;

public record MarchingCubeMesh(int[] indices, Vector3D[] vertices, float[][] textureWeights, int[] prenormalHashes, TripleIndex[] triangleCoords) {
}
