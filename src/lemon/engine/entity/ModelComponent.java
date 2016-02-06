package lemon.engine.entity;

import lemon.engine.render.VertexArray;

public interface ModelComponent extends Component {
	public VertexArray getVertexArray();
}
