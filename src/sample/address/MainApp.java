package sample.address;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainApp extends Application {

	Stage primaryStage;
	int dimension = 4;
	int maxUndoCount = 3;
	public boolean soundEnabled;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("2048");
		this.primaryStage.setWidth(308);
		this.primaryStage.setHeight(430);
		primaryStage.setResizable(false);
		menu();
	}

	public void menu() {
		AnchorPane layout = new AnchorPane();
		layout.setPrefSize(290, 390);

		Scene scene = new Scene(layout);
		scene.getStylesheets().add(
				getClass().getResource("view/menu.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();

		Text welcome = new Text("2048");
		welcome.setLayoutX(70);
		welcome.setLayoutY(110);
		welcome.setId("Welcome_text");

		Button btn1 = new Button();
		btn1.setPrefSize(100, 35);
		btn1.setLayoutX(100);
		btn1.setLayoutY(140);
		btn1.setText("Play");

		Button btn2 = new Button();
		btn2.setPrefSize(100, 35);
		btn2.setLayoutX(100);
		btn2.setLayoutY(190);
		btn2.setText("Options");

		Button btn3 = new Button();
		btn3.setPrefSize(100, 35);
		btn3.setLayoutX(100);
		btn3.setLayoutY(240);
		btn3.setText("Exit");

		layout.getChildren().addAll(welcome, btn1, btn2, btn3);

		btn1.setOnMouseClicked(event -> {
			game(false);
		});

		btn2.setOnMouseClicked(event -> {
			options();
		});

		btn3.setOnMouseClicked(event -> {
			primaryStage.close();
		});
	}

	public void options() {
		Options opt = new Options();
		AnchorPane optionsView = opt.initialize(this);
		Scene scene = new Scene(optionsView);
		scene.getStylesheets().add(
				getClass().getResource("view/menu.css").toExternalForm());
		primaryStage.setScene(scene);
	}

	Controller ctrl;
	AnchorPane gameView;
	Text t2;
	Button btnUndo;

	public void game(boolean newGame) {
		gameView = new AnchorPane();
		gameView.setPrefSize(300, 400);

		Scene scene = new Scene(gameView);
		scene.getStylesheets().add(
				getClass().getResource("view/game.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();

		SplitPane sp1 = new SplitPane();
		sp1.setOrientation(Orientation.VERTICAL);
		gameView.getChildren().add(sp1);

		AnchorPane a1 = new AnchorPane();
		a1.setPrefSize(300, 95);
		AnchorPane a2 = new AnchorPane();
		a2.setPrefSize(300, 300);
		sp1.getItems().addAll(a1, a2);

		Text t1 = new Text("Score:");
		t1.setLayoutX(30);
		t1.setLayoutY(50);
		t1.setId("Score_text");
		t2 = new Text("0");
		t2.setLayoutX(150);
		t2.setLayoutY(50);
		t2.setId("Score_text");

		Button btnAuto = new Button("Auto");
		btnAuto.setPrefSize(50, 20);
		btnAuto.setOnMouseClicked(event -> {
			ctrl.autoMoveEnabled = !ctrl.autoMoveEnabled;
			if (ctrl.autoMoveEnabled)
				ctrl.randomMove();
		});

		Button btnSound = new Button("Sound is "
				+ ((soundEnabled) ? "on" : "off"));
		btnSound.setPrefSize(100, 20);
		btnSound.setLayoutX(100);
		btnSound.setOnMouseClicked(event -> {
			soundEnabled = !soundEnabled;
			if (soundEnabled)
				btnSound.setText("Sound is on");
			else
				btnSound.setText("Sound is off");
		});

		Button btnEsc = new Button("Esc");
		btnEsc.setPrefSize(50, 20);
		btnEsc.setLayoutX(250);
		btnEsc.setOnMouseClicked(event -> {
			escMenu(false);
		});

		Button btnSort = new Button("Sort");
		btnSort.setPrefSize(80, 20);
		btnSort.setLayoutY(75);
		btnSort.setOnMouseClicked(event -> {
			ctrl.sort();
		});

		btnUndo = new Button("Undo (" + maxUndoCount + ")");
		btnUndo.setPrefSize(80, 20);
		btnUndo.setLayoutX(220);
		btnUndo.setLayoutY(75);
		btnUndo.setOnMouseClicked(event -> {
			ctrl.states.undo();
			ctrl.writeLog();
		});

		a1.getChildren().addAll(t1, t2, btnAuto, btnSound, btnEsc, btnSort,
				btnUndo);

		Pane field = new Pane();
		int loadedDimension = Controller.loadOption(0);
		if (!newGame && loadedDimension > 0)
			ctrl = new Controller(this, field, 300, 300, loadedDimension);
		else
			ctrl = new Controller(this, field, 300, 300, dimension);

		field.setPrefSize(300, 300);
		a2.getChildren().add(field);

		for (int i = 0; i < 2; i++)
			field.getChildren().add(ctrl.spawn());

		field.setOnSwipeLeft(event -> {
			ctrl.move("Left");
		});

		field.setOnSwipeUp(event -> {
			ctrl.move("Up");
		});

		field.setOnSwipeRight(event -> {
			ctrl.move("Right");
		});

		field.setOnSwipeDown(event -> {
			ctrl.move("Down");
		});

		scene.setOnKeyPressed(event -> {
			switch (event.getCode()) {
			case LEFT:
				ctrl.move("Left");
				break;
			case UP:
				ctrl.move("Up");
				break;
			case RIGHT:
				ctrl.move("Right");
				break;
			case DOWN:
				ctrl.move("Down");
				break;
			case ESCAPE:
				escMenu(false);
				break;
			default:
				break;
			}
		});

		if (!newGame)
			ctrl.tryLoadGame();
		else
			ctrl.clearTurns();
	}

	public void gameProcess() {
		t2.setText(String.valueOf(ctrl.score));
		btnUndo.setText("Undo (" + ctrl.undoCount + ")");
		if (ctrl.gameOver)
			escMenu(true);
	}

	public boolean escMenuIsShown;
	ArrayList<Node> escMenuElements = new ArrayList<Node>();

	public void escMenu(boolean isGameOver) {
		if (escMenuIsShown) {
			gameView.getChildren().removeAll(escMenuElements);
			escMenuElements.clear();
			escMenuIsShown = false;
			return;
		}
		ctrl.autoMoveEnabled = false;
		escMenuIsShown = true;
		Rectangle background = new Rectangle(0, 100, 305, 305);
		background.setFill(Color.WHITE);
		background.setOpacity(0.5);

		Button btnContinue = new Button();
		btnContinue.setPrefSize(100, 35);
		btnContinue.setLayoutX(100);
		btnContinue.setLayoutY(160);
		btnContinue.setText("Continue");

		Button btnReplay = new Button();
		btnReplay.setPrefSize(100, 35);
		btnReplay.setLayoutX(100);
		btnReplay.setLayoutY(210);
		btnReplay.setText("Replay");

		Button btnMenu = new Button();
		btnMenu.setPrefSize(100, 35);
		btnMenu.setLayoutX(100);
		btnMenu.setLayoutY(260);
		btnMenu.setText("Main menu");

		escMenuElements.add(background);
		if (!isGameOver)
			escMenuElements.add(btnContinue);
		escMenuElements.add(btnReplay);
		escMenuElements.add(btnMenu);
		gameView.getChildren().addAll(escMenuElements);

		btnContinue.setOnMouseClicked(event -> {
			escMenuIsShown = false;
			gameView.getChildren().removeAll(escMenuElements);
			escMenuElements.clear();
		});

		btnReplay.setOnMouseClicked(event -> {
			escMenuIsShown = false;
			escMenuElements.clear();
			game(true);
		});

		btnMenu.setOnMouseClicked(event -> {
			escMenuIsShown = false;
			escMenuElements.clear();
			menu();
		});
	}

	public static void main(String[] args) {
		launch(args);
	}
}