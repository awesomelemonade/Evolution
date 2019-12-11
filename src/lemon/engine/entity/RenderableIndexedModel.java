package lemon.engine.entity;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

public class RenderableIndexedModel implements IndexedModel, Renderable {
	private VertexArray vertexArray;
	private List<Vector3D> vertices;
	private List<Integer> indices;

	public RenderableIndexedModel(List<Vector3D> vertices, List<Integer> indices) {
		this.vertices = vertices;
		this.indices = indices;
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
		FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.size() * 3 * 4);
		for (Vector3D vertex : vertices) {
			buffer.put(vertex.getX());
			buffer.put(vertex.getY());
			buffer.put(vertex.getZ());
			buffer.put((float) Math.random());
			buffer.put((float) Math.random());
			buffer.put((float) Math.random());
			buffer.put(1f);
		}
		buffer.flip();
		return buffer;
	}
	@Override
	public List<Vector3D> getVertices() {
		return Collections.unmodifiableList(vertices);
	}
	@Override
	public List<Integer> getIndices() {
		return Collections.unmodifiableList(indices);
	}
	@Override
	public void render() {
		vertexArray.bind(vao -> {
			GL11.glDrawElements(GL11.GL_TRIANGLES, indices.size(), GL11.GL_UNSIGNED_INT, 0);
		});
	}
}
