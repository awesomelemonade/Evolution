package lemon.engine.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;

public class ShaderProgram implements Listener {
	private int id;
	public ShaderProgram(int[] indices, String[] names, Shader... shaders){
		if(indices.length!=names.length){
			throw new IllegalArgumentException("Indices and Name Arrays have different size: "+indices.length+" - "+names.length);
		}
		id = GL20.glCreateProgram();
		if(shaders!=null){
			for(Shader shader: shaders){
				GL20.glAttachShader(id, shader.getId());
			}
		}
		for(int i=0;i<indices.length;++i){
			GL20.glBindAttribLocation(id, indices[i], names[i]);
		}
		GL20.glLinkProgram(id);
		if(GL20.glGetProgrami(id, GL20.GL_LINK_STATUS)==GL11.GL_FALSE){
			throw new IllegalArgumentException("Shader Program Link Fail: "+GL20.glGetShaderInfoLog(id));
		}
		if(shaders!=null){
			for(Shader shader: shaders){
				GL20.glDetachShader(id, shader.getId());
				GL20.glDeleteShader(shader.getId());
			}
		}
		GL20.glValidateProgram(id);
		if(GL20.glGetProgrami(id, GL20.GL_VALIDATE_STATUS)==GL11.GL_FALSE){
			throw new IllegalArgumentException("Shader Program Validation Fail: "+GL20.glGetShaderInfoLog(id));
		}
		EventManager.INSTANCE.registerListener(this);
	}
	public UniformVariable getUniformVariable(String name){
		return new UniformVariable(GL20.glGetUniformLocation(id, name), name);
	}
	@Subscribe
	public void cleanUp(){
		GL20.glDeleteProgram(id);
	}
	public int getId(){
		return id;
	}
}
