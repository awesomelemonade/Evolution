package lemon.evolution.item;

import lemon.engine.game.Player;
import lemon.engine.math.Vector3D;
import lemon.evolution.entity.ExplodeOnHitProjectile;
import lemon.evolution.entity.ExplodeOnTimeProjectile;
import lemon.evolution.entity.MissileShowerEntity;
import lemon.evolution.world.ControllableEntity;
import lemon.evolution.world.Entity;
import lemon.evolution.world.Location;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public enum BasicItems implements ItemType {
    ROCKET_LAUNCHER("Rocket Launcher", (player, power) -> {
        newLaunchedEntity(player, power, (location, velocity) ->
                new ExplodeOnHitProjectile(location, velocity, ExplodeOnHitProjectile.Type.MISSILE));
    }),
    MISSILE_SHOWER("Missile Shower", (player, power) -> {
        newLaunchedEntity(player, power, MissileShowerEntity::new);
    }),
    GRENADE_LAUNCHER("Grenade Launcher", (player, power) -> {
        newLaunchedEntity(player, power, (location, velocity) ->
                new ExplodeOnTimeProjectile(location, velocity, ExplodeOnTimeProjectile.Type.GRENADE));
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

    private static void newLaunchedEntity(ControllableEntity player, float power, BiFunction<Location, Vector3D, Entity> constructor) {
        player.world().entities().add(constructor.apply(
                getPlayerOffset(player, 0.5f),
                player.vectorDirection().multiply(5f * power)
        ));
    }

    private static Location getPlayerOffset(ControllableEntity player, float offset) {
        return player.location().add(player.vectorDirection().multiply(offset));
    }
}
