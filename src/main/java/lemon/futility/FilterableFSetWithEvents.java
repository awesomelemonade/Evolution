package lemon.futility;

import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FilterableFSetWithEvents<T> implements Disposable {
    private final Disposables disposables = new Disposables();
    private final FSetWithEvents<T> backingSet;
    private final Map<Class<?>, Set<?>> cachedSets = new HashMap<>();

    public FilterableFSetWithEvents(FSetWithEvents<T> backingSet) {
        this.backingSet = backingSet;
    }

    @SuppressWarnings("unchecked")
    public <U> Set<U> ofFiltered(Class<U> clazz) {
        return (Set<U>) cachedSets.computeIfAbsent(clazz, key -> backingSet.ofFiltered(key, disposables::add));
    }

    @Override
    public void dispose() {
        disposables.dispose();
    }
}
