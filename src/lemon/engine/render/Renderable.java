package lemon.engine.render;

import lemon.engine.control.Initializable;

public interface Renderable extends Initializable {
	public VertexArray getVertexArray();
	public int getIndices();
}
