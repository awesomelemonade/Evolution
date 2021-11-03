package lemon.evolution.world;

import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.futility.FSetWithEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class EntityRenderer implements Renderable, Disposable {
	private final Disposables disposables = new Disposables();
	private final List<Runnable> renderers = new ArrayList<>();
	private final FSetWithEvents<Entity> entities;

	public EntityRenderer(FSetWithEvents<Entity> entities) {
		this.entities = entities;
	}

	public <T extends Entity> void registerIndividual(Class<T> clazz, Consumer<? super T> renderer) {
		var filtered = entities.ofFiltered(clazz, disposables::add);
		renderers.add(() -> filtered.forEach(renderer));
	}

	public <T extends Entity> void registerCollection(Class<T> clazz, Consumer<Collection<T>> consumer) {
		var filtered = entities.ofFiltered(clazz, disposables::add);
		renderers.add(() -> consumer.accept(filtered));
	}

	@Override
	public void render() {
		renderers.forEach(Runnable::run);
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
