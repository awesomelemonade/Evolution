package lemon.engine.evolution;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.RenderEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.game2d.Box2D;
import lemon.engine.game2d.Game2D;
import lemon.engine.game2d.Quad2D;
import lemon.engine.input.MouseButtonEvent;
import lemon.engine.math.Matrix;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.render.UniformVariable;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Toolbox;

public enum Menu implements Listener {
	INSTANCE;
	private long window;
	private ShaderProgram shaderProgram;
	private UniformVariable uniform_projectionMatrix;
	private UniformVariable uniform_transformationMatrix;
	private List<Quad2D> buttons;
	
	public void start(long window){
		this.window = window;
		shaderProgram = new ShaderProgram(
				new int[]{0, 1},
				new String[]{"position", "color"},
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/colorVertexShader")),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/colorFragmentShader"))
		);
		uniform_projectionMatrix = shaderProgram.getUniformVariable("projectionMatrix");
		uniform_transformationMatrix = shaderProgram.getUniformVariable("transformationMatrix");
		GL20.glUseProgram(shaderProgram.getId());
		uniform_projectionMatrix.loadMatrix(Matrix.IDENTITY_4);
		uniform_transformationMatrix.loadMatrix(Matrix.IDENTITY_4);
		GL20.glUseProgram(0);
		EventManager.INSTANCE.registerListener(this);
		buttons = new ArrayList<Quad2D>();
		for(int i=0;i<3;++i){
			buttons.add(new Quad2D(new Box2D(-0.3f, -0.3f-i*0.2f, 0.6f, 0.1f), new Color(1f, 1f, 1f)));
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
				if(buttons.get(i).getBox2D().intersect(mouseX, mouseY)){
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
		GL20.glUseProgram(shaderProgram.getId());
		for(Quad2D button: buttons){
			button.render();
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
