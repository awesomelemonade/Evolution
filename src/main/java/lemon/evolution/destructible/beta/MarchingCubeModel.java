package lemon.evolution.destructible.beta;

import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;

import java.util.List;

public record MarchingCubeModel(int[] indices, Vector3D[] vertices, float[][] textureWeights, int[] hashes, PreNormals preNormals, SparseGrid3D<List<Triangle>> triangles) {
}
