package lemon.evolution.ui.beta;

import lemon.engine.draw.TextModel;
import lemon.engine.font.CommonFonts;
import lemon.engine.font.Font;
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
    private final Color textColor;
    private final Color backgroundColor;

    public UIText(UIComponent parent, String text, Vector2D position, float scale, Color textColor) {
        this(parent, text, position, scale, textColor, Color.CLEAR);
    }

    public UIText(UIComponent parent, String text, Vector2D position, float scale, Color textColor, Color backgroundColor) {
        this(parent, text, position, scale, new TextModel(CommonFonts.freeSans(), text), textColor, backgroundColor);
    }

    private UIText(UIComponent parent, String text, Vector2D position, float scale, TextModel model, Color textColor, Color backgroundColor) {
        super(parent);
        this.text = text;
        this.position = position;
        this.scale = scale;
        this.model = model;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void render() {
        if (isVisible()) {
            if (!backgroundColor.isClear()) {
                CommonRenderables.renderQuad2D(new Box2D(position.x(), position.y(), model.width(), model.height()), backgroundColor);
            }
            CommonPrograms2D.TEXT.use(program -> {
                try (var translationMatrix = MatrixPool.ofTranslation(position.x(), position.y(), 0f);
                     var scalarMatrix = MatrixPool.ofScalar(scale, scale, 1f);
                     var transformationMatrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
                    program.loadMatrix(MatrixType.MODEL_MATRIX, transformationMatrix);
                }
                program.loadColor3f(textColor);
                model.draw();
            });
        }
    }

    public static UIText ofHeight(UIComponent parent, Font font, String text, Vector2D position, float height, Color textColor) {
        return new UIText(parent, text, position, height / font.getBase(), new TextModel(font, text), textColor, Color.CLEAR);
    }

    public static UIText ofHeightRightAligned(UIComponent parent, Font font, String text, Vector2D position, float height, Color textColor) {
        var model = new TextModel(font, text);
        var scale = height / font.getBase();
        return new UIText(parent, text, position.subtract(Vector2D.of(model.width() * scale, 0f)), scale, model, textColor, Color.CLEAR);
    }

    public static UIText ofCentered(UIComponent parent, Font font, String text, Vector2D position, float scale, Color textColor) {
        return ofCentered(parent, font, text, position, scale, textColor, Color.CLEAR);
    }

    public static UIText ofCentered(UIComponent parent, Font font, String text, Vector2D position, float scale, Color textColor, Color backgroundColor) {
        var model = new TextModel(font, text);
        return new UIText(parent, text, position.subtract(Vector2D.of(model.width() * scale / 2f, 0f)), scale, model, textColor, backgroundColor);
    }
}
