package lemon.evolution.item;

import lemon.evolution.world.ControllableEntity;

public enum JetpackItemType implements ItemType {
    INSTANCE;

    @Override
    public String getName() {
        return "Jetpack";
    }

    @Override
    public void use(ControllableEntity player, float power) {
        System.out.println("Jetpack used");
    }

    @Override
    public String guiImagePath() {
        return "/res/inventory_icons/jetpack.png";
    }
}
