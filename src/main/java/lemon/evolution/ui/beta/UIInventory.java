package lemon.evolution.ui.beta;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.Box2D;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.item.ItemType;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.world.Inventory;
import org.lwjgl.glfw.GLFW;

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

    private List<UIButton> buttons;

    public UIInventory(UIComponent parent) {
        super(parent);
        color = Color.WHITE;
        buttons = new ArrayList<>();
        box = new Box2D(100, 100, WINDOW_WIDTH,WINDOW_HEIGHT);
        int xOffset = 50; //(WINDOW_WIDTH - (((numColumns * numRows) % numColumns) * WINDOW_WIDTH / numColumns)) / 2;
        int yOffset = 50;
        for (int i = 0; i < numColumns * numRows; i++) {
            int num = i;
            int xSpacing = 200 + (i % numColumns) * WINDOW_WIDTH / numColumns;
            int ySpacing = 200 + (i / numColumns) * WINDOW_HEIGHT / numRows;
            buttons.add(new UIButton(this, new Box2D(
                    xOffset + xSpacing,
                    yOffset + ySpacing,
                    100, 100),
                    new Color(i * 100,(i + 1) % numColumns * 100, (i + 2) % numRows * 100),
                    x -> {
                if (isVisible()) {
                    System.out.println("inventory clicked for button " + num);
                }
            }));
        }
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
            for (UIButton button : buttons) {
                button.render();
            }
        }
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
        items = inventory.items().toArray(new ItemType[0]);
    }
}
