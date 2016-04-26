package lemon.engine.control;

import lemon.engine.event.Event;

public interface UpdateEvent extends Event {
	public long getDelta();
}
