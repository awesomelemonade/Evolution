package lemon.evolution.util;

import lemon.engine.render.ShaderProgram;
import lemon.engine.toolbox.Disposable;

public interface ShaderProgramHolder extends Disposable {
	public ShaderProgram getShaderProgram();

	@Override
	public default void dispose() {
		this.getShaderProgram().dispose();
	}
}
