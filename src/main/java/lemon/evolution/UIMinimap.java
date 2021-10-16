package lemon.evolution;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.math.Box2D;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.evolution.destructible.beta.TerrainRenderer;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.ui.beta.AbstractUIComponent;
import lemon.evolution.ui.beta.UIComponent;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.util.CommonPrograms3D;
import lemon.evolution.world.ControllableEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public class UIMinimap extends AbstractUIComponent {
	private final FrameBuffer frameBuffer;
	private final Box2D box;
	private final TerrainRenderer terrainRenderer;
	private final Supplier<ControllableEntity> entitySupplier;

	public UIMinimap(UIComponent parent, Box2D box, TerrainRenderer terrainRenderer, Supplier<ControllableEntity> entitySupplier) {
		super(parent);
		this.frameBuffer = disposables.add(new FrameBuffer(box));
		this.box = box;
		this.terrainRenderer = terrainRenderer;
		this.entitySupplier = entitySupplier;
		frameBuffer.bind(frameBuffer -> {
			GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
			Texture colorTexture = disposables.add(new Texture());
			var boxWidth = (int) box.width();
			var boxHeight = (int) box.height();
			TextureBank.MINIMAP_COLOR.bind(() -> {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.getId());
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, boxWidth, boxHeight, 0, GL11.GL_RGB,
						GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.getId(), 0);
			});
			Texture depthTexture = disposables.add(new Texture());
			TextureBank.MINIMAP_DEPTH.bind(() -> {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.getId());
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, boxWidth, boxHeight, 0,
						GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthTexture.getId(), 0);
			});
		});
	}

	@Override
	public void render() {
		if (isVisible()) {
			frameBuffer.bind(frameBuffer -> {
				GL11.glViewport(0, 0, (int) box.width(), (int) box.height());
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				var entity = entitySupplier.get();
				var currentPosition = entity.position();
				var currentRotation = entity.rotation();
				CommonPrograms3D.setMatrices(MatrixType.PROJECTION_MATRIX, MathUtil.getOrtho(-50f, 50f, 50f, -50f, 0f, 1000f));
				try (var translationMatrix = MatrixPool.ofTranslation(currentPosition.add(Vector3D.of(0f, 100f, 0f)).invert());
					 var pitchMatrix = MatrixPool.ofRotationX(MathUtil.PI / 2f);
					 var rollMatrix = MatrixPool.ofRotationZ(currentRotation.y());
					 var rotationMatrix = MatrixPool.ofMultiplied(rollMatrix, pitchMatrix);
					 var viewMatrix = MatrixPool.ofMultiplied(rotationMatrix, translationMatrix)) {
					CommonPrograms3D.setMatrices(MatrixType.VIEW_MATRIX, viewMatrix);
				}
				terrainRenderer.render(currentPosition);
			});
			CommonPrograms2D.MINIMAP.use(program -> {
				try (var translationMatrix = MatrixPool.ofTranslation(box.x() + box.width() / 2f, box.y() + box.height() / 2f, 0f);
					 var scalarMatrix = MatrixPool.ofScalar(box.width() / 2f, box.height() / 2f, 1f);
					 var matrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
					program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrix);
					CommonDrawables.TEXTURED_QUAD.draw();
				}
			});
		}
	}
}
