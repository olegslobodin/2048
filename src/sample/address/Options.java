package sample.address;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class Options {
	Text dimText;
	MainApp main;
	int d;
	int width = 110;
	int height = 110;
	AnchorPane exampleField;
	List<Block> list = new ArrayList<Block>();
	
	public AnchorPane initialize(MainApp main) {
		this.main = main;
		d = main.dimension;

		AnchorPane result = new AnchorPane();
		result.setPrefSize(300, 400);

		String dim = String.valueOf(d);
		dimText = new Text("Size: " + dim + "x" + dim);
		dimText.setLayoutX(120);
		dimText.setLayoutY(60);
		dimText.setId("Options_text");

		Slider sl = new Slider();
		sl.setOrientation(Orientation.HORIZONTAL);
		sl.setMin(2);
		sl.setMax(10);
		sl.setValue(d);
		sl.setLayoutX(120);
		sl.setLayoutY(80);
		sl.setOnMouseDragged(event -> {
			d = (int) sl.getValue();
			valueChanged();
		});

		Button btn1 = new Button();
		btn1.setPrefSize(100, 35);
		btn1.setLayoutX(100);
		btn1.setLayoutY(350);
		btn1.setText("Back");
		btn1.setOnMouseClicked(event -> {
			main.menu();
		});

		exampleField = new AnchorPane();
		exampleField.setPrefSize(width, height);

		result.getChildren().addAll(dimText, sl, btn1, exampleField);
		exampleGenerate();
		return result;
	}

	private void valueChanged() {
		main.dimension = d;
		dimText.setText("Size: " + d + "x" + d);
		exampleGenerate();
	}

	private void exampleGenerate() {
		Controller ctrl = new Controller(main, exampleField, width, height, d);
		if (!list.isEmpty())
			exampleField.getChildren().removeAll(list);
		list.clear();
		for (int i = 0; i < d * d; i++) {
			Block b = ctrl.spawn();
			list.add(b);
			exampleField.getChildren().add(b);
		}
	}
}
