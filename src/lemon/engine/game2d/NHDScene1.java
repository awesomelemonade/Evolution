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
import lemon.engine.entity.TriangularModel;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.evolution.BezierCurves;
import lemon.engine.evolution.CommonPrograms2D;
import lemon.engine.evolution.CommonPrograms3D;
import lemon.engine.evolution.SplitScreen;
import lemon.engine.input.KeyEvent;
import lemon.engine.loader.ObjLoader;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.texture.TextureData;

public enum NHDScene1 implements Listener {
	INSTANCE;
	private Matrix projectionMatrix;
	private Map<String, Texture> textures;
	
	private Box2D windowBox;
	
	private SplitScreen main;
	private SplitScreen lightbulbScreen;
	private Box2D lightbulbScreenStencilBox;
	private Box2D lightbulbScreenBox;
	private Box2D lightbulb;
	private Box2D glow;
	private Vector glowMask;
	private SplitScreen entertainmentScreen;
	private Box2D entertainmentScreenStencilBox;
	private Box2D entertainmentScreenBox;
	private Box2D gameConsoleBox;
	private Box2D tvBox;
	private Box2D computerBox;
	
	private Box2D demographics;
	private Box2D[] electricityIcons;
	private String[] electricityIconNames;
	private Box2D text_virtually;
	private Box2D text_impossible;
	
	private Vector starBackgroundColor;
	private TriangularModel globe;
	private Vector3D globePosition;
	private Vector3D globeScalar;
	private Vector globeColor;
	private String[] text_transformedTheWorldNames;
	private Box2D[] text_transformedTheWorldBoxes;
	private Vector[] text_transformedTheWorldMasks;
	
	private List<Interpolator> interpolators;
	
