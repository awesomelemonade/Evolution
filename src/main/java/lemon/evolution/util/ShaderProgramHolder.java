package lemon.evolution.util;

import lemon.engine.render.ShaderProgram;
import lemon.engine.render.UniformVariable;

public interface ShaderProgramHolder extends ShaderProgram {
	public ShaderProgram shaderProgram();

	@Override
	public default int id() {
		return shaderProgram().id();
	}

	@Override
	public default UniformVariable getUniformVariable(String name) {
		return shaderProgram().getUniformVariable(name);
	}

	@Override
	public default void dispose() {
		shaderProgram().dispose();
	}
}
