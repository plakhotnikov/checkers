package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import logic.MoveGenerator;
import logic.MoveLogic;

/**
 * Класс {@code ComputerPlayer} представляет компьютерного игрока
 * и обновляет состояние доски на основе модели игры.
 */
public class ComputerPlayer extends Player {

	/* ----- ВЕСЫ ----- */
	/** Вес возможности совершить взятие. */
	private static final double WEIGHT_SKIP = 25;

	/** Вес возможности совершить взятие в следующий ход. */
	private static final double SKIP_ON_NEXT = 20;

	/** Вес для ситуации «безопасно → безопасно» (до и после хода). */
	private static final double SAFE_SAFE = 5;

	/** Вес для ситуации «безопасно → небезопасно» (до и после хода). */
	private static final double SAFE_UNSAFE = -40;

	/** Вес для ситуации «небезопасно → безопасно» (до и после хода). */
	private static final double UNSAFE_SAFE = 40;

	/** Вес для ситуации «небезопасно → небезопасно» (до и после хода). */
	private static final double UNSAFE_UNSAFE = -40;

	/** Вес шашки в безопасном положении. */
	private static final double SAFE = 3;

	/** Вес шашки в небезопасном положении. */
	private static final double UNSAFE = -5;

	/** Множитель для весов, если шашка является дамкой. */
	private static final double KING_FACTOR = 2;
	/* ------------ */

	/**
	 * Этот игрок не человек.
	 */
	@Override
	public boolean isHuman() {
		return false;
	}

	/**
	 * Обновляет состояние игры, выбирая и выполняя ход.
	 *
	 * @param game текущее состояние игры.
	 */
	@Override
	public void updateGame(Game game) {

		// Нечего делать, если игра закончена или game == null
		if (game == null || game.isGameOver()) {
			return;
		}

		// Получаем все возможные ходы
		Game copy = game.copy();
		List<Move> moves = getMoves(copy);

		// Находим наилучший вес
		int n = moves.size(), count = 1;
		double bestWeight = Move.WEIGHT_INVALID;
		for (Move m : moves) {
			getMoveWeight(copy.copy(), m);
			if (m.getWeight() > bestWeight) {
				bestWeight = m.getWeight();
				count = 1;
			} else if (m.getWeight() == bestWeight) {
				count++;
			}
		}

		// Рандомно выбираем один из лучших ходов
		int choice = ((int) (Math.random() * count)) % count;
		for (Move m : moves) {
			if (m.getWeight() == bestWeight) {
				if (choice == 0) {
					game.move(m.getStartIndex(), m.getEndIndex());
					break;
				}
				choice--;
			}
		}
	}

	/**
	 * Возвращает список всех доступных ходов и взятий для текущего игрока.
	 *
	 * @param game текущее состояние игры.
	 * @return список допустимых ходов.
	 */
	private List<Move> getMoves(Game game) {

		// Если требуется обязательное взятие
		if (game.getSkipIndex() >= 0) {
			List<Move> moves = new ArrayList<>();
			List<Point> skips = MoveGenerator.getSkips(
					game.getBoard(), game.getSkipIndex());
			for (Point end : skips) {
				moves.add(new Move(game.getSkipIndex(), Board.toIndex(end)));
			}
			return moves;
		}

		// Собираем все шашки текущего игрока
		List<Point> checkers = new ArrayList<>();
		Board b = game.getBoard();
		if (game.isP1Turn()) {
			checkers.addAll(b.find(Board.BLACK_CHECKER));
			checkers.addAll(b.find(Board.BLACK_KING));
		} else {
			checkers.addAll(b.find(Board.WHITE_CHECKER));
			checkers.addAll(b.find(Board.WHITE_KING));
		}

		// Ищем возможные взятия
		List<Move> moves = new ArrayList<>();
		for (Point checker : checkers) {
			int idx = Board.toIndex(checker);
			List<Point> skips = MoveGenerator.getSkips(b, idx);
			for (Point end : skips) {
				Move m = new Move(idx, Board.toIndex(end));
				m.changeWeight(WEIGHT_SKIP);
				moves.add(m);
			}
		}

		// Если взятий нет — добавляем обычные ходы
		if (moves.isEmpty()) {
			for (Point checker : checkers) {
				int idx = Board.toIndex(checker);
				List<Point> steps = MoveGenerator.getMoves(b, idx);
				for (Point end : steps) {
					moves.add(new Move(idx, Board.toIndex(end)));
				}
			}
		}

		return moves;
	}

