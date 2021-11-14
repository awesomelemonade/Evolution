package lemon.evolution;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.game.Player;
import lemon.engine.math.Box2D;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.toolbox.Color;
import lemon.evolution.destructible.beta.TerrainChunk;
import lemon.evolution.destructible.beta.TerrainRenderer;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.ui.beta.AbstractUIComponent;
import lemon.evolution.ui.beta.UIComponent;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.util.CommonPrograms3D;
import lemon.evolution.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.function.Supplier;

public class UIMinimap extends AbstractUIComponent {
	private final FrameBuffer frameBuffer;
	private final Box2D box;
	private final World world;
	private final Set<Player> players;
	private final TerrainRenderer terrainRenderer;
	private final Supplier<Player> playerSupplier;

	public UIMinimap(UIComponent parent, Box2D box, World world, Supplier<Player> entitySupplier) {
		super(parent);
		this.frameBuffer = disposables.add(new FrameBuffer(box));
		this.box = box;
		this.world = world;
		this.terrainRenderer = new TerrainRenderer(world.terrain(), 80f / world.terrain().scalar().x() / TerrainChunk.SIZE);
		this.players = world.entities().ofFiltered(Player.class, disposables::add);
		this.playerSupplier = entitySupplier;
		frameBuffer.bind(frameBuffer -> {
			GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
			Texture colorTexture = disposables.add(new Texture());
			var boxWidth = (int) box.width();
			var boxHeight = (int) box.height();
			TextureBank.MINIMAP_COLOR.bind(() -> {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.id());
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, boxWidth, boxHeight, 0, GL11.GL_RGB,
						GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.id(), 0);
			});
			Texture depthTexture = disposables.add(new Texture());
			TextureBank.MINIMAP_DEPTH.bind(() -> {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.id());
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, boxWidth, boxHeight, 0,
						GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthTexture.id(), 0);
			});
		});
	}

	@Override
	public void render() {
		if (isVisible()) {
			frameBuffer.bind(frameBuffer -> {
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				var currentPlayer = playerSupplier.get();
				var currentPosition = currentPlayer.position();
				var currentRotation = currentPlayer.rotation();
				var zoom = (float) Math.sqrt(players.stream().mapToDouble(player -> currentPosition.toXZVector().distanceSquared(player.position().toXZVector())).max().orElse(250.0)) + 20f;
				zoom = MathUtil.clamp(zoom, 20f, 100f);
				terrainRenderer.setRenderDistance(world.terrain().getChunkDistance(zoom + 1f));
				var projectionMatrix = MathUtil.getOrtho(-zoom, zoom, zoom, -zoom, 0f, 1000f);
				CommonPrograms3D.setMatrices(MatrixType.PROJECTION_MATRIX, projectionMatrix);
				try (var translationMatrix = MatrixPool.ofTranslation(currentPosition.add(Vector3D.of(0f, 100f, 0f)).invert());
					 var pitchMatrix = MatrixPool.ofRotationX(MathUtil.PI / 2f);
					 var rollMatrix = MatrixPool.ofRotationZ(currentRotation.y());
					 var rotationMatrix = MatrixPool.ofMultiplied(rollMatrix, pitchMatrix);
					 var viewMatrix = MatrixPool.ofMultiplied(rotationMatrix, translationMatrix)) {
					CommonPrograms3D.setMatrices(MatrixType.VIEW_MATRIX, viewMatrix);
					terrainRenderer.render(currentPosition);

					for (var player : players) {
						var projectedCurrentPosition = projectionMatrix.multiply(viewMatrix.multiply(player.position()));
						var width = 8;
						var height = 8;
						var x = projectedCurrentPosition.x() * box.width() / 2f - width / 2f + box.width() / 2f;
						var y = projectedCurrentPosition.y() * box.height() / 2f - height / 2f + box.height() / 2f;
						var color = player.team() == currentPlayer.team() ? Color.GREEN : Color.RED;
						CommonRenderables.renderQuad2D(new Box2D(x, y, width, height), color);
						CommonRenderables.renderQuad2D(new Box2D(x, y, width, height), color, MathUtil.PI / 4f);
					}
				}
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
