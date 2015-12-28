package lemon.engine.entity;

import lemon.engine.control.Initializable;
import lemon.engine.render.Renderable;
import lemon.engine.render.VertexArrayWatcher;

public interface EntityType extends Initializable {
	public String getName();
}
