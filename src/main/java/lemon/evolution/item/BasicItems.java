package lemon.evolution.item;

import lemon.evolution.entity.ExplosiveProjectile;
import lemon.evolution.entity.ExplosiveProjectileType;
import lemon.evolution.entity.MissileShowerEntity;
import lemon.evolution.world.ControllableEntity;
import lemon.evolution.world.Location;

import java.util.function.BiConsumer;

public enum BasicItems implements ItemType {
    ROCKET_LAUNCHER("Rocket Launcher", (player, power) -> {
        player.world().entities().add(new ExplosiveProjectile(
                getPlayerOffset(player, 0.5f),
                player.vectorDirection().multiply(5f * power),
                ExplosiveProjectileType.MISSILE
        ));
    }),
    MISSILE_SHOWER("Missile Shower", (player, power) -> {
        player.world().entities().add(new MissileShowerEntity(
                getPlayerOffset(player, 0.5f),
                player.vectorDirection().multiply(5f * power)
        ));
    });
    private final String name;
    private final BiConsumer<ControllableEntity, Float> useAction;

    private BasicItems(String name, BiConsumer<ControllableEntity, Float> useAction) {
        this.name = name;
        this.useAction = useAction;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void use(ControllableEntity player, float power) {
        useAction.accept(player, power);
    }

    private static Location getPlayerOffset(ControllableEntity player, float offset) {
        return player.location().add(player.vectorDirection().multiply(offset));
    }
}
