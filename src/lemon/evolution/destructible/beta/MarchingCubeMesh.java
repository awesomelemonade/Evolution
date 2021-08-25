package lemon.evolution.destructible.beta;

import lemon.engine.math.Vector3D;

public class MarchingCubeMesh {
    private final int[] indices;
    private final Vector3D[] vertices;
    private final int[] hashes;

    public MarchingCubeMesh(int[] indices, Vector3D[] vertices, int[] hashes) {
        this.indices = indices;
        this.vertices = vertices;
        this.hashes = hashes;
    }
    public int[] getIndices() {
        return indices;
    }
    public Vector3D[] getVertices() {
        return vertices;
    }
    public int[] getHashes() {
        return hashes;
    }
}
