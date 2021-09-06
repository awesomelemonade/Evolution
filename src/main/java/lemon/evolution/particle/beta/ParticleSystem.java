package lemon.evolution.particle.beta;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Renderable;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import lemon.engine.toolbox.Color;
import lemon.evolution.util.CommonPrograms3D;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

public class ParticleSystem implements Renderable {
	private static final float[] VERTICES = {-1f, -1f, -1f, -1f, -1f, 1f, -1f, 1f, -1f, -1f,
			1f, 1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f, 1f, -1f, 1f, 1f, 1f};
	private static final int[] INDICES = {2, 0, 4, 4, 6, 2, 1, 0, 2, 2, 3, 1, 4, 5, 7, 7, 6, 4,
			1, 3, 7, 7, 5, 1, 2, 6, 7, 7, 3, 2, 0, 1, 4, 4, 1, 5};
	private final VertexArray vertexArray;
	private VertexBuffer vertexBuffer;
	private final int maxParticles;
	private final Deque<Particle> particles;

	public ParticleSystem(int maxParticles) {
		this.maxParticles = maxParticles;
		this.particles = new ArrayDeque<>();
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			new VertexBuffer().bind(GL15.GL_ELEMENT_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, INDICES, GL15.GL_STATIC_DRAW);
			}, false);
			new VertexBuffer().bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, VERTICES, GL15.GL_STATIC_DRAW);
				GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * 4, 0);
			});
			vertexBuffer = new VertexBuffer();
			vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, getInitialFloatBuffer(), GL15.GL_STREAM_DRAW);
				GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 16 * 4, 0);
				GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 16 * 4, 4 * 4);
				GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, 16 * 4, 8 * 4);
				GL20.glVertexAttribPointer(4, 4, GL11.GL_FLOAT, false, 16 * 4, 12 * 4);
				GL33.glVertexAttribDivisor(1, 1);
				GL33.glVertexAttribDivisor(2, 1);
				GL33.glVertexAttribDivisor(3, 1);
				GL33.glVertexAttribDivisor(4, 1);
			});
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);
			GL20.glEnableVertexAttribArray(3);
			GL20.glEnableVertexAttribArray(4);
		});
	}

	public void updateVbo() {
		vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
			GL15.glBufferSubData(target, 0, getFloatBuffer());
		});
	}

	public FloatBuffer getInitialFloatBuffer() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16 * maxParticles);
		for (int i = 0; i < maxParticles; i++) {
			Matrix.ZERO_4.addToFloatBuffer(buffer);
		}
		buffer.flip();
		return buffer;
	}

	public FloatBuffer getFloatBuffer() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16 * particles.size());
		for (Particle particle : particles) {
			particle.getTransformationMatrix()
					.multiply(MathUtil.getScalar(Vector3D.of(0.8f, 0.8f, 0.8f))).addToFloatBuffer(buffer);
		}
		buffer.flip();
		return buffer;
	}

	public Vector3D randomVector() {
		return Vector3D.of((float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1));
	}

	public Vector3D randomVelocityVector() {
		return Vector3D.of((float) (Math.random() * 2 - 1), (float) (Math.random() * 5 + 2.0), (float) (Math.random() * 2 - 1));
	}

	private static final Vector3D GRAVITY = Vector3D.of(0f, -0.01f, 0f);

	public void render() {
		while ((!particles.isEmpty()) && particles.peekFirst().getAge().compareTo(Duration.ofSeconds(5)) >= 0) {
			particles.poll();
		}
		if (particles.size() < maxParticles) {
			if (particles.isEmpty() || particles.peekLast().getAge().compareTo(Duration.ofMillis(5)) >= 0) {
				particles.add(new Particle(Vector3D.ZERO,
						Vector3D.of((float) (-Math.random() * 3 - 2),
								(float) ((Math.random() - 0.5) * 1.4),
								(float) ((Math.random() - 0.5) * 1.4)),
						randomVector(), randomVector().multiply(0.05f)));
			}
		}
		for (Particle particle : particles) {
			particle.getTranslationalVelocity().add(GRAVITY);
			particle.getTranslationalVelocity().multiply(0.95f);
			particle.update();
		}
		updateVbo();
		CommonPrograms3D.PARTICLE.getShaderProgram().use(program -> {
			program.loadColor4f(Color.RED);
			program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(Vector3D.of(65f, 98f, 0f)));
			vertexArray.bind(vao -> {
				GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, INDICES.length, GL11.GL_UNSIGNED_INT, 0, particles.size());
			});
		});
	}
}
