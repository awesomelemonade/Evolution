package lemon.engine.evolution;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.RenderEvent;
import lemon.engine.entity.Quad;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.game2d.Box2D;
import lemon.engine.game2d.Game2D;
import lemon.engine.input.MouseButtonEvent;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;

public enum Menu implements Listener {
	INSTANCE;
	private long window;
	private List<Box2D> buttons;
	
	public void start(long window){
		this.window = window;
		
		CommonPrograms2D.initAll();
		CommonPrograms3D.initAll();
		
		Quad.initAll();
		
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		GL20.glUseProgram(0);
		
		EventManager.INSTANCE.registerListener(this);
		buttons = new ArrayList<Box2D>();
		for(int i=0;i<3;++i){
			buttons.add(new Box2D(-0.3f, -0.3f-0.2f*i, 0.6f, 0.1f));
		}
	}
	@Subscribe
	public void onMouseClick(MouseButtonEvent event){
		if(event.getAction()==GLFW.GLFW_RELEASE){
			DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
			DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
			GLFW.glfwGetCursorPos(window, xBuffer, yBuffer);
			float mouseX = (float) xBuffer.get();
			float mouseY = (float) yBuffer.get();
			IntBuffer width = BufferUtils.createIntBuffer(1);
			IntBuffer height = BufferUtils.createIntBuffer(1);
			GLFW.glfwGetWindowSize(window, width, height);
			int window_width = width.get();
			int window_height = height.get();
			mouseX = (2f*mouseX/window_width)-1f;
			mouseY = -1f*((2f*mouseY/window_height)-1f);
			for(int i=0;i<buttons.size();++i){
				if(buttons.get(i).intersect(mouseX, mouseY)){
					switch(i){
						case 0:
							startLoading();
							break;
						case 1:
							start2D();
							break;
						case 2:
							startText();
							break;
					}
				}
			}
		}
	}
	@Subscribe
	public void render(RenderEvent event){
		GL20.glUseProgram(CommonPrograms2D.COLOR.getShaderProgram().getId());
		for(Box2D button: buttons){
			CommonPrograms2D.COLOR.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX,
					MathUtil.getTranslation(new Vector3D(button.getX()+(button.getWidth()/2f), button.getY()+(button.getHeight()/2f), 0f))
					.multiply(MathUtil.getScalar(new Vector3D(button.getWidth()/2f, button.getHeight()/2f, 1f))));
			Quad.COLORED_2D.render();
		}
		GL20.glUseProgram(0);
	}
	public void startLoading(){
		Loading.INSTANCE.start(window);
		EventManager.INSTANCE.unregisterListener(this);
	}
	public void start2D(){
		Game2D.INSTANCE.start(window);
		EventManager.INSTANCE.unregisterListener(this);
	}
	public void startText(){
		FontTest.INSTANCE.start(window);
		EventManager.INSTANCE.unregisterListener(this);
	}
}
