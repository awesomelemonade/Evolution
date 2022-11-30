package lemon.evolution.fastspheres;

import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.model.SphereModelBuilder;
import lemon.engine.render.MatrixType;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
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
import java.util.*;

public class FastSpheres implements Disposable {
    private static final Vector3D[] VERTICES = SphereModelBuilder.of(1f, 5).build((indices, vertices) -> {
        return Arrays.stream(indices).mapToObj(i -> vertices[i]).toArray(Vector3D[]::new);
    });
    private static final float[] VERTICES_FLOATS;
    static {
        VERTICES_FLOATS = new float[VERTICES.length * 3];
        for (int i = 0; i < VERTICES.length; i++) {
            VERTICES_FLOATS[i * 3] = VERTICES[i].x();
            VERTICES_FLOATS[i * 3 + 1] = VERTICES[i].y();
            VERTICES_FLOATS[i * 3 + 2] = VERTICES[i].z();
        }
    }
    private static final int FLOATS_PER_PARTICLE = 8;
    private final VertexArray vertexArray;
    private VertexBuffer vertexBuffer;
    private final int maxSpheres;
    public final List<FastSphere> spheres = new ArrayList<>();

    public FastSpheres(int maxSpheres) {
        spheres.add(new FastSphere(Vector3D.ZERO, Color.BLUE)); // TODO
        this.maxSpheres = maxSpheres;
        vertexArray = new VertexArray();
        vertexArray.bind(vao -> {
            new VertexBuffer().bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> { // Quad Vertices
                GL15.glBufferData(target, VERTICES_FLOATS, GL15.GL_STATIC_DRAW);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * 4, 0);
            });
            vertexBuffer = new VertexBuffer();
            vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> { // Particle Center [x, y, z] + Size [w] + Color [r, g, b, a]
                GL15.glBufferData(target, getInitialFloatBuffer(), GL15.GL_STREAM_DRAW);
                GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 8 * 4, 0);
                GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 8 * 4, 4 * 4);
            });
            GL33.glVertexAttribDivisor(0, 0);
            GL33.glVertexAttribDivisor(1, 1);
            GL33.glVertexAttribDivisor(2, 1);
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);
        });
    }

    public FloatBuffer getInitialFloatBuffer() {
        var numFloats = FLOATS_PER_PARTICLE * maxSpheres;
        var buffer = BufferUtils.createFloatBuffer(numFloats);
        for (int i = 0; i < numFloats; i++) {
            buffer.put(0f);
        }
        buffer.flip();
        return buffer;
    }

    public FloatBuffer getFloatBuffer() {
        var buffer = BufferUtils.createFloatBuffer(FLOATS_PER_PARTICLE * spheres.size());
        for (var sphere : spheres) {
            sphere.position().putInBuffer(buffer);
            buffer.put(sphere.size()); // size
            sphere.color().putInBuffer(buffer);
        }
        buffer.flip();
        return buffer;
    }

    public void render(Vector3D viewPosition) {
        // Update VBO
        vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
            GL15.glBufferSubData(target, 0, getFloatBuffer());
        });
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        CommonPrograms3D.FAST_SPHERES.use(program -> {
            program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
            program.loadVector("viewPos", viewPosition);
            vertexArray.bind(vao -> {
                GL31.glDrawArraysInstanced(GL11.GL_TRIANGLES, 0, VERTICES.length, spheres.size());
            });
        });
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void dispose() {
        vertexBuffer.dispose();
        vertexArray.dispose();
    }
}
