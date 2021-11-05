package lemon.evolution.entity;

import lemon.engine.math.Vector3D;
import lemon.evolution.world.ControllableEntity;
import lemon.evolution.world.Entity;
import lemon.evolution.world.Location;

import java.util.function.BiFunction;

public class EntityUtil {
    public static void newLaunchedEntity(ControllableEntity player, float power, BiFunction<Location, Vector3D, Entity> constructor) {
        player.world().entities().add(constructor.apply(
                getPlayerOffset(player, 0.5f),
                player.vectorDirection().multiply(5f * power)
        ));
    }

    public static Location getPlayerOffset(ControllableEntity player, float offset) {
        return player.location().add(player.vectorDirection().multiply(offset));
    }
}
