package lemon.engine.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class RawModel {
	private ModelData data;
	private VertexArray vao;
	private List<Integer> attributes;
	
	public RawModel(ModelData data){
		this.data = data;
		attributes = new ArrayList<Integer>();
		vao = new VertexArray();
		GL30.glBindVertexArray(vao.getId());
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vao.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data.getIndicesBuffer(), GL15.GL_STATIC_DRAW);
		for(DataArray array: data.getDataArrays()){
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vao.generateVbo().getId());
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, array.getFloatBuffer(), array.getUsage());
			for(AttributePointer pointer: array.getAttributePointers()){
				GL20.glVertexAttribPointer(pointer.getIndex(), pointer.getSize(), GL11.GL_FLOAT, false, pointer.getStride(), pointer.getPointerOffset());
				attributes.add(pointer.getIndex());
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
		GL30.glBindVertexArray(0);
	}
	public void render(){
		GL30.glBindVertexArray(vao.getId());
		for(int i: attributes){
			GL20.glEnableVertexAttribArray(i);
		}
		GL11.glDrawElements(GL11.GL_TRIANGLES, data.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		for(int i: attributes){
			GL20.glDisableVertexAttribArray(i);
		}
		GL30.glBindVertexArray(0);
	}
}
