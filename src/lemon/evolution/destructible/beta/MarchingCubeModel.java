package lemon.evolution.destructible.beta;

import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;

import java.util.List;

public class MarchingCubeModel {
    private Vector3D[] vertices;
    private int[] indices;
    private int[] hashes;
    private PreNormals preNormals;
    private List<Triangle> triangles;
    public MarchingCubeModel(Vector3D[] vertices, int[] indices, int[] hashes, PreNormals preNormals, List<Triangle> triangles) {
        this.vertices = vertices;
        this.indices = indices;
        this.hashes = hashes;
        this.preNormals = preNormals;
        this.triangles = triangles;
    }
    public Vector3D[] getVertices() {
        return vertices;
    }
    public int[] getIndices() {
        return indices;
    }
    public int[] getHashes() {
        return hashes;
    }
    public PreNormals getPreNormals() {
        return preNormals;
    }
    public List<Triangle> getTriangles() {
        return triangles;
    }
}
