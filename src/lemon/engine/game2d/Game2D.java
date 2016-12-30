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

public enum Game2D implements Listener {
	INSTANCE;
	private Box2D human;
	private Matrix projectionMatrix;
	private Map<String, Texture> textures;
	
	private Box2D windowBox;
	
	private SplitScreen main;
	private SplitScreen lightbulbScreen;
	private Box2D lightbulbScreenStencilBox;
	private Box2D lightbulbScreenBox;
	private Box2D lightbulb;
	
	private Box2D demographics;
	private Box2D[] electricityIcons;
	private String[] electricityIconNames;
	private Box2D text_virtually;
	private Box2D text_impossible;
	
	
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
		human = new Box2D(500f, 0, 1247f, 1067f);
		textures = new HashMap<String, Texture>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("res/texture-config")));
			String line;
			while((line=reader.readLine())!=null){
				StringTokenizer tokenizer = new StringTokenizer(line);
				String name = tokenizer.nextToken();
				System.out.println("Loading: "+name);
				Texture texture = new Texture();
				texture.load(new TextureData(ImageIO.read(new File("res/"+tokenizer.nextToken()))));
				textures.put(name, texture);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		interpolators = new ArrayList<Interpolator>();
		
		demographics = new Box2D(0, 0, 2954, 1491);
		demographics.scaleWidth(windowBox.getWidth());
		demographics.scale(1f);
		demographics.setY(-demographics.getHeight());
		
		
		interpolators.add(new FunctionInterpolator(demographics, getTime(0), getTime(1000), 
				new Vector(0, demographics.getHeight(), 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(demographics, getTime(2000), getTime(3000), 
				new Vector(demographics.getWidth()*0.1f, 0, -demographics.getWidth()*0.2f, -demographics.getHeight()*0.2f), f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(demographics, getTime(4500), getTime(5500), 
				new Vector(0, -(demographics.getHeight()*0.9f), 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		
		electricityIconNames = new String[]{"electricityicon-cord", "electricityicon-lightbulb", "electricityicon-tower", "electricityicon-windmill", "electricityicon-batteries"};
		electricityIcons = new Box2D[electricityIconNames.length];
		float[] yValues = new float[]{-450, -300, -250, -300, -450};
		for(int i=0;i<electricityIcons.length;++i){
			electricityIcons[i] = new Box2D(windowBox.getWidth()/electricityIcons.length*(i+0.5f)-75, windowBox.getHeight(), 150, 150);
			interpolators.add(new FunctionInterpolator(electricityIcons[i], getTime(2000), getTime(3000),
					new Vector(0, yValues[i], 0, 0),f->BezierCurves.EASE_IN.apply(f).get(1)));
			interpolators.add(new FunctionInterpolator(electricityIcons[i], getTime(4500), getTime(5500),
					new Vector(0, -(windowBox.getHeight()+yValues[i])-150, 0, 0),f->BezierCurves.EASE_OUT.apply(f).get(1)));
		}
		
		text_virtually = new Box2D(100, windowBox.getHeight(), 565, 125);
		text_virtually.scale(1.6f);
		text_impossible = new Box2D(700, windowBox.getHeight(), 647, 125);
		text_impossible.scale(1.6f);

		interpolators.add(new FunctionInterpolator(text_virtually, getTime(5500), getTime(5900),
				new Vector(0, -500, 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(text_impossible, getTime(5900), getTime(6300),
				new Vector(0, -850, 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		
		interpolators.add(new FunctionInterpolator(text_virtually, getTime(7500), getTime(7900),
				new Vector(0, -windowBox.getHeight()+500-text_virtually.getHeight(), 0, 0), f->BezierCurves.EASE_OUT.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(text_impossible, getTime(7500), getTime(7900),
				new Vector(0, -windowBox.getHeight()+850-text_impossible.getHeight(), 0, 0), f->BezierCurves.EASE_OUT.apply(f).get(1)));
		
		lightbulb = new Box2D(windowBox.getWidth()/2f-330/2, windowBox.getHeight()/2f-569/2, 330, 569);
		lightbulbScreen = new SplitScreen((int)windowBox.getWidth(), (int)windowBox.getHeight());
		lightbulbScreenStencilBox = new Box2D(0, 0, windowBox.getWidth(), windowBox.getHeight());
		lightbulbScreenBox = new Box2D(-lightbulbScreen.getWidth(), 0, lightbulbScreen.getWidth(), lightbulbScreen.getHeight());
		interpolators.add(new FunctionInterpolator(lightbulbScreenBox, getTime(7900), getTime(8900),
				new Vector(lightbulbScreenBox.getWidth(), 0, 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(lightbulbScreenStencilBox, getTime(9900), getTime(10900),
				new Vector(lightbulbScreenStencilBox.getWidth()/2, 0, -lightbulbScreenStencilBox.getWidth()/2, 0),
				f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(lightbulbScreenBox, getTime(9900), getTime(10900),
				new Vector(lightbulbScreenBox.getWidth()/4, 0, 0, 0),
				f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(lightbulbScreenBox, getTime(11900), getTime(12900),
				new Vector(lightbulbScreenBox.getWidth(), 0, 0, 0), f->BezierCurves.EASE_OUT.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(lightbulbScreenStencilBox, getTime(11900), getTime(12900),
				new Vector(lightbulbScreenBox.getWidth(), 0, 0, 0),
				f->BezierCurves.EASE_IN.apply(f).get(1)));
		
		EventManager.INSTANCE.registerListener(this);
	}
	long time = -500000000;
	@Subscribe
	public void update(UpdateEvent event){
		time+=event.getDelta();
		for(Interpolator interpolator: interpolators){
			interpolator.update(time);
		}
		human.setY(-human.getHeight()+
				BezierCurves.BOUNCE.apply(getTimeProgress(time, getTime(0), getTime(1000))).get(1)*1000f);
	}
	@Subscribe
	public void render(RenderEvent event){
		
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, main.getFrameBuffer().getId());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		
		//CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, human.getTransformationMatrix());
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("human").getId());
		//Quad.TEXTURED_2D.render();
		
		//CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, new Box2D(560f, 640f, 135f, 190f).getTransformationMatrix());
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("lightbulb").getId());
		//Quad.TEXTURED_2D.render();
		
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, demographics.getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("demographics").getId());
		Quad.TEXTURED_2D.render();
		
		for(int i=0;i<electricityIcons.length;++i){
			CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, electricityIcons[i].getTransformationMatrix());
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(electricityIconNames[i]).getId());
			Quad.TEXTURED_2D.render();
		}

		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, text_virtually.getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("text-virtually").getId());
		Quad.TEXTURED_2D.render();
		
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, text_impossible.getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("text-impossible").getId());
		Quad.TEXTURED_2D.render();
		
		GL20.glUseProgram(0);
		
		lightbulbScreen.render(lightbulbScreenStencilBox, lightbulbScreenBox);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lightbulbScreen.getFrameBuffer().getId());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, new Box2D(0, 0, lightbulbScreen.getWidth(), lightbulbScreen.getHeight()).getTransformationMatrix());
		CommonPrograms2D.COLOR.getShaderProgram().loadVector4f("colorMask", new Vector(0.7f, 0.7f, 0.7f, 1));
		Quad.COLORED_2D.render();
		CommonPrograms2D.COLOR.getShaderProgram().loadVector4f("colorMask", new Vector(1f, 1f, 1f, 1));
		GL20.glUseProgram(0);
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, lightbulb.getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("yellowlightbulb").getId());
		Quad.TEXTURED_2D.render();
		GL20.glUseProgram(0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		main.render(windowBox, windowBox);
		
		GL11.glDisable(GL11.GL_BLEND);
	}
	public long getTime(long milliseconds){
		return milliseconds*1000000;
	}
	public float getTimeProgress(long time, long startTime, long endTime){
		if(time<=startTime){
			return 0f;
		}
		if(time>=endTime){
			return 1f;
		}
		return ((float)(time-startTime))/((float)(endTime-startTime));
	}
	public float getProgress(float current, float start, float finish){
		if(current<=start){
			return 0f;
		}
		if(current>=finish){
			return 1f;
		}
		return ((float)(current-start))/((float)(finish-start));
	}
	@Subscribe
	public void onKey(KeyEvent event){
		if(event.getAction()==GLFW.GLFW_PRESS){
			
		}
		if(event.getAction()==GLFW.GLFW_RELEASE){
			if(event.getKey()==GLFW.GLFW_KEY_R){
				time = -500000000;
			}
		}
	}
}
