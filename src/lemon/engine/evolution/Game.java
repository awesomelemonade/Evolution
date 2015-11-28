package lemon.engine.evolution;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.InitEvent;
import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.render.AttributePointer;
import lemon.engine.render.DataArray;
import lemon.engine.render.ModelDataBuffer;
import lemon.engine.render.RawModel;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.toolbox.Toolbox;

public enum Game implements Listener {
	INSTANCE;
	
	private ShaderProgram program;
	
	private RawModel model;
	
	@Subscribe
	public void init(InitEvent event){
		ModelDataBuffer data = new ModelDataBuffer(6);
		data.addIndices(0, 1, 2, 0, 3, 2);
		DataArray array = new DataArray(4*5,
				new AttributePointer(0, 2, 5*4, 0),
				new AttributePointer(1, 3, 5*4, 2*4)
		);
		array.addData(-0.5f, -0.5f, 0, 0, 1);
		array.addData(0.5f, -0.5f, 1, 0, 1);
		array.addData(0.5f, 0.5f, 0, 0, 1);
		array.addData(-0.5f, 0.5f, 0, 1, 0);
		data.addDataArray(array);
		data.flip();
		model = new RawModel(data);
		
		
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
		
		model.render();
		
		GL20.glUseProgram(0);
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
