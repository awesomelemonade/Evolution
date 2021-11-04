package lemon.evolution.ui.beta;

import lemon.engine.math.Box2D;
import lemon.evolution.item.BasicItems;
import lemon.evolution.item.ItemType;
import lemon.evolution.world.Inventory;

import java.util.ArrayList;
import java.util.List;

public class UIInventory extends AbstractUIComponent {

    private static final int SIDE_LENGTH = 500;
    private static final int START_X = 800;
    private static final int START_Y = 50;
    private static final int ITEM_MARGIN = 15;
    private Inventory inventory;
    private ItemType[] items;
    private final ItemType defaultMissile = BasicItems.MISSILE_SHOWER;

    // draw rectangle
    private UIImage grid;

    private List<UIImage> images;
    private List<UIImage> highlighters;

    public UIInventory(UIComponent parent) {
        super(parent);
        images = new ArrayList<>();
        highlighters = new ArrayList<>();
        grid = new UIImage(this, new Box2D(START_X, START_Y, SIDE_LENGTH, SIDE_LENGTH), "/res/inventory_icons/inventory.png");
        visible().setValue(false);
    }

    @Override
    public void render() {
        if (isVisible()) {
            grid.render();
            for (UIImage image : images) {
                image.render();
            }
            for (UIImage highlighter : highlighters) {
                highlighter.render();
            }
        }
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
        items = inventory.items().toArray(new ItemType[0]);
        System.out.println("number of items: " + items.length);
        highlighters.clear();
        images.clear();
        int count = 0;
        for (ItemType item : items) {
            System.out.println(item == null ? "item is null" : item.getName());
            int finalCount = count;
            int xPos = START_X + ITEM_MARGIN + (SIDE_LENGTH - 2 * ITEM_MARGIN) / 4 * (count % 4) + 8;
            int yPos = START_Y + ITEM_MARGIN + (SIDE_LENGTH - 2 * ITEM_MARGIN) / 4 * (3 - count / 4) + 13;
            highlighters.add(new UIImage(this,
                    new Box2D(xPos, yPos, 95, 95),
                    "/res/inventory_icons/Highlighter.png"));
            highlighters.get(count).visible().setValue(false);
            images.add(new UIImage(this,
                    new Box2D(xPos, yPos, 95, 95),
                    item.guiImagePath(),
                    x -> {
                        if (isVisible()) {
                            System.out.println("Player clicked on " + item.getName());
                            this.inventory.setCurrentItem(item);
                            System.out.println("current item: " + inventory.currentItem().orElse(item).getName());
                            setHighlighted();
                        }
                    }));
            setHighlighted();
            count++;
        }
    }

    // makes all items not highlighted except current item
    private void setHighlighted() {
        for (int i = 0; i < highlighters.size(); i++) {
            highlighters.get(i).visible().setValue(false);
            if (items[i] == inventory.currentItem().orElse(defaultMissile)) highlighters.get(i).visible().setValue(true);
        }
    }
}
