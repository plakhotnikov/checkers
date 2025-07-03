package logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import model.Board;

/**
 * Класс {@code MoveGenerator} предоставляет методы для определения,
 * может ли шашка выполнить простой ход или перепрыгивание.
 */
public class MoveGenerator {

	/**
	 * Возвращает список конечных точек для возможных простых ходов от заданного индекса.
	 *
	 * @param board      доска для поиска доступных ходов.
	 * @param startIndex начальный индекс шашки (0–31).
	 * @return список точек, в которые можно переместить шашку.
	 */
	public static List<Point> getMoves(Board board, int startIndex) {
		List<Point> endPoints = new ArrayList<>();
		if (board == null || !Board.isValidIndex(startIndex)) {
			return endPoints;
		}
		int id = board.get(startIndex);
		Point p = Board.toPoint(startIndex);
		addPoints(endPoints, p, id, 1);
		for (int i = 0; i < endPoints.size(); i++) {
			Point end = endPoints.get(i);
			if (board.get(end.x, end.y) != Board.EMPTY) {
				endPoints.remove(i--);
			}
		}
		return endPoints;
	}

	/**
	 * Возвращает список конечных точек для возможных перепрыгиваний от заданного индекса.
	 *
	 * @param board      доска для поиска доступных перепрыгиваний.
	 * @param startIndex начальный индекс шашки (0–31).
	 * @return список точек, через которые можно перепрыгнуть шашку.
	 */
	public static List<Point> getSkips(Board board, int startIndex) {
		List<Point> endPoints = new ArrayList<>();
		if (board == null || !Board.isValidIndex(startIndex)) {
			return endPoints;
		}
		int id = board.get(startIndex);
		Point p = Board.toPoint(startIndex);
		addPoints(endPoints, p, id, 2);
		for (int i = 0; i < endPoints.size(); i++) {
			Point end = endPoints.get(i);
			if (!isValidSkip(board, startIndex, Board.toIndex(end))) {
				endPoints.remove(i--);
			}
		}
		return endPoints;
	}

	/**
	 * Проверяет, можно ли выполнить перепрыгивание из одной клетки в другую.
	 *
	 * @param board      доска для проверки.
	 * @param startIndex начальный индекс перепрыгивания.
	 * @param endIndex   конечный индекс перепрыгивания.
	 * @return {@code true}, если перепрыгивание допустимо.
	 */
	public static boolean isValidSkip(Board board,
									  int startIndex, int endIndex) {
		if (board == null) {
			return false;
		}
		if (board.get(endIndex) != Board.EMPTY) {
			return false;
		}
		int id = board.get(startIndex);
		int midID = board.get(
				Board.toIndex(Board.middle(startIndex, endIndex)));
		return !(id == Board.INVALID || id == Board.EMPTY
				|| midID == Board.INVALID || midID == Board.EMPTY
				|| Board.isBlackChecker(midID) ^ Board.isWhiteChecker(id));
	}

	/**
	 * Добавляет кандидатов для ходов или перепрыгиваний вокруг заданной точки.
	 *
	 * @param points список для добавления новых точек.
	 * @param p      исходная точка на доске.
	 * @param id     идентификатор шашки в исходной точке.
	 * @param delta  шаг по оси для хода (1 для простого хода, 2 для перепрыгивания).
	 */
	public static void addPoints(List<Point> points, Point p, int id, int delta) {
		boolean isKing = Board.isKingChecker(id);
		if (isKing || id == Board.BLACK_CHECKER) {
			points.add(new Point(p.x + delta, p.y + delta));
			points.add(new Point(p.x - delta, p.y + delta));
		}
		if (isKing || id == Board.WHITE_CHECKER) {
			points.add(new Point(p.x + delta, p.y - delta));
			points.add(new Point(p.x - delta, p.y - delta));
		}
	}
}
