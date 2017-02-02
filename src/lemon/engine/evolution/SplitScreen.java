package lemon.engine.evolution;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import lemon.engine.entity.Quad;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.game2d.Box2D;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;

public class SplitScreen {
	private FrameBuffer frameBuffer;
	private Texture colorTexture;
	private Texture depthTexture;
	
	private int width;
	private int height;
	
	public SplitScreen(int width, int height){
		this.width = width;
		this.height = height;
		frameBuffer = new FrameBuffer();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer.getId());
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		colorTexture = new Texture();
		GL13.glActiveTexture(TextureBank.REUSE.getBind());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.getId());
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.getId(), 0);
		depthTexture = new Texture();
		GL13.glActiveTexture(TextureBank.REUSE.getBind());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.getId());
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH24_STENCIL8, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer)null);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, depthTexture.getId(), 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	public FrameBuffer getFrameBuffer(){
		return frameBuffer;
	}
	public Texture getColorTexture(){
		return colorTexture;
	}
	public Texture getDepthTexture(){
		return depthTexture;
	}
	public int getWidth(){
		return width;
	}
	public int getHeight(){
		return height;
	}
	public void render(Box2D stencil, Box2D box){
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glColorMask(false, false, false, false);
		
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, stencil.getTransformationMatrix());
		Quad.COLORED_2D.render();
		GL20.glUseProgram(0);
		
		GL11.glStencilMask(0x00);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
		GL11.glColorMask(true, true, true, true);
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.getId());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX,
				box.getTransformationMatrix().multiply(MathUtil.getScalar(new Vector3D(1, -1, 1))));
		Quad.TEXTURED_2D.render();
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_STENCIL_TEST);
	}
	public void render(Box2D stencil, Box2D box, Vector colorMask){
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glColorMask(false, false, false, false);
		
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, stencil.getTransformationMatrix());
		Quad.COLORED_2D.render();
		GL20.glUseProgram(0);
		
		GL11.glStencilMask(0x00);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
		GL11.glColorMask(true, true, true, true);
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", colorMask);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.getId());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX,
				box.getTransformationMatrix().multiply(MathUtil.getScalar(new Vector3D(1, -1, 1))));
		Quad.TEXTURED_2D.render();
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", new Vector(1f, 1f, 1f, 1f));
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_STENCIL_TEST);
	}
}
