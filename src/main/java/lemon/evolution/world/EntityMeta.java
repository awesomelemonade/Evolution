package lemon.evolution.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EntityMeta {
    private final Map<String, Object> metamap = new HashMap<>();

    public void set(String key, Object obj) {
        metamap.put(key, obj);
    }

    public <T> Optional<T> get(String key, Class<T> clazz) {
        return Optional.ofNullable(metamap.get(key)).map(clazz::cast);
    }
}
