package lemon.engine.entity;

import lemon.engine.render.VertexArray;

public interface Renderable extends Component {
	public VertexArray getVertexArray();
	public int getVertices();
}
