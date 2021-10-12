package lemon.evolution.ui.beta;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.Box2D;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class UIInventory extends AbstractUIComponent {
    // draw rectangle
    private Box2D box;
    private Color color;

    private List<UIButton> buttons;

    public UIInventory(UIComponent parent) {
        super(parent);
        color = Color.WHITE;
        buttons = new ArrayList<>();
        box = new Box2D(100, 100, 1000, 1000);
        buttons.add(new UIButton(this, new Box2D(200,200,100,100),Color.RED,x -> {
            System.out.println("inventory clicked");
        }));
    }

    @Override
    public void render() {
        CommonPrograms2D.COLOR.use(program -> {
            try (var translationMatrix = MatrixPool.ofTranslation(box.x() + box.width() / 2f, box.y() + box.height() / 2f, 0f);
                 var scalarMatrix = MatrixPool.ofScalar(box.width() / 2f, box.height() / 2f, 1f);
                 var matrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
                program.loadColor4f("filterColor", color);
                program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrix);
                CommonDrawables.COLORED_QUAD.draw();
                program.loadColor4f("filterColor", Color.WHITE);
            }
        });
        for (UIButton button : buttons) {
            button.render();
        }
    }

    public void addItem() {

    }
}
