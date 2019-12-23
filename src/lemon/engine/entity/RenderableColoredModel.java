package lemon.engine.entity;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lemon.engine.toolbox.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.control.Initializable;
import lemon.engine.math.Vector3D;
import lemon.engine.render.Renderable;
import lemon.engine.render.VertexArray;
import lemon.engine.toolbox.Toolbox;

public class RenderableColoredModel implements ColoredModel, Renderable {
	private VertexArray vertexArray;
	private Vector3D[] vertices;
	private int[] indices;
	private Color[] colors;

	public RenderableColoredModel(Vector3D[] vertices, int[] indices) {
		this(vertices, indices, Color.randomOpaque(vertices.length));
	}
	public RenderableColoredModel(Vector3D[] vertices, int[] indices, Color[] colors) {
		this.vertices = vertices;
		this.indices = indices;
		this.colors = colors;
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			vao.generateVbo().bind(GL15.GL_ELEMENT_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, Toolbox.toIntBuffer(indices), GL15.GL_STATIC_DRAW);
			}, false);
			vao.generateVbo().bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, getFloatBuffer(), GL15.GL_STATIC_DRAW);
				GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 * 4, 0);
				GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 7 * 4, 3 * 4);
			});
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
		});
	}
	private FloatBuffer getFloatBuffer() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length * 3 * 4);
		for (int i = 0; i < vertices.length; i++) {
			buffer.put(vertices[i].getX());
			buffer.put(vertices[i].getY());
			buffer.put(vertices[i].getZ());
			buffer.put(colors[i].getRed());
			buffer.put(colors[i].getGreen());
			buffer.put(colors[i].getBlue());
			buffer.put(colors[i].getAlpha());
		}
		buffer.flip();
		return buffer;
	}
	@Override
	public Vector3D[] getVertices() {
		return vertices;
	}
	@Override
	public int[] getIndices() {
		return indices;
	}
	@Override
	public Color[] getColors() {
		return colors;
	}
	@Override
	public void render() {
		vertexArray.bind(vao -> {
			GL11.glDrawElements(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_INT, 0);
		});
	}
}
