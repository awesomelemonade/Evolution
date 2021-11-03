package lemon.engine.toolbox;

import lemon.engine.math.Box2D;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.render.MatrixType;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

public class GLState {
	private static final Deque<Box2D> viewportStack = new ArrayDeque<>();
	public static void pushViewport(int x, int y, int width, int height) {
		pushViewport(new Box2D(x, y, width, height));
	}

	public static void pushViewport(Box2D box) {
		viewportStack.push(box);
		setViewport(box);
	}

	public static void popViewport() {
		viewportStack.pop();
		var box = viewportStack.peek();
		if (box != null) {
			setViewport(box);
		}
	}

	private static void setViewport(Box2D box) {
		GL11.glViewport((int) box.x(), (int) box.y(), (int) box.width(), (int) box.height());
		var orthoProjectionMatrix = MathUtil.getOrtho(box.width(), box.height(), -1, 1);
		CommonPrograms2D.setMatrices(MatrixType.PROJECTION_MATRIX, orthoProjectionMatrix);
	}
}
