package lemon.evolution.item;

import lemon.evolution.entity.*;
import lemon.evolution.world.ControllableEntity;

import java.util.function.BiConsumer;

public enum BasicItems implements ItemType {
    ROCKET_LAUNCHER("Rocket Launcher", "/res/inventory_icons/rocketLauncher.png", (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power, (location, velocity) ->
                new ExplodeOnHitProjectile(location, velocity, ExplodeType.MISSILE));
    }),
    MISSILE_SHOWER("Missile Shower", "/res/inventory_icons/missile.png", (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power, MissileShowerEntity::new);
    }),
    GRENADE_LAUNCHER("Grenade Launcher", "/res/inventory_icons/rocketLauncher.png", (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power / 3f, (location, velocity) ->
                new ExplodeOnTimeProjectile(location, velocity, ExplodeType.GRENADE));
    }),
    RAINMAKER("Rainmaker", "/res/inventory_icons/rainMaker.png", (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power * 1.5f, RainmakerEntity::new);
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
