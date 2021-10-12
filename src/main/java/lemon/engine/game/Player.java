package lemon.engine.game;

import lemon.engine.event.Observable;
import lemon.engine.math.Camera;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.physics.beta.CollisionResponse;
import lemon.evolution.world.AbstractControllableEntity;
import lemon.evolution.world.Location;

public class Player extends AbstractControllableEntity implements Disposable {
	private final Disposables disposables = new Disposables();
	private final String name;
	private final Camera camera;
	private final Observable<Boolean> alive;

	public Player(String name, Location location, Projection projection) {
		super(location, Vector3D.ZERO);
		this.name = name;
		this.camera = new Camera(mutablePosition(), mutableRotation(), projection);
		disposables.add(onUpdate().add(() -> {
			if (position().y() < -250f) {
				world().entities().remove(this);
			}
		}));
		this.alive = world().entities().observableContains(this, disposables::add);
	}

	@Override
	public CollisionResponse getCollisionResponse() {
		return CollisionResponse.SLIDE;
	}

	public String name() {
		return name;
	}

	public Camera camera() {
		return camera;
	}

	public Observable<Boolean> alive() {
		return alive;
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
