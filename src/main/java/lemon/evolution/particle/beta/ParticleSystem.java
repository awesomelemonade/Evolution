package lemon.evolution.particle.beta;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import lemon.engine.texture.Texture;
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
import java.util.Comparator;
import java.util.Deque;

public class ParticleSystem {
	// [x, y, u, w] - where (x, y) are vertices and (u, w) are textureCoords
	private static final float[] VERTICES = {
			-0.5f, -0.5f, 0f, 0f,
			0.5f, -0.5f, 1f, 0f,
			-0.5f, 0.5f, 0f, 1f,
			0.5f, 0.5f, 1f, 1f
	};
	private final VertexArray vertexArray;
	private VertexBuffer vertexBuffer;
	private final int maxParticles;
	private final Deque<Particle> particles;
	private final Texture texture;

	public ParticleSystem(int maxParticles, Texture texture) {
		this.maxParticles = maxParticles;
		this.texture = texture;
		this.particles = new ArrayDeque<>();
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			new VertexBuffer().bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> { // Quad Vertices
				GL15.glBufferData(target, VERTICES, GL15.GL_STATIC_DRAW);
				GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * 4, 0);
				GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * 4, 2 * 4);
			});
			vertexBuffer = new VertexBuffer();
			vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> { // Particle Center [x, y, z] + Size [w]
				GL15.glBufferData(target, getInitialFloatBuffer(), GL15.GL_STREAM_DRAW);
				GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 4 * 4, 0);
			});
			GL33.glVertexAttribDivisor(0, 0);
			GL33.glVertexAttribDivisor(1, 0);
			GL33.glVertexAttribDivisor(2, 1);
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);
		});
	}

	public FloatBuffer getInitialFloatBuffer() {
		var buffer = BufferUtils.createFloatBuffer(4 * maxParticles);
		for (int i = 0; i < maxParticles; i++) {
			Vector3D.ZERO.putInBuffer(buffer); // center
			buffer.put(1f); // size
		}
		buffer.flip();
		return buffer;
	}

	public FloatBuffer getFloatBuffer(Vector3D viewPosition, Vector3D position) {
		var particles = this.particles.stream().sorted(
				Comparator.comparingDouble(p -> -p.position().add(position).distanceSquared(viewPosition))).toList();
		var buffer = BufferUtils.createFloatBuffer(4 * particles.size());
		for (Particle particle : particles) {
			particle.position().putInBuffer(buffer);
			buffer.put(5f); // size
		}
		buffer.flip();
		return buffer;
	}

	private static final Vector3D GRAVITY = Vector3D.of(0f, -0.01f, 0f);

	public void render(Vector3D viewPosition, Vector3D position) {
		while ((!particles.isEmpty()) && particles.peekFirst().getAge().compareTo(Duration.ofSeconds(5)) >= 0) {
			particles.poll();
		}
		if (particles.size() < maxParticles) {
			if (particles.isEmpty() || particles.peekLast().getAge().compareTo(Duration.ofMillis(5)) >= 0) {
				for (int i = 0; i < 50; i++) {
					var velocity = 0.2f;
					particles.add(new Particle(Vector3D.ZERO,
							Vector3D.of((float) (Math.random() * 2f - 1f),
									(float) (Math.random() * 2f - 1f),
									(float) (Math.random() * 2f - 1f)).multiply(velocity)));
				}
			}
		}
		for (Particle particle : particles) {
			particle.mutableVelocity().add(GRAVITY);
			particle.mutableVelocity().multiply(0.95f);
			particle.update();
		}
		// Update VBO
		vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
			GL15.glBufferSubData(target, 0, getFloatBuffer(viewPosition, position));
		});
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		CommonPrograms3D.PARTICLE.use(program -> {
			program.loadColor4f(Color.WHITE);
			program.loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(position));
			texture.bind(GL11.GL_TEXTURE_2D, () -> {
				vertexArray.bind(vao -> {
					GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, 4, particles.size());
				});
			});
		});
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
