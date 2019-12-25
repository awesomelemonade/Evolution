package lemon.engine.model;

import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Color;

public class AbstractColoredModel implements ColoredModel {
	private Vector3D[] vertices;
	private int[] indices;
	private Color[] colors;

	public AbstractColoredModel(Vector3D[] vertices, int[] indices) {
		this(vertices, Color.randomOpaque(indices.length), indices);
	}
	public AbstractColoredModel(Vector3D[] vertices, Color[] colors, int[] indices) {
		this.vertices = vertices;
		this.colors = colors;
		this.indices = indices;
	}

	@Override
	public Color[] getColors() {
		return colors;
	}
	@Override
	public Vector3D[] getVertices() {
		return vertices;
	}
	@Override
	public int[] getIndices() {
		return indices;
	}
}
