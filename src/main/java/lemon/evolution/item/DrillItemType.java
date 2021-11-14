package lemon.evolution.item;

import lemon.evolution.entity.DrillEntity;
import lemon.evolution.world.ControllableEntity;

public enum DrillItemType implements ItemType {
    INSTANCE;

    @Override
    public String getName() {
        return "Drill";
    }

    @Override
    public void use(ControllableEntity player, float power) {
        player.world().entities().add(new DrillEntity(
                player,
                player.location().add(player.vectorDirection().multiply(1f)),
                player.vectorDirection().multiply(1f * power)));
    }

    @Override
    public String guiImagePath() {
        return "/res/inventory_icons/drill.png";
    }
}
