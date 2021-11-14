package lemon.engine.game;

import lemon.engine.event.Observable;
import lemon.engine.math.Camera;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.item.BasicItems;
import lemon.evolution.physics.beta.CollisionResponse;
import lemon.evolution.world.AbstractControllableEntity;
import lemon.evolution.world.Inventory;
import lemon.evolution.world.Location;

public class Player extends AbstractControllableEntity implements Disposable {
	public static final float START_HEALTH = 100f;
	private final Disposables disposables = new Disposables();
	private final String name;
	private final Camera camera;
	private final Observable<Float> health = new Observable<>(START_HEALTH);
	private final Observable<Boolean> alive;
	private final Inventory inventory = disposables.add(new Inventory());
	private final Team team;

	public Player(String name, Team team, Location location, Projection projection) {
		super(location, Vector3D.ZERO);
		this.name = name;
		this.team = team;
		this.camera = new Camera(mutablePosition(), mutableRotation(), projection);
		disposables.add(health.onChange(newHealth -> newHealth <= 0f, this::removeFromWorld));
		this.alive = world().entities().observableContains(this, disposables::add);
		disposables.add(this.alive.onChangeTo(false, () -> health.setValue(0f)));
		// Add items
		for (int i = 0; i < 100; i++) {
			inventory.addItem(BasicItems.ROCKET_LAUNCHER);
		}
		inventory.addAndSetCurrentItem(BasicItems.ROCKET_LAUNCHER);
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

	public Observable<Float> health() {
		return health;
	}

	public Observable<Boolean> alive() {
		return alive;
	}

	public Inventory inventory() {
		return inventory;
	}

	public Team team() {
		return team;
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
