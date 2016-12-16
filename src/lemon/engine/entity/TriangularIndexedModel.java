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

public class TriangularIndexedModel implements IndexedModel, Initializable, Renderable {
	private VertexArray vertexArray;
	private List<Vector3D> vertices;
	private List<Integer> indices;
	private TriangularIndexedModel(List<Vector3D> vertices, List<Integer> indices){
		this.vertices = vertices;
		this.indices = indices;
	}
	public static class Builder{
		private List<Vector3D> vertices;
		private List<Integer> indices;
		public Builder(){
			vertices = new ArrayList<Vector3D>();
			indices = new ArrayList<Integer>();
		}
		public Builder addVertices(Vector3D... vertices){
			for(Vector3D vertex: vertices){
				this.vertices.add(vertex);
			}
			return this;
		}
		public Builder addIndices(int... indices){
			for(int index: indices){
				this.indices.add(index);
			}
			return this;
		}
		public List<Vector3D> getVertices(){
			return vertices;
		}
		public List<Integer> getIndices(){
			return indices;
		}
		public TriangularIndexedModel build(){
			return new TriangularIndexedModel(vertices, indices);
		}
		public TriangularIndexedModel buildAndInit(){
			TriangularIndexedModel model = this.build();
			model.init();
			return model;
		}
	}
	public void init(){
		vertexArray = new VertexArray();
		GL30.glBindVertexArray(vertexArray.getId());
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vertexArray.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, Toolbox.toIntBuffer(indices), GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexArray.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, getFloatBuffer(), GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7*4, 0);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 7*4, 3*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	private FloatBuffer getFloatBuffer(){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.size()*3*4);
		for(Vector3D vertex: vertices){
			buffer.put(vertex.getX());
			buffer.put(vertex.getY());
			buffer.put(vertex.getZ());
			buffer.put((float)Math.random());
			buffer.put((float)Math.random());
			buffer.put((float)Math.random());
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
		GL30.glBindVertexArray(vertexArray.getId());
		GL11.glDrawElements(GL11.GL_TRIANGLES, indices.size(), GL11.GL_UNSIGNED_INT, 0);
		GL30.glBindVertexArray(0);
	}
}
