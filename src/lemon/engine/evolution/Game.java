package lemon.engine.evolution;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.InitEvent;
import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.math.Matrix;
import lemon.engine.render.AttributePointer;
import lemon.engine.render.DataArray;
import lemon.engine.render.ModelDataBuffer;
import lemon.engine.render.RawModel;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.render.UniformVariable;
import lemon.engine.toolbox.Toolbox;

public enum Game implements Listener {
	INSTANCE;
	
	private ShaderProgram program;
	private UniformVariable uniform_modelMatrix;
	private UniformVariable uniform_viewMatrix;
	private UniformVariable uniform_projectionMatrix;
	
	private RawModel model;
	
	@Subscribe
	public void init(InitEvent event){
		ModelDataBuffer data = new ModelDataBuffer(6);
		data.addIndices(0, 1, 2, 0, 3, 2);
		DataArray array = new DataArray(4*7,
				new AttributePointer(0, 3, 7*4, 0),
				new AttributePointer(1, 4, 7*4, 3*4)
		);
		array.addData(-0.5f, -0.5f, 0f, (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
		array.addData(0.5f, -0.5f, 0f, (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
		array.addData(0.5f, 0.5f, 0f, (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
		array.addData(-0.5f, 0.5f, 0f, (float)Math.random(), (float)Math.random(), (float)Math.random(), (float)Math.random());
		data.addDataArray(array);
		data.flip();
		model = new RawModel(data);
		
		
		program = new ShaderProgram(
			new int[]{0, 1},
			new String[]{"position", "color"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/vertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/fragmentShader"))
		);
		uniform_modelMatrix = program.getUniformVariable("modelMatrix");
		uniform_viewMatrix = program.getUniformVariable("viewMatrix");
		uniform_projectionMatrix = program.getUniformVariable("projectionMatrix");
		GL20.glUseProgram(program.getId());
		uniform_modelMatrix.loadMatrix(Matrix.getIdentity(4));
		uniform_viewMatrix.loadMatrix(Matrix.getIdentity(4));
		uniform_projectionMatrix.loadMatrix(Matrix.getIdentity(4));
		GL20.glUseProgram(0);
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
