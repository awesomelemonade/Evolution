package lemon.evolution;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Renderable;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.engine.toolbox.SkyboxLoader;
import lemon.evolution.util.CommonPrograms3D;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class SkyboxBackground implements Disposable, Renderable {
	private final Disposables disposables = new Disposables();
	private static final long LOOP_TIME_MILLIS = 30000;
	public SkyboxBackground(SkyboxInfo info) {
		TextureBank.SKYBOX.bind(() -> {
			Texture skyboxTexture = disposables.add(new Texture());
			skyboxTexture.load(new SkyboxLoader("/res/" + info.directoryPath()).load());
			GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, skyboxTexture.id());
		});
	}

	@Override
	public void render() {
		var scaledTime = ((float) (System.currentTimeMillis() % LOOP_TIME_MILLIS)) / ((float) LOOP_TIME_MILLIS);
		var rotation = Vector3D.of(scaledTime * MathUtil.TAU, 2f * scaledTime * MathUtil.TAU, 0f);
		GL11.glDepthMask(false);
		// render skybox
		CommonPrograms3D.CUBEMAP.use(program -> {
			var rotationMatrix = MathUtil.getRotation(rotation);
			CommonPrograms3D.CUBEMAP.loadMatrix(MatrixType.VIEW_MATRIX, rotationMatrix);
			CommonDrawables.SKYBOX.draw();
		});
		GL11.glDepthMask(true);
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
