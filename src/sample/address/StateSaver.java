package sample.address;

import java.util.LinkedList;
import java.util.List;

public class StateSaver {
	List<Block[][]> blocks;
	List<Long> score;
	Controller ctrl;

	StateSaver(Controller ctrl) {
		this.ctrl = ctrl;
		blocks = new LinkedList<Block[][]>();
		score = new LinkedList<Long>();
	}

	void saveState(Block[][] blockArray, Long currentScore) {
		Block[][] aTemp = new Block[ctrl.d][ctrl.d];
		for (int i = 0; i < ctrl.d; i++)
			for (int j = 0; j < ctrl.d; j++)
				aTemp[i][j] = blockArray[i][j];
		blocks.add(aTemp);
		score.add(currentScore);
		while (blocks.size() > ctrl.undoCount) {
			blocks.remove(0);
			score.remove(0);
		}
	}

	void undo() {
		if (blocks.size() == 0 || ctrl.autoMoveEnabled)
			return;

		for (int i = 0; i < ctrl.d; i++)
			for (int j = 0; j < ctrl.d; j++)
				if (ctrl.a[i][j] != null)
					ctrl.field.getChildren().remove(ctrl.a[i][j]);

		Block[][] aTemp = blocks.remove(blocks.size() - 1);
		ctrl.score = score.remove(score.size() - 1);
		ctrl.undoCount--;

		ctrl.a = new Block[ctrl.d][ctrl.d];
		for (int i = 0; i < ctrl.d; i++)
			for (int j = 0; j < ctrl.d; j++)
				ctrl.a[i][j] = aTemp[i][j];

		for (int i = 0; i < ctrl.d; i++)
			for (int j = 0; j < ctrl.d; j++)
				if (ctrl.a[i][j] != null) {
					ctrl.a[i][j].setLayoutX(Block.getCoord(ctrl.fieldWidth,
							ctrl.fieldHeight, ctrl.d, i, j).x);
					ctrl.a[i][j].setLayoutY(Block.getCoord(ctrl.fieldWidth,
							ctrl.fieldHeight, ctrl.d, i, j).y);
					ctrl.field.getChildren().add(ctrl.a[i][j]);
				}
		ctrl.main.gameProcess();
	}
}
