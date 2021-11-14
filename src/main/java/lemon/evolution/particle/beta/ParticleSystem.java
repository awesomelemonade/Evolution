package lemon.evolution.particle.beta;

import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import lemon.engine.texture.Texture;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import lemon.evolution.util.CommonPrograms3D;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.PriorityQueue;

public class ParticleSystem implements Disposable {
	// [x, y, u, w] - where (x, y) are vertices and (u, w) are textureCoords
	private static final float[] VERTICES = {
			-0.5f, -0.5f, 0f, 0f,
			0.5f, -0.5f, 1f, 0f,
			-0.5f, 0.5f, 0f, 1f,
			0.5f, 0.5f, 1f, 1f
	};
	private static final int FLOATS_PER_PARTICLE = 8;
	private final VertexArray vertexArray;
	private VertexBuffer vertexBuffer;
	private final int maxParticles;
	private final PriorityQueue<Particle> particles = new PriorityQueue<>(Comparator.comparing(Particle::expiryTime));
	private final Texture texture;

	public ParticleSystem(int maxParticles, Texture texture) {
		this.maxParticles = maxParticles;
		this.texture = texture;
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			new VertexBuffer().bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> { // Quad Vertices
				GL15.glBufferData(target, VERTICES, GL15.GL_STATIC_DRAW);
				GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * 4, 0);
				GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * 4, 2 * 4);
			});
			vertexBuffer = new VertexBuffer();
			vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> { // Particle Center [x, y, z] + Size [w] + Color [r, g, b, a]
				GL15.glBufferData(target, getInitialFloatBuffer(), GL15.GL_STREAM_DRAW);
				GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 8 * 4, 0);
				GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, 8 * 4, 4 * 4);
			});
			GL33.glVertexAttribDivisor(0, 0);
			GL33.glVertexAttribDivisor(1, 0);
			GL33.glVertexAttribDivisor(2, 1);
			GL33.glVertexAttribDivisor(3, 1);
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);
			GL20.glEnableVertexAttribArray(3);
		});
	}

	public FloatBuffer getInitialFloatBuffer() {
		var numFloats = FLOATS_PER_PARTICLE * maxParticles;
		var buffer = BufferUtils.createFloatBuffer(numFloats);
		for (int i = 0; i < numFloats; i++) {
			buffer.put(0f);
		}
		buffer.flip();
		return buffer;
	}

	public FloatBuffer getFloatBuffer(Vector3D viewPosition) {
		var particles = this.particles.stream().sorted(
				Comparator.comparingDouble(p -> -p.position().distanceSquared(viewPosition))).toList();
		var buffer = BufferUtils.createFloatBuffer(FLOATS_PER_PARTICLE * particles.size());
		for (Particle particle : particles) {
			particle.position().putInBuffer(buffer);
			buffer.put(particle.size()); // size
			particle.color().putInBuffer(buffer);
		}
		buffer.flip();
		return buffer;
	}

	private static final Vector3D GRAVITY = Vector3D.of(0f, -0.0005f, 0f);

	public void addExplosionParticles(Vector3D position, float size) {
		for (int i = 0; i < 50; i++) {
			var velocity = (float) (Math.random() * size / 5f);
			particles.add(new Particle(ParticleType.FIRE, position, Vector3D.ofRandomUnitVector().multiply(velocity), GRAVITY, Duration.ofSeconds(1), new Color(226f / 255f, 88f / 255f, 34f / 255f), 5f));
		}
	}

	public void render(Vector3D viewPosition) {
		var now = Instant.now();
		while ((!particles.isEmpty()) && particles.peek().expiryTime().isBefore(now)) {
			particles.poll();
		}
		for (Particle particle : particles) {
			particle.update();
		}
		// Update VBO
		vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
			GL15.glBufferSubData(target, 0, getFloatBuffer(viewPosition));
		});
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		CommonPrograms3D.PARTICLE.use(program -> {
			program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
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

	@Override
	public void dispose() {
		vertexBuffer.dispose();
		vertexArray.dispose();
	}
}
