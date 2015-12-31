package sample.address;

import java.awt.Point;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class Block extends Pane {
	public Block(double fieldWidth, double fieldHeight, int dimension, int row,
			int column, int value) {
		super();
		this.setId("Block");
		this.value = value;
		double interval = 0.1; // interval as part on the size
		double width = fieldWidth;
		width /= (dimension * (interval + 1) + interval);
		double height = fieldHeight;
		height /= (dimension * (interval + 1) + interval);
		double absolutInterval = width * interval;
		int x = (int) (column * width + (column + 1) * absolutInterval);
		int y = (int) (row * height + (row + 1) * absolutInterval);

		this.setLayoutX(x);
		this.setLayoutY(y);
		this.setWidth(width);
		this.setHeight(height);

		Rectangle base = new Rectangle(0, 0, width, height);
		base.setArcWidth(width * 0.2);
		base.setArcHeight(height * 0.2);
		base.setFill(getColorFor(value));

		String title = String.valueOf(value);
		Label titleLabel = new Label(title);
		titleLabel.setFont(new Font(height / (title.length() + 1)));
		titleLabel.setPrefSize(width, height);
		titleLabel.setAlignment(Pos.CENTER);
		titleLabel.setOpacity(1);

		this.getChildren().add(base);
		this.getChildren().add(titleLabel);
	}

	int value;

	public int getValue() {
		return value;
	}

	public static Point[][] getAllCoords(double fieldWidth, double fieldHeight,
			int dimension) {
		int d = dimension;
		Point[][] result = new Point[d][d];
		for (int row = 0; row < d; row++)
			for (int column = 0; column < d; column++) {
				result[row][column] = getCoord(fieldWidth, fieldHeight,
						dimension, row, column);
			}
		return result;
	}

	static Point getCoord(double fieldWidth, double fieldHeight, int dimension,
			int row, int column) {
		double interval = 0.1; // interval as part on the size
		double width = fieldWidth;
		width /= (dimension * (interval + 1) + interval);
		double height = fieldHeight;
		height /= (dimension * (interval + 1) + interval);
		double absolutInterval = width * interval;
		int x = (int) (column * width + (column + 1) * absolutInterval);
		int y = (int) (row * height + (row + 1) * absolutInterval);
		Point result = new Point(x, y);
		return result;
	}

	private Color getColorFor(int value) {
		switch (value) {
		case 2:
			return Color.web("c7ff00");
		case 4:
			return Color.web("f3ff00");
		case 8:
			return Color.web("ffd300");
		case 16:
			return Color.web("ffbc00");
		case 32:
			return Color.web("ff8400");
		case 64:
			return Color.web("ff5700");
		case 128:
			return Color.web("ff1f00");
		case 256:
			return Color.web("ff001b");
		case 512:
			return Color.web("ff0047");
		case 1024:
			return Color.web("ff0067");
		case 2048:
			return Color.web("ff00a9");
		default:
			return Color.web("b400ff");
		}
	}

}
