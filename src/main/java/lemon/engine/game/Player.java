package lemon.engine.game;

import lemon.engine.event.Observable;
import lemon.engine.math.Camera;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.item.*;
import lemon.evolution.item.ItemType;
import lemon.evolution.item.MissileShowerItemType;
import lemon.evolution.item.RocketLauncherItemType;
import lemon.evolution.physics.beta.CollisionResponse;
import lemon.evolution.world.AbstractControllableEntity;
import lemon.evolution.world.Inventory;
import lemon.evolution.world.Location;

import java.util.Optional;

public class Player extends AbstractControllableEntity implements Disposable {
	private static final float VOID_Y_COORDINATE = -100f;
	private static final float START_HEALTH = 100f;
	private final Disposables disposables = new Disposables();
	private final String name;
	private final Camera camera;
	private final Observable<Float> health = new Observable<>(START_HEALTH);
	private final Observable<Boolean> alive;
	private final Inventory inventory = new Inventory();
	/*private final Observable<Optional<ItemType>> currentItem = new Observable<>(Optional.empty());*/

	public Player(String name, Location location, Projection projection) {
		super(location, Vector3D.ZERO);
		this.name = name;
		this.camera = new Camera(mutablePosition(), mutableRotation(), projection);
		disposables.add(onUpdate().add(() -> {
			if (position().y() < VOID_Y_COORDINATE) {
				world().entities().remove(this);
			}
		}));
		disposables.add(health.onChange(newHealth -> newHealth <= 0f, () -> world().entities().remove(this)));
		this.alive = world().entities().observableContains(this, disposables::add);
		disposables.add(inventory.items().onFallToZero(item -> {
			inventory.currentItem().ifPresent(current -> {
				if (current.equals(item)) {
					inventory.clearCurrentItem();
				}
			});
		}));
		// TODO: Temporary
		inventory.addAndSetCurrentItem(MissileShowerItemType.INSTANCE);
		inventory.addItem(RocketLauncherItemType.INSTANCE);
		inventory.addItem(PenguinGunItemType.INSTANCE);
		inventory.addItem(DrillItemType.INSTANCE);
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

	/*public void addAndSetCurrentItem(ItemType item) {
		inventory.items().add(item);
		currentItem.setValue(Optional.of(item));
	}

	public void setCurrentItem(ItemType item) {
		if (inventory.items().contains(item)) {
			currentItem.setValue(Optional.of(item));
		} else {
			throw new IllegalStateException();
		}
	}

	public void clearCurrentItem() {
		currentItem.setValue(Optional.empty());
	}

	public Optional<ItemType> currentItem() {
		return currentItem.getValue();
	}

	public void useCurrentItem() {
		this.currentItem.getValue().ifPresent(item -> {
			inventory.items().remove(item);
		});
	}*/

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
