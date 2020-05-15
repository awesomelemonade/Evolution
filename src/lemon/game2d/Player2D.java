package lemon.game2d;

import org.lwjgl.opengl.GL20;

import lemon.engine.game2d.Quad2D;
import lemon.engine.math.Box2D;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.Color;
import lemon.evolution.util.CommonPrograms2D;

public class Player2D {
	private static final float BASE_MASS = 1f;
	private Vector3D position;
	private Vector3D velocity;
	private Quad2D quad;
	
	public Player2D() {
		position = new Vector3D();
		velocity = new Vector3D();
		quad = new Quad2D(new Box2D(-0.5f, -0.5f, 1f, 1f), Color.RED);
	}
	public void render() {
		position.add(new Vector3D(0f, -0.01f, 0f));
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.TRANSFORMATION_MATRIX, MathUtil.getTranslation(position));
		quad.render();
		GL20.glUseProgram(0);
	}
	public Vector3D getPosition() {
		return position;
	}
	public Vector3D getVelocity() {
		return velocity;
	}
	public float getMass() {
		return BASE_MASS;
	}
}
