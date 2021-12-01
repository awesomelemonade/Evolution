package lemon.evolution;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.function.MurmurHash;
import lemon.engine.function.PerlinNoise;
import lemon.engine.function.SzudzikIntPair;
import lemon.engine.math.Box2D;
import lemon.engine.math.MathUtil;
import lemon.engine.math.MutableVector4D;
import lemon.engine.math.Vector4D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Renderable;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.texture.TextureData;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.ToIntFunction;

public class PerlinBackground implements Disposable, Renderable {
	private static final int BACKGROUND_LOOP_TIME = 128;
	private final Disposables disposables = new Disposables();
	private final Box2D box;

	public PerlinBackground(Box2D box) {
		this.box = box;
		ToIntFunction<int[]> pairer = (b) -> Math.toIntExact(SzudzikIntPair.pair(b[0], b[1], b[2] * 32 + b[3]));
		PerlinNoise<Vector4D> noise = new PerlinNoise<>(4, MurmurHash::createWithSeed, pairer, x -> 1f, 3);
		var width = 32;
		var height = 32;
		var vector = MutableVector4D.ofZero();
		var buffers = new ByteBuffer[BACKGROUND_LOOP_TIME];
		for (int i = 0; i < buffers.length; i++) {
			buffers[i] = BufferUtils.createByteBuffer(width * height * 4);
		}
		for (int k = 0; k < BACKGROUND_LOOP_TIME; k++) {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					var radius = BACKGROUND_LOOP_TIME / 8;
					var angle = ((float) k) / BACKGROUND_LOOP_TIME * MathUtil.TAU;
					var value = noise.apply(vector.set(i - width / 2f, j - height / 2f, (float) (radius * Math.cos(angle)), (float) (radius * Math.sin(angle)))
							.divide(64f).asImmutable()) / 5f + 0.5f; // Normalize to 0f -> 1f range
					value /= 2f; // darker
					int intValue = Math.max(0, Math.min(255, (int) (256f * value)));
					buffers[k].put((byte) intValue);
					buffers[k].put((byte) intValue);
					buffers[k].put((byte) intValue);
					buffers[k].put((byte) 0b11111111);
				}
			}
			buffers[k].flip();
		}
		var texture = disposables.add(new Texture());
		texture.load(Arrays.stream(buffers).map(buffer -> new TextureData(width, height, buffer)).toArray(TextureData[]::new), GL12.GL_CLAMP_TO_EDGE);
		TextureBank.PERLIN_NOISE.bind(() -> {
			GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, texture.id());
		});
	}

	@Override
	public void render() {
		CommonPrograms2D.PERLIN.use(program -> {
			CommonRenderables.boxToMatrix(box, matrix -> {
				program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrix);
				program.loadInt("time", (int) ((System.currentTimeMillis() / 50.0) % BACKGROUND_LOOP_TIME));
				CommonDrawables.TEXTURED_QUAD.draw();
			});
		});
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