	/**
	 * Вычисляет максимальную глубину последовательных взятий
	 * от указанной точки за один ход.
	 *
	 * @param game      состояние игры для проверки.
	 * @param startIndex индекс точки начала взятия.
	 * @param isP1Turn   флаг, чей сейчас ход.
	 * @return максимальное количество последовательных взятий.
	 */
	private int getSkipDepth(Game game, int startIndex, boolean isP1Turn) {

		// Если ход перешёл другому игроку, нет взятий
		if (isP1Turn != game.isP1Turn()) {
			return 0;
		}

		// Рекурсивно проверяем все варианты взятий
		List<Point> skips = MoveGenerator.getSkips(
				game.getBoard(), startIndex);
		int depth = 0;
		for (Point end : skips) {
			int endIdx = Board.toIndex(end);
			game.move(startIndex, endIdx);
			int d = getSkipDepth(game, endIdx, isP1Turn);
			if (d > depth) {
				depth = d;
			}
		}

		return depth + (skips.isEmpty() ? 0 : 1);
	}

	/**
	 * Определяет и устанавливает вес для данного хода
	 * с учётом различных факторов (безопасность, дальнейшие взятия и т.д.).
	 *
	 * @param game состояние игры перед ходом.
	 * @param m    объект хода для оценки.
	 */
	private void getMoveWeight(Game game, Move m) {

		Point start = m.getStart(), end = m.getEnd();
		int sIdx = Board.toIndex(start), eIdx = Board.toIndex(end);
		Board b = game.getBoard();
		boolean changedTurn = game.isP1Turn();
		boolean safeBefore = MoveLogic.isSafe(b, start);

		// Начальный вес по безопасности
		m.changeWeight(getSafetyWeight(b, game.isP1Turn()));

		// Пробуем сделать ход
		if (!game.move(sIdx, eIdx)) {
			m.setWeight(Move.WEIGHT_INVALID);
			return;
		}
		b = game.getBoard();
		changedTurn = (changedTurn != game.isP1Turn());
		int id = b.get(eIdx);
		boolean isKing = Board.isKingChecker(id);
		boolean safeAfter = true;

		// Если ход привёл к смене хода — проверяем возможное следующее взятие
		if (changedTurn) {
			safeAfter = MoveLogic.isSafe(b, end);
			int depth = getSkipDepth(game, eIdx, !game.isP1Turn());
			if (safeAfter) {
				m.changeWeight(SKIP_ON_NEXT * depth * depth);
			} else {
				m.changeWeight(SKIP_ON_NEXT);
			}
		} else {
			// Если ход не сменил игрока — считаем глубину текущих взятий
			int depth = getSkipDepth(game, sIdx, game.isP1Turn());
			m.changeWeight(WEIGHT_SKIP * depth * depth);
		}

		// Добавляем вес за изменения в безопасности шашки
		if (safeBefore && safeAfter) {
			m.changeWeight(SAFE_SAFE);
		} else if (!safeBefore && safeAfter) {
			m.changeWeight(UNSAFE_SAFE);
		} else if (safeBefore) {
			m.changeWeight(SAFE_UNSAFE * (isKing ? KING_FACTOR : 1));
		} else {
			m.changeWeight(UNSAFE_UNSAFE);
		}
		m.changeWeight(getSafetyWeight(
				b, changedTurn != game.isP1Turn()));
	}

	/**
	 * Вычисляет общий «вес безопасности» для шашек указанного цвета.
	 * «Безопасные» шашки дают один вес, «небезопасные» — другой,
	 * дамки — с учётом множителя.
	 *
	 * @param b       состояние доски.
	 * @param isBlack true, если оцениваются чёрные шашки; иначе — белые.
	 * @return суммарный вес безопасности.
	 */
	private double getSafetyWeight(Board b, boolean isBlack) {

		double weight = 0;
		List<Point> checkers = new ArrayList<>();
		if (isBlack) {
			checkers.addAll(b.find(Board.BLACK_CHECKER));
			checkers.addAll(b.find(Board.BLACK_KING));
		} else {
			checkers.addAll(b.find(Board.WHITE_CHECKER));
			checkers.addAll(b.find(Board.WHITE_KING));
		}

		for (Point checker : checkers) {
			int idx = Board.toIndex(checker);
			int id = b.get(idx);
			if (MoveLogic.isSafe(b, checker)) {
				weight += SAFE;
			} else {
				weight += UNSAFE * (Board.isKingChecker(id) ? KING_FACTOR : 1);
			}
		}

		return weight;
	}
}