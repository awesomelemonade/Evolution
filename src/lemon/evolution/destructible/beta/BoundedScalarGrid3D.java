package lemon.evolution.destructible.beta;

public interface BoundedScalarGrid3D extends ScalarGrid3D {
	public int getSizeX();
	public int getSizeY();
	public int getSizeZ();
	public static BoundedScalarGrid3D of(ScalarGrid3D grid, int size) {
		return BoundedScalarGrid3D.of(grid, size, size, size);
	}
	public static BoundedScalarGrid3D of(ScalarGrid3D grid, int sizeX, int sizeY, int sizeZ) {
		return new BoundedScalarGrid3D() {
			@Override
			public int getSizeX() {
				return sizeX;
			}
			@Override
			public int getSizeY() {
				return sizeY;
			}
			@Override
			public int getSizeZ() {
				return sizeZ;
			}
			@Override
			public float get(int x, int y, int z) {
				return grid.get(x, y, z);
			}
		};
	}
	public static BoundedScalarGrid3D of(float[][][] data) {
		return new BoundedScalarGrid3D() {
			@Override
			public int getSizeX() {
				return data.length;
			}
			@Override
			public int getSizeY() {
				return data[0].length;
			}
			@Override
			public int getSizeZ() {
				return data[0][0].length;
			}
			@Override
			public float get(int x, int y, int z) {
				return data[x][y][z];
			}
		};
	}
}
