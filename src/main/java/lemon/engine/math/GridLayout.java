package lemon.engine.math;

public class GridLayout {
	private final Box2D box;
	private final int rows;
	private final int columns;
	private final float spacingX;
	private final float spacingY;
	private final float totalSpacingX;
	private final float totalSpacingY;
	private final float innerBoxWidth;
	private final float innerBoxHeight;

	public GridLayout(Box2D box, int rows, int columns, float spacing) {
		this.box = box;
		this.rows = rows;
		this.columns = columns;
		this.spacingX = spacing;
		this.spacingY = spacing;
		this.totalSpacingX = (columns - 1) * spacingX;
		this.totalSpacingY = (rows - 1) * spacingY;
		this.innerBoxWidth = (box.width() - totalSpacingX) / columns;
		this.innerBoxHeight = (box.height() - totalSpacingY) / rows;
	}

	public Box2D getBox(int row, int column) {
		return new Box2D(box.x() + column * (innerBoxWidth + spacingX), box.y() + row * (innerBoxHeight + spacingY), innerBoxWidth, innerBoxHeight);
	}

	public Box2D getBox(int row, int column, int numRows, int numColumns) {
		return new Box2D(box.x() + row * (innerBoxWidth + spacingX),
				box.y() + column * (innerBoxHeight + spacingY),
				innerBoxWidth * numColumns + spacingY * (numColumns - 1),
				innerBoxHeight * numRows + spacingY * (numRows - 1));
	}
}
