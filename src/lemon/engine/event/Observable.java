package lemon.engine.event;

public class Observable<T> {
    private EventWith<T> onSet;
    private T value;
    public Observable (T value) {
        this.onSet = new EventWith<>();
        this.value = value;
    }
    public void setValue(T value) {
        this.value = value;
        onSet.callListeners(value);
    }
    public T getValue() {
        return value;
    }
    public EventWith<T> onSet() {
        return onSet;
    }
}
