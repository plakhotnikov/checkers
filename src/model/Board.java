/*
 * Описание: Этот класс реализует доску для игры в шашки размером 8×8.
 * По стандартным правилам шашка может перемещаться только по чёрным клеткам,
 * поэтому доступно только 32 клетки. Для хранения состояния доски используются
 * три целых числа, по 3 бита на каждую чёрную клетку.
 */

package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс {@code Board} представляет состояние игры в шашки.
 * Стандартная доска имеет размер 8×8 (64) клеток, чередующихся белыми и чёрными.
 * Шашки могут находиться только на чёрных клетках и двигаться только по диагонали.
 * Для экономии памяти состояние доски хранится в трёх целых числах
 * (по 3 бита на каждую из 32 чёрных клеток). Это обеспечивает быструю и эффективную
 * работу методов, например {@link #copy()} для копирования состояния доски.
 * <p>
 * Для представления состояния клеток используются константы:
 * {@link #EMPTY}, {@link #BLACK_CHECKER}, {@link #WHITE_CHECKER},
 * {@link #BLACK_KING}, {@link #WHITE_KING}.
 * <p>
 * Методами {@link #get(int)} и {@link #get(int, int)} можно получить
 * текущее состояние конкретной клетки. Метод {@link #reset()} сбрасывает
 * доску в начальное состояние.
 */
public class Board {

	/** Идентификатор, указывающий на точку вне доски. */
	public static final int INVALID = -1;

	/** Идентификатор пустой клетки. */
	public static final int EMPTY = 0;

	/** Идентификатор чёрной шашки. */
	public static final int BLACK_CHECKER = 0b110;

	/** Идентификатор белой шашки. */
	public static final int WHITE_CHECKER = 0b100;

	/** Идентификатор чёрной дамки. */
	public static final int BLACK_KING = 0b111;

	/** Идентификатор белой дамки. */
	public static final int WHITE_KING = 0b101;

	/** Массив из трёх целых чисел, хранящий текущее состояние доски. */
	private int[] state;

	/**
	 * Создаёт новую доску для игры в шашки с установкой начального положения шашек.
	 */
	public Board() {
		reset();
	}

	/**
	 * Создаёт точную копию доски. Изменения в копии не повлияют на исходный объект.
	 *
	 * @return новая доска-копия с тем же состоянием.
	 */
	public Board copy() {
		Board copy = new Board();
		copy.state = state.clone();
		return copy;
	}

	/**
	 * Сбрасывает доску к начальному состоянию:
	 * чёрные шашки размещаются в первых трёх рядах сверху,
	 * белые — в трёх рядах снизу. По 12 шашек каждого цвета.
	 */
	public void reset() {
		// Reset the state
		this.state = new int[3];
		for (int i = 0; i < 12; i ++) {
			set(i, BLACK_CHECKER);
			set(31 - i, WHITE_CHECKER);
		}
	}

	/**
	 * Ищет на доске чёрные клетки с заданным идентификатором.
	 *
	 * @param id искомый идентификатор.
	 * @return список координат (Point) всех чёрных клеток с указанным идентификатором;
	 *         если таких нет, возвращается пустой список.
	 */
	public List<Point> find(int id) {
		List<Point> points = new ArrayList<>();
		for (int i = 0; i < 32; i ++) {
			if (get(i) == id) {
				points.add(toPoint(i));
			}
		}
		return points;
	}

	/**
	 * Устанавливает новый идентификатор для чёрной клетки по её индексу.
	 * Если индекс невалидный или точка не является чёрной клеткой, изменений не будет.
	 * Если id < 0, клетка станет {@link #EMPTY}.
	 *
	 * @param index индекс чёрной клетки (0–31).
	 * @param id    новый идентификатор ({@link #EMPTY}, {@link #BLACK_CHECKER},
	 *              {@link #WHITE_CHECKER}, {@link #BLACK_KING}, {@link #WHITE_KING}).
	 */
	public void set(int index, int id) {
		if (!isValidIndex(index)) {
			return;
		}
		if (id < 0) {
			id = EMPTY;
		}
		for (int i = 0; i < state.length; i ++) {
			boolean set = ((1 << (state.length - i - 1)) & id) != 0;
			this.state[i] = setBit(state[i], index, set);
		}
	}

	/**
	 * Возвращает идентификатор шашки по координатам на доске.
	 *
	 * @param x координата X (0–7).
	 * @param y координата Y (0–7).
	 * @return идентификатор шашки или {@link #INVALID},
	 *         если точка вне доски или на белой клетке.
	 */
	public int get(int x, int y) {
		return get(toIndex(x, y));
	}

	/**
	 * Возвращает идентификатор шашки по индексу чёрной клетки.
	 *
	 * @param index индекс чёрной клетки (0–31).
	 * @return идентификатор шашки или {@link #INVALID},
	 *         если индекс невалиден.
	 */
	public int get(int index) {
		if (!isValidIndex(index)) {
			return INVALID;
		}
		return getBit(state[0], index) * 4
				+ getBit(state[1], index) * 2
				+ getBit(state[2], index);
	}

	/**
	 * Преобразует индекс чёрной клетки (0–31) в координаты (x, y).
	 *
	 * @param index индекс чёрной клетки.
	 * @return точка на доске или (-1,-1), если индекс вне диапазона.
	 */
	public static Point toPoint(int index) {
		int y = index / 4;
		int x = 2 * (index % 4) + (y + 1) % 2;
		return !isValidIndex(index)
				? new Point(-1, -1)
				: new Point(x, y);
	}

	/**
	 * Преобразует координаты (x, y) в индекс чёрной клетки.
	 *
	 * @param x координата X (0–7).
	 * @param y координата Y (0–7).
	 * @return индекс чёрной клетки или -1, если точка не является чёрной клеткой.
	 */
	public static int toIndex(int x, int y) {
		if (!isValidPoint(new Point(x, y))) {
			return -1;
		}
		return y * 4 + x / 2;
	}

	/**
	 * То же, что {@link #toIndex(int, int)}, но принимает {@link Point}.
	 *
	 * @param p точка на доске.
	 * @return индекс чёрной клетки или -1, если точка невалидна.
	 */
	public static int toIndex(Point p) {
		return (p == null) ? -1 : toIndex(p.x, p.y);
	}

	/**
	 * Устанавливает или сбрасывает указанный бит в числе.
	 *
	 * @param target исходное число.
	 * @param bit    номер бита (0–31).
	 * @param set    true — установить бит, false — сбросить.
	 * @return обновлённое число.
	 */
	public static int setBit(int target, int bit, boolean set) {
		if (bit < 0 || bit > 31) {
			return target;
		}
		if (set) {
			target |= (1 << bit);
		} else {
			target &= ~(1 << bit);
		}
		return target;
	}

	/**
	 * Возвращает состояние указанного бита.
	 *
	 * @param target число.
	 * @param bit    номер бита (0–31).
	 * @return 1, если бит установлен; иначе 0.
	 */
	public static int getBit(int target, int bit) {
		if (bit < 0 || bit > 31) {
			return 0;
		}
		return (target & (1 << bit)) != 0 ? 1 : 0;
	}

	/**
	 * Находит среднюю точку между двумя чёрными клетками.
	 *
	 * @param p1 первая точка.
	 * @param p2 вторая точка.
	 * @return средняя точка или (-1,-1), если точки вне доски,
	 *         не на расстоянии 2 по x и y или на белой клетке.
	 */
	public static Point middle(Point p1, Point p2) {
		if (p1 == null || p2 == null) {
			return new Point(-1, -1);
		}
		return middle(p1.x, p1.y, p2.x, p2.y);
	}

	/**
	 * То же, что {@link #middle(Point, Point)}, но по индексам.
	 *
	 * @param index1 индекс первой точки (0–31).
	 * @param index2 индекс второй точки (0–31).
	 * @return средняя точка или (-1,-1) при ошибке.
	 */
	public static Point middle(int index1, int index2) {
		return middle(toPoint(index1), toPoint(index2));
	}

	/**
	 * То же, что {@link #middle(Point, Point)}, но по координатам.
	 *
	 * @param x1 x первой точки.
	 * @param y1 y первой точки.
	 * @param x2 x второй точки.
	 * @param y2 y второй точки.
	 * @return средняя точка или (-1,-1) при ошибке.
	 */
	public static Point middle(int x1, int y1, int x2, int y2) {
		int dx = x2 - x1, dy = y2 - y1;
		if (x1 < 0 || y1 < 0 || x2 < 0 || y2 < 0
				|| x1 > 7 || y1 > 7 || x2 > 7 || y2 > 7) {
			return new Point(-1, -1);
		} else if (x1 % 2 == y1 % 2 || x2 % 2 == y2 % 2) {
			return new Point(-1, -1);
		} else if (Math.abs(dx) != Math.abs(dy) || Math.abs(dx) != 2) {
			return new Point(-1, -1);
		}
		return new Point(x1 + dx / 2, y1 + dy / 2);
	}

	/**
	 * Проверяет, допустим ли индекс для чёрной клетки.
	 *
	 * @param testIndex индекс.
	 * @return true, если индекс в диапазоне 0–31.
	 */
	public static boolean isValidIndex(int testIndex) {
		return testIndex >= 0 && testIndex < 32;
	}

	/**
	 * Проверяет, является ли точка чёрной клеткой на доске.
	 *
	 * @param testPoint точка.
	 * @return true, если точка на доске и находится на чёрной клетке.
	 */
	public static boolean isValidPoint(Point testPoint) {
		if (testPoint == null) {
			return false;
		}
		int x = testPoint.x, y = testPoint.y;
		if (x < 0 || x > 7 || y < 0 || y > 7) {
			return false;
		}
		return x % 2 != y % 2;
	}

	/**
	 * Проверяет, является ли идентификатор чёрной шашкой.
	 *
	 * @param id идентификатор.
	 * @return true, если это {@link #BLACK_CHECKER} или {@link #BLACK_KING}.
	 */
	public static boolean isBlackChecker(int id) {
		return id == Board.BLACK_CHECKER || id == Board.BLACK_KING;
	}

	/**
	 * Проверяет, является ли идентификатор белой шашкой.
	 *
	 * @param id идентификатор.
	 * @return true, если это {@link #WHITE_CHECKER} или {@link #WHITE_KING}.
	 */
	public static boolean isWhiteChecker(int id) {
		return id == Board.WHITE_CHECKER || id == Board.WHITE_KING;
	}

	/**
	 * Проверяет, является ли шашка дамкой.
	 *
	 * @param id идентификатор.
	 * @return true, если это {@link #BLACK_KING} или {@link #WHITE_KING}.
	 */
	public static boolean isKingChecker(int id) {
		return id == Board.BLACK_KING || id == Board.WHITE_KING;
	}

	@Override
	public String toString() {
		StringBuilder obj = new StringBuilder(getClass().getName() + "[");
		for (int i = 0; i < 31; i ++) {
			obj.append(get(i)).append(", ");
		}
		obj.append(get(31));
		return obj + "]";
	}
}