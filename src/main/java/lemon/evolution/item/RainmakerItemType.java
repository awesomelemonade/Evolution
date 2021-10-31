package lemon.evolution.item;

import lemon.evolution.entity.EntityUtil;
import lemon.evolution.entity.RainmakerEntity;
import lemon.evolution.world.ControllableEntity;

public enum RainmakerItemType implements ItemType {
    INSTANCE;

    @Override
    public String getName() {
        return "Rainmaker";
    }

    @Override
    public void use(ControllableEntity player, float power) {
        System.out.println("Rainmaker used.");
        EntityUtil.newLaunchedEntity(player, power * 1.5f, RainmakerEntity::new);
    }

    @Override
    public String guiImagePath() {
        return "/res/inventory_icons/rainMaker.png";
    }
}
