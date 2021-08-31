package lemon.engine.render;

public enum MatrixType {
	MODEL_MATRIX("modelMatrix"), VIEW_MATRIX("viewMatrix"), PROJECTION_MATRIX(
			"projectionMatrix"), TRANSFORMATION_MATRIX("transformationMatrix");
	private final String uniformVariableName;

	private MatrixType(String uniformVariableName) {
		this.uniformVariableName = uniformVariableName;
	}

	public String getUniformVariableName() {
		return uniformVariableName;
	}
}
