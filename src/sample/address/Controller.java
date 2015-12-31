package sample.address;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class Controller {
	Controller(MainApp main, Pane Field, double fieldWidth, double fieldHeight,
			int Dimension) {
		this.main = main;
		this.field = Field;
		this.fieldWidth = fieldWidth;
		this.fieldHeight = fieldHeight;
		d = Dimension;
		a = new Block[Dimension][Dimension];
		m = new MotionInfo[Dimension][Dimension];
		coords = Block.getAllCoords(fieldWidth, fieldHeight, Dimension);
		refreshCounter = 0;
		gameOver = false;
		undoCount = main.maxUndoCount;
		score = 0;
		motionLock = false;
		autoMoveEnabled = false;
		mediaNotFound = false;
		states = new StateSaver(this);
		// String filename =
		// "file:///D:/must_have/eclipse/Projects/game/src/address/media/sound.mp3";
		String filename = "file:///D:/Projects/Idea/fx_sample/src/sample/address/media/vk_sound.mp3";
		try {
			Media media = new Media(filename);
			player = new MediaPlayer(media);
			player.setAutoPlay(false);
		} catch (Throwable e) {
			mediaNotFound = true;
		}
	}

	MainApp main;
	Block[][] a;
	MotionInfo[][] m;
	Point[][] coords;
	Pane field;
	double fieldWidth;
	double fieldHeight;
	int d;
	int dirX;
	int dirY;
	int[] order;
	int refreshCounter;
	public int undoCount;
	public long score;
	boolean gameOver;
	boolean motionLock;
	boolean autoMoveEnabled;
	boolean mediaNotFound;
	MediaPlayer player;
	BufferedWriter out;
	BufferedReader in;
	StateSaver states;
	String lastDirection;

	public Block spawn() {
		int number = ((int) (Math.random() * 1000)) % (d * d); // position
		int startNumber = number;
		int pos = 0;
		while (number >= 0) {
			if (pos >= d * d) {
				if (number == startNumber)
					return null;
				pos = 0;
			}
			if (a[pos / d][pos % d] == null)
				number--;
			pos++;
		}
		pos--;
		int value = (Math.random() > 0.9) ? 4 : 2;
		Block result = new Block(fieldWidth, fieldHeight, d, pos / d, pos % d,
				value);
		a[pos / d][pos % d] = result;
		return result;
	}

	public void move(String direction) {
		if (gameOver || motionLock || main.escMenuIsShown)
			return;
		motionLock = true;
		dirX = 0;
		dirY = 0;
		order = new int[d * d];
		int n = 0;

		switch (direction) {
		case "Left":
			for (int j = 0; j < d; j++)
				for (int i = 0; i < d; i++)
					order[n++] = i * d + j;
			dirX = -1;
			break;

		case "Up":
			for (int i = 0; i < d; i++)
				for (int j = 0; j < d; j++)
					order[n++] = i * d + j;
			dirY = -1;
			break;

		case "Right":
			for (int j = d - 1; j >= 0; j--)
				for (int i = 0; i < d; i++)
					order[n++] = i * d + j;
			dirX = 1;
			break;

		case "Down":
			for (int i = d - 1; i >= 0; i--)
				for (int j = 0; j < d; j++)
					order[n++] = i * d + j;
			dirY = 1;
			break;
		}

		int stayingBlocksCount = 0;
		boolean somethingMoved = false;
		for (int i = 0; i < d * d; i++) {
			int pos = order[i];
			int x = pos % d;
			int y = pos / d;

			m[y][x] = new MotionInfo();
			if (a[y][x] != null)
				m[y][x].value = a[y][x].getValue();
			else
				stayingBlocksCount--;
			if (x + dirX < 0 || x + dirX >= d || y + dirY < 0 || y + dirY >= d)
				stayingBlocksCount++;
			if (!(x + dirX < 0 || x + dirX >= d || y + dirY < 0 || y + dirY >= d)) {
				// if there where to go
				Block nextBlock = a[y + dirY][x + dirX];
				m[y][x].moveOn = m[y + dirY][x + dirX].moveOn;
				if (a[y][x] == null) {
					m[y][x].added = m[y + dirY][x + dirX].added;
					m[y][x].value = m[y + dirY][x + dirX].value;
				}
				if (nextBlock == null)
					m[y][x].moveOn++;
				if (a[y][x] != null
						&& m[y + dirY][x + dirX].value == a[y][x].getValue()
						&& !m[y + dirY][x + dirX].added) {
					m[y][x].moveOn++;
					m[y][x].added = true;
				}
				if (m[y][x].moveOn == 0)
					stayingBlocksCount++;
				if (a[y][x] == null || m[y][x].moveOn == 0)
					continue;

				somethingMoved = true;
				refreshCounter++;


				DoubleProperty currValue = (dirX != 0) ? a[y][x]
						.layoutXProperty() : a[y][x].layoutYProperty();
				Point nextCoord = coords[y + m[y][x].moveOn * dirY][x
						+ m[y][x].moveOn * dirX];
				double newValue = (dirX != 0) ? nextCoord.getX() : nextCoord
						.getY();
				Timeline timeline = new Timeline();
				timeline.setCycleCount(1);
				KeyValue kv = new KeyValue(currValue, newValue);
				KeyFrame kf = new KeyFrame(Duration.millis(100), kv);
				timeline.getKeyFrames().add(kf);
				timeline.play();

				timeline.setOnFinished(event -> {
					refreshCounter--;
					if (refreshCounter == 0)
						refresh();
				});
				lastDirection = direction;
			}
		}
		if (!somethingMoved) // all blocks stand still. we can move again
			motionLock = false;
		else if (main.soundEnabled && !mediaNotFound) {
			player.stop();
			player.play();
		}
		if (stayingBlocksCount == d * d && !thereAreMoves()) {
			gameOver = true;
			main.gameProcess();
		}
	}

	void refresh() {
		states.saveState(a, score);
		for (int i = 0; i < d * d; i++) {
			int pos = order[i];
			int x = pos % d;
			int y = pos / d;
			int step = m[y][x].moveOn;
			int newX = x + dirX * step;
			int newY = y + dirY * step;

			if (a[y][x] == null || step == 0)
				continue;
			if (!m[y][x].added) {
				a[newY][newX] = a[y][x];
			} else {
				field.getChildren().removeAll(a[newY][newX], a[y][x]);
				a[newY][newX] = new Block(fieldWidth, fieldHeight, d, newY,
						newX, m[y][x].value * 2);
				field.getChildren().add(a[newY][newX]);
				score += m[y][x].value * 2;
			}
			a[y][x] = null;
		}
		field.getChildren().add(this.spawn());
		main.gameProcess();
		writeLog();
		motionLock = false;
		if (autoMoveEnabled)
			randomMove();
	}

	void clearTurns() {
		try {
			out = new BufferedWriter(new FileWriter("turns.txt"));
			out.close();
		} catch (IOException e1) {
		}
	}

	void sort() {
		if (autoMoveEnabled || gameOver || motionLock || main.escMenuIsShown)
			return;
		motionLock = true;
		int[] values = new int[d * d];
		for (int i = 0; i < d * d; i++)
			values[i] = (a[i / d][i % d] != null) ? a[i / d][i % d].value : 0;

		InsertSort.Sort(values);

		for (int i = 0; i < d * d; i++)
			field.getChildren().remove(a[i / d][i % d]);
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < d; j++) {
				int XCoord = (i % 2 == 0) ? j : d - 1 - j;
				if (values[i * d + j] == 0)
					a[i][XCoord] = null;
				else {
					a[i][XCoord] = new Block(fieldWidth, fieldHeight, d, i,
							XCoord, values[i * d + j]);
					field.getChildren().add(a[i][XCoord]);
				}
			}
		}
		motionLock = false;
	}

	void writeLog() {
		try {
			out = new BufferedWriter(new FileWriter("log.txt"));
			out.write(d + " " + undoCount + " " + states.blocks.size() + " "
					+ score);
			out.newLine();

			for (int i = 0; i < d; i++) {
				for (int j = 0; j < d; j++)
					if (a[i][j] != null)
						out.write(a[i][j].value + " ");
					else
						out.write("0 ");
				out.newLine();
			}
			out.newLine();

			for (int k = 0; k < states.blocks.size(); k++) {
				for (int i = 0; i < d; i++) {
					for (int j = 0; j < d; j++)
						if (states.blocks.get(k)[i][j] != null)
							out.write(states.blocks.get(k)[i][j].value + " ");
						else
							out.write("0 ");
					out.newLine();
				}
				out.write(states.score.get(k).intValue() + " ");
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
		}
	}

	public void randomMove() {
		if (gameOver) {
			autoMoveEnabled = false;
			return;
		}
		switch (((int) (Math.random() * 100)) % 4) {
		case 0:
			move("Left");
			break;
		case 1:
			move("Up");
			break;
		case 2:
			move("Right");
			break;
		case 3:
			move("Down");
			break;
		}
		if (refreshCounter == 0)
			randomMove();
	}

	private boolean thereAreMoves() {
		for (int i = 0; i < d * d; i++) {
			int pos = order[i];
			int x = pos % d;
			int y = pos / d;
			if (x + 1 < d && a[y][x].getValue() == a[y][x + 1].getValue())
				return true;
			if (y + 1 < d && a[y][x].getValue() == a[y + 1][x].getValue())
				return true;
		}
		return false;
	}

	public void tryLoadGame() {
		List<Block> copy = new LinkedList<Block>();
		for (int i = 0; i < d; i++)
			for (int j = 0; j < d; j++) {
				if (a[i][j] != null)
					copy.add(a[i][j]);
			}
		field.getChildren().removeAll(copy);

		try {
			in = new BufferedReader(new FileReader("log.txt"));
			int a[] = lineParse(in.readLine());
			d = a[0];
			undoCount = a[1];
			int statesCount = a[2];
			score = a[3];

			for (int i = 0; i < d; i++) {
				a = lineParse(in.readLine());
				for (int j = 0; j < d; j++) {
					if (a[j] == 0)
						this.a[i][j] = null;
					else {
						this.a[i][j] = new Block(fieldWidth, fieldHeight, d, i,
								j, a[j]);
						field.getChildren().add(this.a[i][j]);
					}
				}
			}

			in.readLine();

			for (int k = 0; k < statesCount; k++) {
				Block[][] temp = new Block[d][d];
				for (int i = 0; i < d; i++) {
					a = lineParse(in.readLine());
					for (int j = 0; j < d; j++) {
						if (a[j] == 0)
							temp[i][j] = null;
						else {
							temp[i][j] = new Block(fieldWidth, fieldHeight, d,
									i, j, a[j]);
						}
					}
				}
				a = lineParse(in.readLine());
				states.saveState(temp, (long) a[0]);
			}
			in.close();
		} catch (Exception e) {
			copy = new LinkedList<Block>();
			for (int i = 0; i < d; i++)
				for (int j = 0; j < d; j++) {
					if (a[i][j] != null)
						copy.add(a[i][j]);
				}
			field.getChildren().removeAll(copy);
			for (int i = 0; i < 2; i++)
				field.getChildren().add(spawn());

			clearTurns();
		}
		main.gameProcess();
	}

	static int loadOption(int optionNumber) {
		int a[] = null;
		try {
			BufferedReader r = new BufferedReader(new FileReader("log.txt"));
			a = lineParse(r.readLine());
			r.close();
		} catch (Exception e) {
		}
		if (a == null)
			return -1;
		return a[optionNumber];
	}

	private static int[] lineParse(String s) throws IOException {
		if (s.length() == 0)
			return null;
		char str[] = new char[s.length()];
		s.getChars(0, s.length(), str, 0);

		List<Long> l = new LinkedList<Long>();
		Long temp = (long) 0;
		int tempShouldBeAdded = 0;
		for (int i = 0; i < s.length(); i++) {
			if ((str[i] < '0' || str[i] > '9') && (tempShouldBeAdded != 0)) {
				l.add(temp);
				temp = (long) 0;
				tempShouldBeAdded = 0;
			}
			if (str[i] >= '0' && str[i] <= '9') {
				temp *= 10;
				temp += str[i] - '0';
				tempShouldBeAdded = 1;
			}
		}
		if (tempShouldBeAdded != 0)
			l.add(temp);

		if (l.size() == 0)
			return null;
		Object tempArray[] = l.toArray();
		int a[] = new int[l.size()];
		for (int i = 0; i < l.size(); i++)
			a[i] = ((Long) tempArray[i]).intValue();

		return a;
	}
}
