package lemon.engine.game2d;

import lemon.engine.draw.Drawable;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import lemon.engine.toolbox.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class Triangle2D implements Drawable {
    private final VertexArray vertexArray;
    private final Triangle triangle;
    private final Color[] colors;
    public Triangle2D(Vector3D a, Vector3D b, Vector3D c, Color... colors) {
        this.colors = new Color[3];
        for (int i = 0; i < 3; ++i) {
            this.colors[i] = colors[i % colors.length];
        }
        this.triangle = Triangle.of(a, b, c);
        vertexArray = new VertexArray();
        vertexArray.bind(vao -> {
            new VertexBuffer().bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
                GL15.glBufferData(target, getFloatBuffer(), GL15.GL_STATIC_DRAW);
                GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 6 * 4, 0);
                GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 6 * 4, 2 * 4);
            });
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
        });
    }
    public FloatBuffer getFloatBuffer() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(24);
        triangle.a().putInBuffer(buffer);
        colors[0].putInBuffer(buffer);
        triangle.b().putInBuffer(buffer);
        colors[1].putInBuffer(buffer);
        triangle.c().putInBuffer(buffer);
        colors[2].putInBuffer(buffer);
        buffer.flip();
        return buffer;
    }
    public void draw() {
        vertexArray.bind(vao -> {
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 3);
        });
    }
    public Triangle getTriangle() {
        return triangle;
    }
}
