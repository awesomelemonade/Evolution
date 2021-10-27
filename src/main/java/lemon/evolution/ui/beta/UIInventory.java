package lemon.evolution.ui.beta;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.Box2D;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.TextureData;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.engine.toolbox.Toolbox;
import lemon.evolution.item.ItemType;
import lemon.evolution.item.MissileShowerItemType;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.world.Inventory;
import org.lwjgl.glfw.GLFW;
import lemon.engine.render.CommonRenderables;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class UIInventory extends AbstractUIComponent {

    private static final int WINDOW_HEIGHT = 900 - 200;
    private static final int WINDOW_WIDTH = 1600 - 200;
    private static int numRows = 2;
    private static int numColumns = 4;
    private Inventory inventory;
    private ItemType[] items;

    // draw rectangle
    private Box2D box;
    private Color color;

    private List<UIImage> images;
    private List<UIImage> highlighters;

    public UIInventory(UIComponent parent) {
        super(parent);
        color = Color.WHITE;
        images = new ArrayList<>();
        highlighters = new ArrayList<>();
        box = new Box2D(100, 100, WINDOW_WIDTH,WINDOW_HEIGHT);
        int xOffset = 50; //(WINDOW_WIDTH - (((numColumns * numRows) % numColumns) * WINDOW_WIDTH / numColumns)) / 2;
        int yOffset = 50;
        visible().setValue(false);
    }

    @Override
    public void render() {
        if (isVisible()) {
            CommonPrograms2D.COLOR.use(program -> {
                try (var translationMatrix = MatrixPool.ofTranslation(box.x() + box.width() / 2f, box.y() + box.height() / 2f, 0f);
                     var scalarMatrix = MatrixPool.ofScalar(box.width() / 2f, box.height() / 2f, 1f);
                     var matrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
                    program.loadColor4f("filterColor", color);
                    program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrix);
                    CommonDrawables.COLORED_QUAD.draw();
                    program.loadColor4f("filterColor", color);
                }
            });
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
        int count = 0;
        for (ItemType item : items) {
            System.out.println(item == null ? "item is null" : item.getName());
            int finalCount = count;
            highlighters.add(new UIImage(this, new Box2D(
                    190 + 300 * count, 190, 120, 120),
                    "/res/inventory_icons/rocket_launcher.png"));
            highlighters.get(count).visible().setValue(false);
            images.add(new UIImage(this, new Box2D(
                    200 + 300 * count, 200, 100, 100),
                    item.guiImagePath(),
                    x -> {
                        if (isVisible()) {
                            System.out.println("Player clicked on " + item.getName());
                            this.inventory.setCurrentItem(item);
                            System.out.println("current item: " + inventory.currentItem().orElse(item).getName());
                            for (int i = 0; i < highlighters.size(); i++) {
                                highlighters.get(i).visible().setValue(false);
                                if (i == finalCount) highlighters.get(finalCount).visible().setValue(true);
                            }
                        }
                    }));
            count++;
        }

    }
}
