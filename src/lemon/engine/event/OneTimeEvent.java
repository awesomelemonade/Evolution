package lemon.engine.event;

import java.util.ArrayList;
import java.util.List;

public class OneTimeEvent {
    private final List<Runnable> listeners = new ArrayList<>();
    public void add(Runnable listener) {
        listeners.add(listener);
    }
    public void callListeners() {
        listeners.forEach(Runnable::run);
        listeners.clear();
    }
}
