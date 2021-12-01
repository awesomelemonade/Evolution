package lemon.evolution.item;

import lemon.evolution.entity.*;
import lemon.evolution.world.ControllableEntity;

import java.util.function.BiConsumer;

public enum BasicItems implements ItemType {
    ROCKET_LAUNCHER("Rocket Launcher", "/res/inventory_icons/rocketLauncher.png", true, (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power, (location, velocity) ->
                new ExplodeOnHitProjectile(location, velocity, ExplodeType.MISSILE));
    }),
    MISSILE_SHOWER("Missile Shower", "/res/inventory_icons/missile.png", true, (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power, MissileShowerEntity::new);
    }),
    GRENADE_LAUNCHER("Grenade Launcher", "/res/inventory_icons/grenade.png", true, (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power / 3f, (location, velocity) ->
                new ExplodeOnTimeProjectile(location, velocity, ExplodeType.GRENADE));
    }),
    RAINMAKER("Rainmaker", "/res/inventory_icons/rainMaker.png", true, (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power * 1.5f, RainmakerEntity::new);
    }),
    TELEPORT_BALL("Teleport Ball", "/res/inventory_icons/enderpearl.png", false, (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power / 2f, (location, velocity) -> new TeleportBallEntity(location, velocity, player));
    }),
    DRILL("Drill", "/res/inventory_icons/drill.png", true, (player, power) -> {
        player.world().entities().add(new DrillEntity(
                player,
                player.location().add(player.vectorDirection()),
                player.vectorDirection().multiply(power)));
    }),
    NUKE("Nuke", "/res/inventory_icons/nuke.png", true, (player, power) -> {
        EntityUtil.newLaunchedEntity(player, power, (location, velocity) ->
                new ExplodeOnHitProjectile(location, velocity, ExplodeType.NUKE));
    });
    private final String name;
    private final String guiImagePath;
    private final BiConsumer<ControllableEntity, Float> useAction;
    private final boolean isWeapon;

    private BasicItems(String name, String guiImagePath, boolean isWeapon, BiConsumer<ControllableEntity, Float> useAction) {
        this.name = name;
        this.guiImagePath = guiImagePath;
        this.useAction = useAction;
        this.isWeapon = isWeapon;
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

    @Override
    public boolean isWeapon() {
        return isWeapon;
    }
}
