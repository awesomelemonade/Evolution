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

public enum NHDScene implements Listener {
	INSTANCE;
	private Matrix projectionMatrix;
	private Map<String, Texture> textures;
	
	private Box2D windowBox;
	
	private SplitScreen main;
	
	private String[] names;
	private Box2D[] boxes;
	private Vector[] masks;
	
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
		
		names = new String[]{};
		boxes = new Box2D[]{};
		masks = new Vector[names.length];
		for(int i=0;i<masks.length;++i){
			masks[i] = new Vector(1, 1, 1, 0);
		}
		
		interpolators.add(new FunctionInterpolator(masks[0], getTime(0), getTime(2000),
				new Vector(0, 0, 0, 1), f->BezierCurves.LINEAR.apply(f).get(1)));
		
		EventManager.INSTANCE.registerListener(this);
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
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		
		for(int i=0;i<names.length;++i){
			renderTexture(names[i], boxes[i], masks[i]);
		}
		
		GL20.glUseProgram(0);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		
		main.render(windowBox, windowBox);
		
		GL11.glDisable(GL11.GL_BLEND);
	}
	public void renderTexture(String texture, Box2D box){
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, box.getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(texture).getId());
		Quad.TEXTURED_2D.render();
	}
	public void renderTexture(String texture, Box2D box, Vector mask){
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", mask);
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, box.getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(texture).getId());
		Quad.TEXTURED_2D.render();
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", new Vector(1f, 1f, 1f, 1f));
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
