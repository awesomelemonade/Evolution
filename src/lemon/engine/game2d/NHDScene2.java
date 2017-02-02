package lemon.engine.game2d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.animation.FunctionInterpolator;
import lemon.engine.animation.Interpolator;
import lemon.engine.control.RenderEvent;
import lemon.engine.control.UpdateEvent;
import lemon.engine.entity.Quad;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.evolution.BezierCurves;
import lemon.engine.evolution.CommonPrograms2D;
import lemon.engine.evolution.SplitScreen;
import lemon.engine.input.KeyEvent;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.texture.TextureData;

public enum NHDScene2 implements Listener {
	INSTANCE;
	private Matrix projectionMatrix;
	private Map<String, Texture> textures;
	
	private Box2D windowBox;
	
	private SplitScreen main;
	
	private Box2D franklinwikipedia;
	private Vector franklinwikipediaMask;
	private Box2D franklinkite;
	private Vector franklinkiteMask;
	
	private String[] edisonNames;
	private Box2D[] edisonBoxes;
	private Vector[] edisonMasks;
	private Box2D[] edisonStencils;
	
	private static final Vector RESET_MASK = new Vector(1, 1, 1, 1);
	
	private List<Interpolator> interpolators;
	
	public void start(long window){
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		int window_width = width.get();
		int window_height = height.get();
		windowBox = new Box2D(0, 0, window_width, window_height);
		projectionMatrix = MathUtil.getOrtho(window_width, window_height, -1f, 1f);
		
		//projectionMatrix = MathUtil.getOrtho(window_width*2f, window_height*2f, -1f, 1f);
		//projectionMatrix = projectionMatrix.multiply(MathUtil.getTranslation(new Vector3D(window_width/2, window_height/2, 0)));
		
		main = new SplitScreen((int)windowBox.getWidth(), (int)windowBox.getHeight());
		
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		GL20.glUseProgram(0);
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms2D.TEXTURE.getShaderProgram().loadInt("textureSampler", TextureBank.REUSE.getId());
		GL20.glUseProgram(0);
		textures = new HashMap<String, Texture>();
		String resFolder = "res/"+this.getClass().getSimpleName()+"/";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(resFolder+"texture-config")));
			String line;
			while((line=reader.readLine())!=null){
				StringTokenizer tokenizer = new StringTokenizer(line);
				String name = tokenizer.nextToken();
				System.out.println("Loading: "+name);
				Texture texture = new Texture();
				texture.load(new TextureData(ImageIO.read(new File(resFolder+tokenizer.nextToken()))));
				textures.put(name, texture);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		interpolators = new ArrayList<Interpolator>();
		
		franklinwikipedia = new Box2D(0f, 0f, 447, 599);
		franklinwikipedia.scaleHeight(windowBox.getHeight());
		franklinwikipedia.setX(windowBox.getWidth()/2-franklinwikipedia.getWidth()/2);
		franklinwikipediaMask = new Vector(1, 1, 1, 0f);
		
		interpolators.add(new FunctionInterpolator(franklinwikipedia, getTime(0), getTime(14000),
				asEndGoal(franklinwikipedia,
						new Vector(windowBox.getWidth()/2-(franklinwikipedia.getWidth()*1.4f)/2f, -400f,
								franklinwikipedia.getWidth()*1.4f, franklinwikipedia.getHeight()*1.4f)),
				f->BezierCurves.LINEAR.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(franklinwikipediaMask, getTime(0), getTime(2000),
				new Vector(0, 0, 0, 1),
				f->BezierCurves.LINEAR.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(franklinwikipediaMask, getTime(5000), getTime(7000),
				new Vector(0, 0, 0, -1),
				f->BezierCurves.LINEAR.apply(f).get(1)));
		
		franklinkite = new Box2D(0f, 0f, 1371, 830);
		franklinkite.scaleWidth(windowBox.getWidth());
		franklinkiteMask = new Vector(1, 1, 1, 0);
		
		interpolators.add(new FunctionInterpolator(franklinkite, getTime(0), getTime(14000),
				asEndGoal(franklinkite,
						new Vector(windowBox.getWidth()/2-(franklinkite.getWidth()*1.4f)/2f, -400f,
								franklinkite.getWidth()*1.4f, franklinkite.getHeight()*1.4f)),
				f->BezierCurves.LINEAR.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(franklinkiteMask, getTime(200), getTime(2200),
				new Vector(0, 0, 0, 1),
				f->BezierCurves.LINEAR.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(franklinkiteMask, getTime(10000), getTime(12000),
				new Vector(0, 0, 0, -1),
				f->BezierCurves.LINEAR.apply(f).get(1)));
		
		edisonNames = new String[]{"edison", "lightbulbsketch", "earlylightbulb", "edisonpatent2", "edisonpatent"};
		
		edisonBoxes = new Box2D[edisonNames.length];
		edisonBoxes[0] = new Box2D(0, 0, 468, 599); //edison
		edisonBoxes[1] = new Box2D(0, windowBox.getHeight()-650, 206, 300); //lightbulbsketch
		edisonBoxes[2] = new Box2D(0, 0, 1974, 2594); //earlylightbulb
		edisonBoxes[3] = new Box2D(0, 375, 500, 729); //patent2
		edisonBoxes[4] = new Box2D(0, -250, 500, 827); //patent
		
		edisonBoxes[0].scaleHeight(windowBox.getHeight());
		edisonBoxes[0].setX(windowBox.getWidth()/2-edisonBoxes[0].getWidth()/2);
		float x = (windowBox.getWidth()-edisonBoxes[0].getWidth())/2;
		for(int i=1;i<edisonBoxes.length;++i){
			edisonBoxes[i].scaleWidth(x);
		}
		edisonBoxes[3].setX(windowBox.getWidth()-x);
		edisonBoxes[4].setX(windowBox.getWidth()-x);
		edisonBoxes[2].setY(windowBox.getHeight()/2-edisonBoxes[2].getHeight());
		
		edisonMasks = new Vector[edisonNames.length];
		edisonMasks[0] = new Vector(1, 1, 1, 0);
		edisonMasks[1] = new Vector(1, 1, 1, 0);
		edisonMasks[2] = new Vector(1, 1, 1, 0);
		edisonMasks[3] = new Vector(1, 1, 1, 0);
		edisonMasks[4] = new Vector(1, 1, 1, 0);
		
		interpolators.add(new FunctionInterpolator(edisonMasks[0], getTime(10000), getTime(10001),
				new Vector(0, 0, 0, 1), f->BezierCurves.LINEAR.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(edisonMasks[0], getTime(16500), getTime(18500),
				new Vector(0, 0, 0, -1), f->BezierCurves.LINEAR.apply(f).get(1)));
		
		for(int i=1;i<edisonNames.length;++i){
			interpolators.add(new FunctionInterpolator(edisonMasks[i], getTime(12000+i*500), getTime(14000+i*500),
					new Vector(0, 0, 0, 1), f->BezierCurves.LINEAR.apply(f).get(1)));
			interpolators.add(new FunctionInterpolator(edisonMasks[i], getTime(16500), getTime(18500),
					new Vector(0, 0, 0, -1), f->BezierCurves.LINEAR.apply(f).get(1)));
		}
		
		edisonStencils = new Box2D[edisonNames.length];
		edisonStencils[0] = new Box2D(edisonBoxes[0]);
		edisonStencils[1] = new Box2D(0, windowBox.getHeight()/2-50, x, windowBox.getHeight()/2+50);
		edisonStencils[2] = new Box2D(0, 0, x, windowBox.getHeight()/2-50);
		edisonStencils[3] = new Box2D(windowBox.getWidth()-x, windowBox.getHeight()/2, x, windowBox.getHeight()/2);
		edisonStencils[4] = new Box2D(windowBox.getWidth()-x, 0, x, windowBox.getHeight()/2);
		
		interpolators.add(new FunctionInterpolator(edisonBoxes[0], getTime(10000), getTime(19000),
				new Vector(-100, -200, edisonBoxes[0].getWidth()*0.2f, edisonBoxes[0].getHeight()*0.2f), f->BezierCurves.LINEAR.apply(f).get(1)));
		
		
		EventManager.INSTANCE.registerListener(this);
	}
	public Vector asEndGoal(Vector before, Vector after){
		return after.subtract(before);
	}
	long time = 0;
	@Subscribe
	public void update(UpdateEvent event){
		time+=event.getDelta();
		for(Interpolator interpolator: interpolators){
			interpolator.update(time);
		}
	}
	@Subscribe
	public void render(RenderEvent event){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, main.getFrameBuffer().getId());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		for(int i=0;i<edisonBoxes.length;++i){
			doStencil(edisonStencils[i]);
			GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, edisonBoxes[i].getTransformationMatrix());
			CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", edisonMasks[i]);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(edisonNames[i]).getId());
			Quad.TEXTURED_2D.render();
			CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", RESET_MASK);
			GL11.glDisable(GL11.GL_STENCIL_TEST);
			GL20.glUseProgram(0);
		}
		
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, franklinkite.getTransformationMatrix());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", franklinkiteMask);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("franklin-kite").getId());
		Quad.TEXTURED_2D.render();
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", RESET_MASK);
		
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, franklinwikipedia.getTransformationMatrix());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", franklinwikipediaMask);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("franklin-wikipedia").getId());
		Quad.TEXTURED_2D.render();
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", RESET_MASK);
		
		
		GL20.glUseProgram(0);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		
		main.render(windowBox, windowBox);
		
		GL11.glDisable(GL11.GL_BLEND);
	}
	public long getTime(long milliseconds){
		return milliseconds*1000000;
	}
	public void doStencil(Box2D box){
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glColorMask(false, false, false, false);
		
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, box.getTransformationMatrix());
		Quad.COLORED_2D.render();
		GL20.glUseProgram(0);
		
		GL11.glStencilMask(0x00);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
		GL11.glColorMask(true, true, true, true);
		GL11.glDisable(GL11.GL_STENCIL_TEST);
	}
	@Subscribe
	public void onKey(KeyEvent event){
		if(event.getAction()==GLFW.GLFW_PRESS){
			
		}
		if(event.getAction()==GLFW.GLFW_RELEASE){
			if(event.getKey()==GLFW.GLFW_KEY_R){
				time = 0;
			}
		}
	}
}
