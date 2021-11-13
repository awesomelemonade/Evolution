package lemon.evolution.entity;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.item.BasicItems;
import lemon.evolution.item.ItemType;
import lemon.evolution.world.AbstractEntity;
import lemon.evolution.world.Location;
import lemon.evolution.world.World;

public class ItemDropEntity extends AbstractEntity implements Disposable {
    private final Disposables disposables = new Disposables();
    private final float radius;
    private final ItemType item;
    private final int quantity;
    private boolean initialDrop = true;

    public ItemDropEntity(Location location) {
        this(location, Vector3D.ZERO, 1f, MathUtil.randomChoice(BasicItems.values()), 1);
    }

    public ItemDropEntity(Location location, Vector3D velocity, float radius, ItemType item, int quantity) {
        super(location, velocity, radius);
        this.radius = radius;
        this.item = item;
        this.quantity = quantity;
        disposables.add(this.onCollide().add(collisionLocation -> initialDrop = false));
        var collectRadius = 1.4f * radius;
        disposables.add(this.onUpdate().add(() -> {
            world().players().stream()
                    .filter(player -> position().isWithinDistanceSquared(player.position(), collectRadius))
                    .findAny().ifPresent(player -> {
                        player.inventory().addItems(this.item, quantity);
                        removeFromWorld();
                    });
        }));
    }

    @Override
    public Vector3D getEnvironmentalForce() {
        if (initialDrop) {
            return World.GRAVITY_VECTOR.add(this.velocity().multiply(World.AIR_FRICTION * 10f));
        } else {
            return super.getEnvironmentalForce();
        }
    }

    public boolean initialDrop() {
        return initialDrop;
    }

    @Override
    public void dispose() {
        disposables.dispose();
    }
}
