package lemon.evolution.destructible.beta;

import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;

import java.util.List;

public record MarchingCubeModel(Vector3D[] vertices, int[] indices, int[] hashes, PreNormals preNormals, List<Triangle> triangles) {
}
