package model;

import java.awt.Point;
import java.util.List;

import logic.MoveGenerator;
import logic.MoveLogic;

/**
 * Класс {@code Game} представляет игру в шашки и гарантирует,
 * что все сделанные ходы соответствуют правилам.
 */
public class Game {

	/** Текущее состояние доски. */
	private Board board;

	/** Флаг, указывающий, что сейчас ход игрока 1. */
	private boolean isP1Turn;

	/** Индекс последнего взятия для возможности нескольких взятий подряд. */
	private int skipIndex;

	public Game() {
		restart();
	}

	/**
	 * Создаёт копию этой игры так, что изменения в одной не затрагивают другую.
	 *
	 * @return точная копия текущей игры.
	 */
	public Game copy() {
		Game g = new Game();
		g.board = board.copy();
		g.isP1Turn = isP1Turn;
		g.skipIndex = skipIndex;
		return g;
	}

	/**
	 * Сбрасывает игру в начальное состояние.
	 */
	public void restart() {
		this.board = new Board();
		this.isP1Turn = false;
		this.skipIndex = -1;
	}

	/**
	 * Пытается выполнить ход с одной точки на доске в другую.
	 *
	 * @param start начальная точка хода.
	 * @param end   конечная точка хода.
	 * @return true, если состояние игры было обновлено.
	 */
	public boolean move(Point start, Point end) {
		if (start == null || end == null) {
			return false;
		}
		return move(Board.toIndex(start), Board.toIndex(end));
	}

	/**
	 * Пытается выполнить ход по индексам чёрных клеток.
	 *
	 * @param startIndex индекс начальной клетки (0–31).
	 * @param endIndex   индекс конечной клетки (0–31).
	 * @return true, если состояние игры было обновлено.
	 */
	public boolean move(int startIndex, int endIndex) {

		// Проверяем валидность хода
		if (!MoveLogic.isValidMove(this, startIndex, endIndex)) {
			return false;
		}

		// Выполняем ход
		Point middle = Board.middle(startIndex, endIndex);
		int midIndex = Board.toIndex(middle);
		this.board.set(endIndex, board.get(startIndex));
		this.board.set(midIndex, Board.EMPTY);
		this.board.set(startIndex, Board.EMPTY);

		// Превращаем шашку в дамку, если она дошла до края
		Point end = Board.toPoint(endIndex);
		int id = board.get(endIndex);
		boolean switchTurn = false;
		if (end.y == 0 && id == Board.WHITE_CHECKER) {
			this.board.set(endIndex, Board.WHITE_KING);
			switchTurn = true;
		} else if (end.y == 7 && id == Board.BLACK_CHECKER) {
			this.board.set(endIndex, Board.BLACK_KING);
			switchTurn = true;
		}

		// Определяем, меняется ли ход (нет дальнейших взятий)
		boolean midValid = Board.isValidIndex(midIndex);
		if (midValid) {
			this.skipIndex = endIndex;
		}
		if (!midValid || MoveGenerator.getSkips(board.copy(), endIndex).isEmpty()) {
			switchTurn = true;
		}
		if (switchTurn) {
			this.isP1Turn = !isP1Turn;
			this.skipIndex = -1;
		}

		return true;
	}

	/**
	 * Возвращает копию текущего состояния доски.
	 *
	 * @return копия объекта {@link Board}.
	 */
	public Board getBoard() {
		return board.copy();
	}

	/**
	 * Определяет, закончена ли игра.
	 * Игра завершается, если один из игроков не может сделать ни одного хода.
	 *
	 * @return true, если игра окончена.
	 */
	public boolean isGameOver() {

		// Если нет чёрных шашек или дамок
		List<Point> black = board.find(Board.BLACK_CHECKER);
		black.addAll(board.find(Board.BLACK_KING));
		if (black.isEmpty()) {
			return true;
		}
		// Если нет белых шашек или дамок
		List<Point> white = board.find(Board.WHITE_CHECKER);
		white.addAll(board.find(Board.WHITE_KING));
		if (white.isEmpty()) {
			return true;
		}

		// Проверяем, может ли текущий игрок сделать ход или взятие
		List<Point> test = isP1Turn ? black : white;
		for (Point p : test) {
			int i = Board.toIndex(p);
			if (!MoveGenerator.getMoves(board, i).isEmpty() ||
					!MoveGenerator.getSkips(board, i).isEmpty()) {
				return false;
			}
		}

		// Нет доступных ходов
		return true;
	}

	/**
	 * Проверяет, чей сейчас ход.
	 *
	 * @return true, если ход игрока 1.
	 */
	public boolean isP1Turn() {
		return isP1Turn;
	}

	/**
	 * Возвращает индекс последнего взятия в текущем ходе.
	 *
	 * @return индекс клетки или -1, если взятий нет.
	 */
	public int getSkipIndex() {
		return skipIndex;
	}

	/**
	 * Формирует строковое представление состояния игры,
	 * пригодное для передачи в {@link #setGameState(String)}.
	 *
	 * @return строка, описывающая текущее состояние игры.
	 */
	public String getGameState() {

		StringBuilder state = new StringBuilder();
		for (int i = 0; i < 32; i++) {
			state.append(board.get(i));
		}

		state.append(isP1Turn ? "1" : "0");
		state.append(skipIndex);

		return state.toString();
	}

	/**
	 * Устанавливает состояние игры из строки, сгенерированной
	 * {@link #getGameState()}.
	 *
	 * @param state строка с описанием состояния игры.
	 */
	public void setGameState(String state) {

		restart();

		if (state == null || state.isEmpty()) {
			return;
		}

		int n = state.length();
		for (int i = 0; i < 32 && i < n; i++) {
			try {
				int id = Integer.parseInt(String.valueOf(state.charAt(i)));
				this.board.set(i, id);
			} catch (NumberFormatException ignored) {}
		}

		if (n > 32) {
			this.isP1Turn = (state.charAt(32) == '1');
		}
		if (n > 33) {
			try {
				this.skipIndex = Integer.parseInt(state.substring(33));
			} catch (NumberFormatException e) {
				this.skipIndex = -1;
			}
		}
	}
}