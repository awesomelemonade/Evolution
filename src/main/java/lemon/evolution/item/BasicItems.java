package lemon.evolution.item;

import lemon.engine.math.Vector3D;
import lemon.evolution.entity.EntityUtil;
import lemon.evolution.entity.ExplodeOnHitProjectile;
import lemon.evolution.entity.ExplodeOnTimeProjectile;
import lemon.evolution.entity.MissileShowerEntity;
import lemon.evolution.world.ControllableEntity;

import java.util.function.BiConsumer;

public enum BasicItems implements ItemType {
    ROCKET_LAUNCHER("Rocket Launcher", "/res/inventory_icons/rocketLauncher.png", (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power, (location, velocity) ->
                new ExplodeOnHitProjectile(location, velocity, ExplodeOnHitProjectile.Type.MISSILE));
    }),
    MISSILE_SHOWER("Missile Shower", "/res/inventory_icons/missile.png", (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power, MissileShowerEntity::new);
    }),
    GRENADE_LAUNCHER("Grenade Launcher", "/res/inventory_icons/rocketLauncher.png", (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power / 3f, (location, velocity) ->
                new ExplodeOnTimeProjectile(location, velocity, ExplodeOnTimeProjectile.Type.GRENADE));
    });
    private final String name;
    private final String guiImagePath;
    private final BiConsumer<ControllableEntity, Float> useAction;

    private BasicItems(String name, String guiImagePath, BiConsumer<ControllableEntity, Float> useAction) {
        this.name = name;
        this.guiImagePath = guiImagePath;
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

    @Override
    public String guiImagePath() {
        return guiImagePath;
    }
}
