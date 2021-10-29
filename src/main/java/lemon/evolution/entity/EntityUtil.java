package lemon.evolution.entity;

import lemon.engine.game.Player;
import lemon.engine.math.Vector3D;
import lemon.evolution.world.World;

public class EntityUtil {
    public static void generateExplosion(World world, Vector3D position) {
        world.terrain().generateExplosion(position, 3f);
        world.entities().forEach(entity -> {
            if (entity instanceof Player) {
                float strength = Math.min(2f, 10f / entity.position().distanceSquared(position));
                var direction = entity.position().subtract(position);
                if (direction.equals(Vector3D.ZERO)) {
                    direction = Vector3D.ofRandomUnitVector();
                }
                entity.mutableVelocity().add(direction.scaleToLength(strength));
            }
        });
    }
}
