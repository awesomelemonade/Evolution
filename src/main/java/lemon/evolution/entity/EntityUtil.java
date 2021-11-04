package lemon.evolution.entity;

import lemon.engine.game.Player;
import lemon.engine.math.Vector3D;
import lemon.evolution.world.ControllableEntity;
import lemon.evolution.world.Entity;
import lemon.evolution.world.Location;
import lemon.evolution.world.World;

import java.util.function.BiFunction;

public class EntityUtil {
    public static void generateExplosion(World world, Vector3D position, float radius) {
        world.terrain().generateExplosion(position, radius);
        world.entities().forEach(entity -> {
            if (entity instanceof Player player) {
                float strength = Math.min(1f, 3f * radius / entity.position().distanceSquared(position));
                var direction = entity.position().subtract(position);
                if (direction.equals(Vector3D.ZERO)) {
                    direction = Vector3D.ofRandomUnitVector();
                }
                entity.mutableVelocity().add(direction.scaleToLength(strength));
                player.health().setValue(player.health().getValue() - strength * 20f);
            }
        });
    }

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
