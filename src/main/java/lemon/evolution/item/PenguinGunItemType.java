package lemon.evolution.item;

import lemon.evolution.world.ControllableEntity;

public enum PenguinGunItemType implements ItemType {
    INSTANCE;

    @Override
    public String getName() {
        return "Penguin Gun";
    }

    @Override
    public void use(ControllableEntity player) {
        System.out.println("Penguin Gun used");
    }

    @Override
    public String guiImagePath() {
        return "/res/inventory_icons/penguin.png";
    }
}
