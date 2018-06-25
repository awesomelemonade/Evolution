package lemon.engine.render;

import lemon.engine.control.CleanUpEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class VertexArray implements Listener {
	private int id;
	private List<VertexBuffer> vbos;

	public VertexArray() {
		id = GL30.glGenVertexArrays();
		vbos = new ArrayList<VertexBuffer>();
		EventManager.INSTANCE.registerListener(this);
	}
	public int getId() {
		return id;
	}
	public VertexBuffer generateVbo() {
		VertexBuffer vbo = new VertexBuffer();
		vbos.add(vbo);
		return vbo;
	}
	public List<VertexBuffer> getVbos() {
		return Collections.unmodifiableList(vbos);
	}
	@Subscribe
	public void cleanUp(CleanUpEvent event) {
		GL30.glDeleteVertexArrays(id);
		for (VertexBuffer vbo : vbos) {
			GL15.glDeleteBuffers(vbo.getId());
		}
	}
}
