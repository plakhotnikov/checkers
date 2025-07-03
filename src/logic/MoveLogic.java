package logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import model.Board;
import model.Game;

/**
 * Класс {@code MoveLogic} определяет, какой ход считается допустимым.
 * Полностью реализует все правила игры в шашки.
 */
public class MoveLogic {

	/**
	 * Проверяет, является ли указанный ход допустимым в соответствии с правилами шашек.
	 *
	 * @param game        проверяемая игра.
	 * @param startIndex  начальный индекс хода.
	 * @param endIndex    конечный индекс хода.
	 * @return {@code true}, если ход легален по правилам шашек.
	 */
	public static boolean isValidMove(Game game,
									  int startIndex, int endIndex) {
		return game != null && isValidMove(
				game.getBoard(), game.isP1Turn(),
				startIndex, endIndex, game.getSkipIndex());
	}

	/**
	 * Проверяет, является ли указанный ход допустимым в соответствии с правилами шашек.
	 *
	 * @param board       текущая доска для проверки.
	 * @param isP1Turn    {@code true}, если ходит первый игрок.
	 * @param startIndex  начальный индекс хода.
	 * @param endIndex    конечный индекс хода.
	 * @param skipIndex   индекс последнего перепрыгивания в этом ходу.
	 * @return {@code true}, если ход легален по правилам шашек.
	 */
	public static boolean isValidMove(Board board, boolean isP1Turn,
									  int startIndex, int endIndex, int skipIndex) {

		// Базовые проверки
		if (board == null || !Board.isValidIndex(startIndex)
				|| !Board.isValidIndex(endIndex)) {
			return false;
		} else if (startIndex == endIndex) {
			return false;
		} else if (Board.isValidIndex(skipIndex) && skipIndex != startIndex) {
			return false;
		}

		// Проверка идентификаторов
		if (!validateIDs(board, isP1Turn, startIndex, endIndex)) {
			return false;
		} else {
			// Проверка расстояния хода
			return validateDistance(board, isP1Turn, startIndex, endIndex);
		}
	}

	/**
	 * Проверяет корректность идентификаторов шашек в начальной, конечной
	 * и средней (для перепрыгивания) клетках.
	 *
	 * @param board       текущая доска для проверки.
	 * @param isP1Turn    {@code true}, если ходит первый игрок.
	 * @param startIndex  начальный индекс хода.
	 * @param endIndex    конечный индекс хода.
	 * @return {@code true}, если все идентификаторы корректны.
	 */
	private static boolean validateIDs(Board board, boolean isP1Turn,
									   int startIndex, int endIndex) {

		// Проверка, что целевая клетка пуста
		if (board.get(endIndex) != Board.EMPTY) {
			return false;
		}

		// Проверка цвета шашки в начальной клетке
		int id = board.get(startIndex);
		if ((isP1Turn && !Board.isBlackChecker(id))
				|| (!isP1Turn && !Board.isWhiteChecker(id))) {
			return false;
		}

		// Проверка средней клетки при перепрыгивании
		Point middle = Board.middle(startIndex, endIndex);
		int midID = board.get(Board.toIndex(middle));
		return midID == Board.INVALID
				|| ((isP1Turn || Board.isBlackChecker(midID))
				&& (!isP1Turn || Board.isWhiteChecker(midID)));
	}

	/**
	 * Проверяет, что ход по диагонали имеет длину 1 или 2 и выполнен в
	 * допустимом направлении. Если длина 1, также проверяется, что у других
	 * шашек игрока нет обязательного перепрыгивания.
	 *
	 * @param board       текущая доска для проверки.
	 * @param isP1Turn    {@code true}, если ходит первый игрок.
	 * @param startIndex  начальный индекс хода.
	 * @param endIndex    конечный индекс хода.
	 * @return {@code true}, если расстояние хода корректно.
	 */
	private static boolean validateDistance(Board board, boolean isP1Turn,
											int startIndex, int endIndex) {

		Point start = Board.toPoint(startIndex);
		Point end = Board.toPoint(endIndex);
		int dx = end.x - start.x;
		int dy = end.y - start.y;

		// Проверка диагональности и длины
		if (Math.abs(dx) != Math.abs(dy) || Math.abs(dx) > 2 || dx == 0) {
			return false;
		}

		// Проверка направления хода
		int id = board.get(startIndex);
		if ((id == Board.WHITE_CHECKER && dy > 0)
				|| (id == Board.BLACK_CHECKER && dy < 0)) {
			return false;
		}

		// Если не перепрыгивание, проверяем отсутствие доступных перепрыгиваний
		Point middle = Board.middle(startIndex, endIndex);
		int midID = board.get(Board.toIndex(middle));
		if (midID < 0) {
			List<Point> checkers = new ArrayList<>();
			if (isP1Turn) {
				checkers.addAll(board.find(Board.BLACK_CHECKER));
				checkers.addAll(board.find(Board.BLACK_KING));
			} else {
				checkers.addAll(board.find(Board.WHITE_CHECKER));
				checkers.addAll(board.find(Board.WHITE_KING));
			}
			for (Point p : checkers) {
				int idx = Board.toIndex(p);
				if (!MoveGenerator.getSkips(board, idx).isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Определяет, безопасна ли шашка (то есть не может быть перепрыгнута
	 * соперником).
	 *
	 * @param board   текущее состояние доски.
	 * @param checker расположение шашки для проверки.
	 * @return {@code true}, если шашка безопасна.
	 */
	public static boolean isSafe(Board board, Point checker) {

		if (board == null || checker == null) {
			return true;
		}
		int index = Board.toIndex(checker);
		if (index < 0) {
			return true;
		}
		int id = board.get(index);
		if (id == Board.EMPTY) {
			return true;
		}

		boolean isBlack = Board.isBlackChecker(id);
		List<Point> check = new ArrayList<>();
		MoveGenerator.addPoints(check, checker, Board.BLACK_KING, 1);
		for (Point p : check) {
			int start = Board.toIndex(p);
			int tid = board.get(start);
			if (tid == Board.EMPTY || tid == Board.INVALID) {
				continue;
			}
			boolean isWhite = Board.isWhiteChecker(tid);
			if (isBlack && !isWhite) {
				continue;
			}
			int dx = (checker.x - p.x) * 2;
			int dy = (checker.y - p.y) * 2;
			if (!Board.isKingChecker(tid) && (isWhite ^ (dy < 0))) {
				continue;
			}
			int endIndex = Board.toIndex(new Point(p.x + dx, p.y + dy));
			if (MoveGenerator.isValidSkip(board, start, endIndex)) {
				return false;
			}
		}
		return true;
	}
}
