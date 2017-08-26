package lemon.engine.math;

import java.util.Arrays;

public class Matrix {
	public static final Matrix IDENTITY_4 = Matrix.unmodifiableMatrix(Matrix.getIdentity(4));
	private static final String unmodifiableMessage = "Cannot Modify Matrix";
	private float[][] data;
	public Matrix(int size){
		this(size, size);
	}
	public Matrix(int m, int n){
		this.data = new float[m][n];
	}
	public Matrix(float[][] data){
		this.data = new float[data.length][data[0].length];
		for(int i=0;i<data.length;++i){
			for(int j=0;j<data[0].length;++j){
				this.data[i][j] = data[i][j];
			}
		}
	}
	public Matrix(Matrix matrix){
		this(matrix.data);
	}
	public void set(int m, int n, float data){
		this.data[m][n] = data;
	}
	public float get(int m, int n){
		return data[m][n];
	}
	public int getRows(){
		return data.length;
	}
	public int getColumns(){
		return data[0].length;
	}
	public Matrix multiply(Matrix matrix){
		if(getColumns()!=matrix.getRows()){
			throw new IllegalArgumentException("You cannot multiply "+getRows()+" x "+getColumns()+" by "+matrix.getRows()+" x "+matrix.getColumns());
		}
		Matrix product = new Matrix(getRows(), matrix.getColumns());
		for(int i=0;i<getRows();++i){
			for(int j=0;j<matrix.getColumns();++j){
				float sum = 0;
				for(int k=0;k<matrix.getRows();++k){
					sum+=get(i, k)*matrix.get(k, j);
				}
				product.set(i, j, sum);
			}
		}
		return product;
	}
	@Override
	public String toString(){
		return Arrays.deepToString(data);
	}
	public static Matrix unmodifiableMatrix(Matrix matrix){
		return new Matrix(matrix){
			@Override
			public void set(int x, int y, float data){
				throw new IllegalStateException(unmodifiableMessage);
			}
		};
	}
	public static Matrix getIdentity(int size){
		Matrix matrix = new Matrix(size);
		for(int i=0;i<size;++i){
			matrix.set(i, i, 1);
		}
		return matrix;
	}
}
