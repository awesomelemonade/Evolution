package lemon.engine.evolution;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.control.InitEvent;
import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.render.ModelDataBuffer;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.render.VertexArray;
import lemon.engine.toolbox.Toolbox;

public enum Game implements Listener {
	INSTANCE;
	
	private VertexArray vao;
	private ShaderProgram program;
	private ModelDataBuffer data;
	
	@Subscribe
	public void init(InitEvent event){
		data = new ModelDataBuffer(4*5, 6);
		data.addIndices(0, 1, 2, 0, 3, 2);
		data.addData(-0.5f, -0.5f, 0, 0, 1);
		data.addData(0.5f, -0.5f, 1, 0, 1);
		data.addData(0.5f, 0.5f, 0, 0, 1);
		data.addData(-0.5f, 0.5f, 0, 1, 0);
		data.flip();
		vao = new VertexArray();
		GL30.glBindVertexArray(vao.getId());
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vao.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data.getIndicesBuffer(), GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vao.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data.getDataBuffer(), GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 5*4, 0); //5 floats, 4 bytes per float
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 5*4, 2*4); //2 floats, 4 bytes per float
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		program = new ShaderProgram(
			new int[]{0, 1},
			new String[]{"position", "color"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/vertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/fragmentShader"))
		);
		//uniform_test = program.getUniformVariable("Test");
	}
	@Subscribe
	public void update(UpdateEvent event){
		
	}
	@Subscribe
	public void render(RenderEvent event){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		//GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
		GL20.glUseProgram(program.getId());
		GL30.glBindVertexArray(vao.getId());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL11.glDrawElements(GL11.GL_TRIANGLES, data.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
