package lemon.engine.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Shader {
	private int id;
	private int type;
	public Shader(int type, CharSequence sequence){
		this.type = type;
		id = GL20.glCreateShader(type);
		GL20.glShaderSource(id, sequence);
		GL20.glCompileShader(id);
		if(GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS)==GL11.GL_FALSE){
			throw new IllegalStateException("Failed to Compile Shader: "+GL20.glGetShaderInfoLog(id));
		}
	}
	public int getId(){
		return id;
	}
	public int getType(){
		return type;
	}
}
