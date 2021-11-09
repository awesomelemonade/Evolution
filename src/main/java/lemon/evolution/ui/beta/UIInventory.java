package lemon.evolution.ui.beta;

import lemon.engine.event.Observable;
import lemon.engine.math.Box2D;
import lemon.engine.render.Renderable;
import lemon.engine.texture.Texture;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.item.ItemType;
import lemon.evolution.world.Inventory;

import java.util.HashMap;
import java.util.Map;


public class UIInventory extends AbstractUIComponent implements Disposable {
    private static final int SIDE_LENGTH = 500;
    private static final int START_X = 50;
    private static final int START_Y = 50;
    private static final int ITEM_MARGIN = 15;
    private static final int IMAGE_WIDTH = 95;
    private static final int IMAGE_HEIGHT = 95;
    private final Observable<Inventory> inventory;
    private final Map<ItemType, Texture> itemTextures = new HashMap<>();
    private final Texture highlighterTexture;

    public UIInventory(UIComponent parent, Inventory initialInventory) {
        super(parent);
        this.inventory = new Observable<>(initialInventory);
        children().add(new UIImage(this, new Box2D(START_X, START_Y, SIDE_LENGTH, SIDE_LENGTH), "/res/inventory_icons/inventory.png"));
        highlighterTexture = new Texture("/res/inventory_icons/Highlighter.png", true);
        var disposeOnInventoryChange = new Disposables();
        var disposeOnItemsChange = new Disposables();
        disposables.add(this.inventory.onChangeAndRun(inventory -> {
            disposeOnInventoryChange.dispose();
            disposeOnItemsChange.dispose();
            var items = inventory.itemsList();
            disposeOnInventoryChange.add(items.onChangeAndRun(() -> {
                disposeOnItemsChange.dispose();
                items.forEachWithIndex((index, item) -> {
                    int xPos = START_X + ITEM_MARGIN + (SIDE_LENGTH - 2 * ITEM_MARGIN) / 4 * (index % 4) + 8;
                    int yPos = START_Y + ITEM_MARGIN + (SIDE_LENGTH - 2 * ITEM_MARGIN) / 4 * (3 - index / 4) + 13;
                    var highlighter = disposeOnItemsChange.add(new UIImage(this,
                            new Box2D(xPos, yPos, IMAGE_WIDTH, IMAGE_HEIGHT),
                            highlighterTexture));
                    disposeOnItemsChange.add(inventory.observableCurrentItem().onChangeAndRun(optionalItem -> {
                        highlighter.visible().setValue(optionalItem.map(item::equals).orElse(false));
                    }));
                    // Add for rendering
                    children().add(disposeOnItemsChange.add(new UIImage(this,
                            new Box2D(xPos, yPos, IMAGE_WIDTH, IMAGE_HEIGHT),
                            itemTextures.computeIfAbsent(item, x -> new Texture(x.guiImagePath(), true)),
                            uiImage -> {
                                if (isVisible()) {
                                    inventory.setCurrentItem(item);
                                }
                            })));
                    children().add(highlighter);
                });
            }));
        }));
    }

    @Override
    public void render() {
        if (isVisible()) {
            children().forEach(Renderable::render);
        }
    }

    public void setInventory(Inventory inventory) {
        this.inventory.setValue(inventory);
    }

    @Override
    public void dispose() {
        disposables.dispose();
    }
}
