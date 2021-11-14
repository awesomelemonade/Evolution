package lemon.evolution.item;

import lemon.evolution.entity.EntityUtil;
import lemon.evolution.entity.RainmakerEntity;
import lemon.evolution.entity.TeleportBallEntity;
import lemon.evolution.world.ControllableEntity;

public enum TeleportBallItem implements ItemType {
    INSTANCE;

    @Override
    public String getName() {
        return "Teleport Ball";
    }

    @Override
    public void use(ControllableEntity player, float power) {
        EntityUtil.newLaunchedEntity(player, power / 2f, (location, velocity) -> new TeleportBallEntity(location, velocity, player));
    }

    @Override
    //stand in
    public String guiImagePath() {
        return "/res/inventory_icons/missile.png";
    }
}