package lemon.engine.entity;

import lemon.engine.math.Vector3D;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class IndexedModelBuilder<T extends IndexedModel> {
    private BiFunction<List<Vector3D>, List<Integer>, T> constructor;
    private List<Vector3D> vertices;
    private List<Integer> indices;

    public IndexedModelBuilder(BiFunction<List<Vector3D>, List<Integer>, T> constructor) {
        this.constructor = constructor;
        vertices = new ArrayList<Vector3D>();
        indices = new ArrayList<Integer>();
    }
    public IndexedModelBuilder<T> addVertices(Vector3D... vertices) {
        for (Vector3D vertex : vertices) {
            this.vertices.add(vertex);
        }
        return this;
    }
    public IndexedModelBuilder<T> addIndices(int... indices) {
        for (int index : indices) {
            this.indices.add(index);
        }
        return this;
    }
    public List<Vector3D> getVertices() {
        return vertices;
    }
    public List<Integer> getIndices() {
        return indices;
    }
    public T build() {
        return constructor.apply(vertices, indices);
    }
}
