package lemon.engine.render;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.math.Box2D;
import lemon.engine.math.Matrix;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.toolbox.Color;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class CommonRenderables {
	public static void renderQuad2D(Box2D box, Color color) {
		CommonPrograms2D.COLOR.use(program -> {
			boxToMatrix(box, matrix -> {
				program.loadColor4f("filterColor", color);
				program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrix);
				CommonDrawables.COLORED_QUAD.draw();
			});
		});
	}
	
	public static void renderQuad2D(Box2D box, Color color, float rotation) {
		CommonPrograms2D.COLOR.use(program -> {
			try (var translationMatrix = MatrixPool.ofTranslation(box.x() + box.width() / 2f, box.y() + box.height() / 2f, 0f);
				 var rotationMatrix = MatrixPool.ofRotationZ(rotation);
				 var scalarMatrix = MatrixPool.ofScalar(box.width() / 2f, box.height() / 2f, 1f);
				 var matrix = MatrixPool.ofMultiplied(translationMatrix, rotationMatrix);
				 var transformationMatrix = MatrixPool.ofMultiplied(matrix, scalarMatrix)) {
				program.loadColor4f("filterColor", color);
				program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, transformationMatrix);
				CommonDrawables.COLORED_QUAD.draw();
			}
		});
	}

	public static void renderTexturedQuad2D(Box2D box, Texture texture) {
		CommonPrograms2D.TEXTURE.use(program -> {
			boxToMatrix(box, matrix -> {
				program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrix);
				texture.bind(GL11.GL_TEXTURE_2D, CommonDrawables.TEXTURED_QUAD::draw);
			});
		});
	}

	public static void renderTexturedQuad2D(Box2D box, TextureBank textureBank) {
		CommonPrograms2D.TEXTURE.use(program -> {
			boxToMatrix(box, matrix -> {
				program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrix);
				program.loadSampler("textureSampler", textureBank);
				CommonDrawables.TEXTURED_QUAD.draw();
				program.loadSampler("textureSampler", TextureBank.REUSE);
			});
		});
	}

	private static void boxToMatrix(Box2D box, Consumer<Matrix> consumer) {
		try (var translationMatrix = MatrixPool.ofTranslation(box.x() + box.width() / 2f, box.y() + box.height() / 2f, 0f);
			 var scalarMatrix = MatrixPool.ofScalar(box.width() / 2f, box.height() / 2f, 1f);
			 var matrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
			consumer.accept(matrix);
		}
	}
}
