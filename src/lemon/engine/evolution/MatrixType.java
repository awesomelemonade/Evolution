package lemon.engine.evolution;

public enum MatrixType {
	MODEL_MATRIX("modelMatrix"), VIEW_MATRIX("viewMatrix"), PROJECTION_MATRIX("projectionMatrix");
	private String uniformVariableName;
	private MatrixType(String uniformVariableName){
		this.uniformVariableName = uniformVariableName;
	}
	public String getUniformVariableName(){
		return uniformVariableName;
	}
}