	public void start(long window){
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		int window_width = width.get();
		int window_height = height.get();
		windowBox = new Box2D(0, 0, window_width, window_height);
		projectionMatrix = MathUtil.getOrtho(window_width, window_height, -1000f, 1000f);
		
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
		GL20.glUseProgram(CommonPrograms3D.PROGRAM.getShaderProgram().getId());
		CommonPrograms3D.PROGRAM.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms3D.PROGRAM.getShaderProgram().loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms3D.PROGRAM.getShaderProgram().loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
		CommonPrograms3D.PROGRAM.getShaderProgram().loadInt("colorSampler", TextureBank.REUSE.getId());
		CommonPrograms3D.PROGRAM.getShaderProgram().loadVector4f("color", new Vector(0, 0, 0, 0));
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
		
		text_virtually = new Box2D(120, windowBox.getHeight(), 565, 125);
		text_virtually.scale(1.6f);
		text_impossible = new Box2D(720, windowBox.getHeight(), 647, 125);
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
		glow = new Box2D(0, 350, 500, 600);
		glow.setX(lightbulb.getX()+lightbulb.getWidth()/2-glow.getWidth()/2);
		glowMask = new Vector(1, 1, 1, 1);
		interpolators.add(new FunctionInterpolator(lightbulbScreenBox, getTime(8900), getTime(9900),
				new Vector(lightbulbScreenBox.getWidth(), 0, 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(lightbulbScreenStencilBox, getTime(10400), getTime(11400),
				new Vector(lightbulbScreenStencilBox.getWidth()/2, 0, -lightbulbScreenStencilBox.getWidth()/2, 0),
				f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(lightbulbScreenBox, getTime(10400), getTime(11400),
				new Vector(lightbulbScreenBox.getWidth()/4, 0, 0, 0),
				f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(lightbulbScreenBox, getTime(11900), getTime(12900),
				new Vector(lightbulbScreenBox.getWidth(), 0, 0, 0), f->BezierCurves.EASE_OUT.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(lightbulbScreenStencilBox, getTime(11900), getTime(12900),
				new Vector(lightbulbScreenBox.getWidth(), 0, 0, 0),
				f->BezierCurves.EASE_IN.apply(f).get(1)));
		
		entertainmentScreen = new SplitScreen((int)windowBox.getWidth()/2, (int)windowBox.getHeight());
		entertainmentScreenStencilBox = new Box2D(0, 0, windowBox.getWidth()/2, windowBox.getHeight());
		entertainmentScreenBox = new Box2D(-windowBox.getWidth()/2, 0, entertainmentScreen.getWidth(), entertainmentScreen.getHeight());
		interpolators.add(new FunctionInterpolator(entertainmentScreenBox, getTime(10400), getTime(11400),
				new Vector(windowBox.getWidth()/2, 0, 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(entertainmentScreenBox, getTime(11900), getTime(12900),
				new Vector(windowBox.getWidth(), 0, 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(entertainmentScreenStencilBox, getTime(11900), getTime(12900),
				new Vector(windowBox.getWidth(), 0, 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		
		gameConsoleBox = new Box2D(200, 250, 1275, 1430);
		tvBox = new Box2D(150, 100, 900, 711);
		computerBox = new Box2D(500, 100, 2400, 1908);
		gameConsoleBox.scale(0.55f);
		tvBox.scale(0.35f);
		computerBox.scale(0.15f);
		
		starBackgroundColor = new Vector(0.4f, 0.4f, 0.4f, 0f);
		interpolators.add(new FunctionInterpolator(starBackgroundColor, getTime(11900), getTime(11901),
				new Vector(0, 0, 0, 1), f->BezierCurves.LINEAR.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(starBackgroundColor, getTime(14900), getTime(15900),
				new Vector(0, 0, 0, -1), f->BezierCurves.LINEAR.apply(f).get(1)));
		
		
		globe = new ObjLoader(new File("res/globe.obj")).load();
		globe.init();
		
		globePosition = new Vector3D(windowBox.getWidth()/2, windowBox.getHeight()/2+100, 0);
		globeScalar = new Vector3D(5, 5, 5);
		globeColor = new Vector(0, 1, 0, 0);

		interpolators.add(new FunctionInterpolator(globeColor, getTime(11900), getTime(12900),
				new Vector(0, 0, 0, 1), f->BezierCurves.LINEAR.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(globeColor, getTime(14900), getTime(15900),
				new Vector(0, 0, 0, -1), f->BezierCurves.LINEAR.apply(f).get(1)));
		
		text_transformedTheWorldNames = new String[]{"text-transformed", "text-the", "text-world"};
		
		text_transformedTheWorldBoxes = new Box2D[text_transformedTheWorldNames.length];
		text_transformedTheWorldBoxes[0] = new Box2D(70, windowBox.getHeight(), 827, 125);
		text_transformedTheWorldBoxes[1] = new Box2D(1090, windowBox.getHeight(), 215, 125);
		text_transformedTheWorldBoxes[2] = new Box2D(1370, windowBox.getHeight(), 410, 125);
		
		for(Box2D box: text_transformedTheWorldBoxes){
			box.scale(1.2f);
		}
		
		text_transformedTheWorldMasks = new Vector[text_transformedTheWorldNames.length];
		for(int i=0;i<text_transformedTheWorldMasks.length;++i){
			text_transformedTheWorldMasks[i] = new Vector(1f, 1f, 1f, 1f);
			interpolators.add(new FunctionInterpolator(text_transformedTheWorldMasks[i], getTime(14900), getTime(15900),
					new Vector(0, 0, 0, -1), f->BezierCurves.LINEAR.apply(f).get(1)));
		}
		
		interpolators.add(new FunctionInterpolator(text_transformedTheWorldBoxes[0], getTime(11900), getTime(12900),
				new Vector(0, -1000, 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(text_transformedTheWorldBoxes[1], getTime(12400), getTime(13400),
				new Vector(0, -1000, 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		interpolators.add(new FunctionInterpolator(text_transformedTheWorldBoxes[2], getTime(12600), getTime(13600),
				new Vector(0, -1000, 0, 0), f->BezierCurves.EASE_IN.apply(f).get(1)));
		
		EventManager.INSTANCE.registerListener(this);
	}
	long time = 0;
	@Subscribe
	public void update(UpdateEvent event){
		time+=event.getDelta();
		for(Interpolator interpolator: interpolators){
			interpolator.update(time);
		}
		if(Math.random()<0.2){
			glowMask.set(3, (float)Math.random());
		}
	}
	float angle = 0;
	@Subscribe
	public void render(RenderEvent event){
		angle+=2f;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, main.getFrameBuffer().getId());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, windowBox.getTransformationMatrix());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", starBackgroundColor);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("starsbackground").getId());
		Quad.TEXTURED_2D.render();
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", new Vector(1f, 1f, 1f, 1f));
		GL20.glUseProgram(0);
		GL20.glUseProgram(CommonPrograms3D.PROGRAM.getShaderProgram().getId());
		CommonPrograms3D.PROGRAM.getShaderProgram().loadVector4f("color", globeColor);
		CommonPrograms3D.PROGRAM.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX,
				MathUtil.getTranslation(new Vector3D(globePosition)).multiply(MathUtil.getRotationY((float)angle).multiply(
						MathUtil.getScalar(globeScalar).multiply(MathUtil.getTranslation(new Vector3D(-13.4f, -7.8f, 21.8f))))));
		globe.render();
		GL20.glUseProgram(0);
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		
		for(int i=0;i<text_transformedTheWorldNames.length;++i){
			CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, text_transformedTheWorldBoxes[i].getTransformationMatrix());
			CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", text_transformedTheWorldMasks[i]);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(text_transformedTheWorldNames[i]).getId());
			Quad.TEXTURED_2D.render();
			CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", new Vector(1f, 1f, 1f, 1f));
		}
		
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
		entertainmentScreen.render(entertainmentScreenStencilBox, entertainmentScreenBox);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lightbulbScreen.getFrameBuffer().getId());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, new Box2D(0, 0, lightbulbScreen.getWidth(), lightbulbScreen.getHeight()).getTransformationMatrix());
		CommonPrograms2D.COLOR.getShaderProgram().loadVector4f("colorMask", new Vector(0.5f, 0.5f, 0.7f, 1f));
		Quad.COLORED_2D.render();
		CommonPrograms2D.COLOR.getShaderProgram().loadVector4f("colorMask", new Vector(1f, 1f, 1f, 1f));
		GL20.glUseProgram(0);
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, lightbulb.getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("yellowlightbulb").getId());
		Quad.TEXTURED_2D.render();
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, glow.getTransformationMatrix());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", glowMask);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("glow").getId());
		Quad.TEXTURED_2D.render();
		CommonPrograms2D.TEXTURE.getShaderProgram().loadVector4f("colorMask", new Vector(1f, 1f, 1f, 1f));
		GL20.glUseProgram(0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, entertainmentScreen.getFrameBuffer().getId());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, new Box2D(0, 0, entertainmentScreen.getWidth(), entertainmentScreen.getHeight()).getTransformationMatrix());
		CommonPrograms2D.COLOR.getShaderProgram().loadVector4f("colorMask", new Vector(0.7f, 0.5f, 0.5f, 1f));
		Quad.COLORED_2D.render();
		CommonPrograms2D.COLOR.getShaderProgram().loadVector4f("colorMask", new Vector(1f, 1f, 1f, 1f));
		GL20.glUseProgram(0);
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, gameConsoleBox.getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("gameconsole").getId());
		Quad.TEXTURED_2D.render();
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, tvBox.getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("television").getId());
		Quad.TEXTURED_2D.render();
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, computerBox.getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("computer").getId());
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
