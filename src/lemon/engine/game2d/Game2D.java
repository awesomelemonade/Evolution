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
import lemon.engine.math.Vector3D;
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
	
	private SplitScreen lightbulb;
	
	public void start(long window){
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		int window_width = width.get();
		int window_height = height.get();
		windowBox = new Box2D(0, 0, window_width, window_height);
		projectionMatrix = MathUtil.getOrtho(window_width, window_height, -1f, 1f);
		
		lightbulb = new SplitScreen(135, 190);
		
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
				Texture texture = new Texture();
				texture.load(new TextureData(ImageIO.read(new File("res/"+tokenizer.nextToken()))));
				textures.put(name, texture);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		EventManager.INSTANCE.registerListener(this);
	}
	long time = -500000000;
	float y = 0;
	@Subscribe
	public void update(UpdateEvent event){
		time+=event.getDelta();
		human.setY(-human.getHeight()+
				BezierCurves.BOUNCE.apply(getTimeProgress(time, getTime(0), getTime(1000))).get(1)*1000f);
		if(time>0){
			float number = (BezierCurves.EASE_IN.apply(getProgress(time, getTime(0), getTime(1000))).get(1)*3+2.7f+((float)(Math.random()*2f)));
			float width = (windowBox.getWidth()*0.4f);
			for(int i=0;i<number;++i){
				population.add(new Vector3D(width/(((int)number))*((float)(i+Math.random()-0.5)), y, 0f));
			}
		}
		/*for(int i=0;i<number;++i){
			population.add(new Vector3D((float)(Math.random()*(windowBox.getWidth()*0.4f)+(0.5f*windowBox.getWidth())),
					y, (float)(Math.random()*200f-100f)));
		}*/
		y = BezierCurves.EASE_IN.apply(getTimeProgress(time, getTime(0), getTime(1000))).get(1)*windowBox.getHeight();
		//y = ((float)time)/((float)1000000)-400;
	}
	List<Vector3D> population = new ArrayList<Vector3D>();
	@Subscribe
	public void render(RenderEvent event){
		
		
		GL20.glUseProgram(CommonPrograms2D.TEXTURE.getShaderProgram().getId());
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, human.getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("human").getId());
		Quad.TEXTURED_2D.render();
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lightbulb.getFrameBuffer().getId());
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, new Box2D(0f, 0f, 135f, 190f).getTransformationMatrix());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("lightbulb").getId());
		Quad.TEXTURED_2D.render();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		

		//CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, new Box2D(560f, 640f, 135f, 190f).getTransformationMatrix());
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("lightbulb").getId());
		//Quad.TEXTURED_2D.render();
		
		
		
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		for(Vector3D vector: population){
			CommonPrograms2D.TEXTURE.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, new Box2D(vector.getX(), vector.getY(), 80, 202).getTransformationMatrix());
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get("population").getId());
			Quad.TEXTURED_2D.render();
		}
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		GL20.glUseProgram(0);
		
		
		//if(time>getTime(2750)){
			lightbulb.render(new Box2D(560f, 640f, lightbulb.getWidth(), lightbulb.getHeight()));
		//}
		
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
				population.clear();
			}
		}
	}
}
