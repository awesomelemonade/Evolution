package lemon.engine.texture;

import org.lwjgl.opengl.GL13;

public enum TextureBank {
	REUSE(0), COLOR(1), DEPTH(2), SKYBOX(3);
	private final int id;
	private TextureBank(int id){
		this.id = id;
	}
	public int getId(){
		return id;
	}
	public int getBind(){
		return GL13.GL_TEXTURE0+id;
	}
}
