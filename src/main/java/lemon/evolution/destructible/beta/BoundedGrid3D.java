package lemon.evolution.destructible.beta;

public interface BoundedGrid3D<T> extends Grid3D<T> {
	public int getSizeX();

	public int getSizeY();

	public int getSizeZ();

	public static <T> BoundedGrid3D<T> of(Grid3D<T> grid, int size) {
		return BoundedGrid3D.of(grid, size, size, size);
	}

	public static <T> BoundedGrid3D<T> of(Grid3D<T> grid, int sizeX, int sizeY, int sizeZ) {
		return new BoundedGrid3D<>() {
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
			public T get(int x, int y, int z) {
				return grid.get(x, y, z);
			}
		};
	}

	public static <T> BoundedGrid3D<T> of(T[][][] data) {
		return new BoundedGrid3D<>() {
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
			public T get(int x, int y, int z) {
				return data[x][y][z];
			}
		};
	}
}
