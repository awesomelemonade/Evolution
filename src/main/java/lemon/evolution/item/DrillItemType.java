package lemon.evolution.item;

import lemon.evolution.world.ControllableEntity;

public enum DrillItemType implements ItemType {
    INSTANCE;

    @Override
    public String getName() {
        return "Drill";
    }

    @Override
    public void use(ControllableEntity player) {
        System.out.println("Drill gun used");
    }

    @Override
    public String guiImagePath() {
        return "/res/inventory_icons/drill.png";
    }
}
