package lemon.evolution.item;

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
    }

    @Override
    public String guiImagePath() {
        return "/res/inventory_icons/rainMaker.png";
    }
}
