package lemon.evolution.ui.beta;

import lemon.engine.draw.TextModel;
import lemon.engine.font.CommonFonts;
import lemon.engine.math.Box2D;
import lemon.engine.math.Vector2D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.Color;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms2D;

public class UIText extends AbstractUIComponent {
    private final Vector2D position;
    private final float scale;
    private final String text;
    private final TextModel model;

    public UIText(UIComponent parent, String text, Vector2D position, float scale) {
        super(parent);
        this.text = text;
        this.model = new TextModel(CommonFonts.freeSans(), text);
        this.position = position;
        this.scale = scale;
    }

    @Override
    public void render() {
        CommonRenderables.renderQuad2D(new Box2D(position.x(), position.y(), model.width(), model.height()), Color.WHITE);
        CommonPrograms2D.TEXT.use(program -> {
            try (var translationMatrix = MatrixPool.ofTranslation(position.x(), position.y(), 0f);
                 var scalarMatrix = MatrixPool.ofScalar(scale, scale, 1f);
                 var transformationMatrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
                program.loadMatrix(MatrixType.MODEL_MATRIX, transformationMatrix);
            }
            program.loadColor3f("color", Color.RED);
            model.draw();
        });
    }
}
