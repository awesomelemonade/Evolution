package lemon.evolution.world;

import lemon.engine.render.Renderable;
import lemon.futility.FilterableFSetWithEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntityRenderer implements Renderable {
	private final List<Runnable> renderers = new ArrayList<>();
	private final FilterableFSetWithEvents<Entity> entities;

	public EntityRenderer(FilterableFSetWithEvents<Entity> entities) {
		this.entities = entities;
	}

	public <T extends Entity> void registerIndividual(Class<T> clazz, Predicate<? super T> predicate, Consumer<? super T> renderer) {
		registerIndividual(clazz, entity -> {
			if (predicate.test(entity)) {
				renderer.accept(entity);
			}
		});
	}

	public <T extends Entity> void registerIndividual(Class<T> clazz, Consumer<? super T> renderer) {
		var filtered = entities.ofFiltered(clazz);
		renderers.add(() -> filtered.forEach(renderer));
	}

	public <T extends Entity> void registerCollection(Class<T> clazz, Consumer<Collection<T>> consumer) {
		var filtered = entities.ofFiltered(clazz);
		renderers.add(() -> consumer.accept(filtered));
	}

	@Override
	public void render() {
		renderers.forEach(Runnable::run);
	}
}
