/*
 * Описание: Этот класс представляет ход.
 */

package model;

import java.awt.Point;

/**
 * Класс {@code Move} представляет один ход и хранит связанный с ним вес.
 */
public class Move {

	/** Вес, соответствующий невалидному ходу. */
	public static final double WEIGHT_INVALID = Double.NEGATIVE_INFINITY;

	/** Индекс начальной клетки хода. */
	private byte startIndex;

	/** Индекс конечной клетки хода. */
	private byte endIndex;

	/** Вес, присвоенный этому ходу. */
	private double weight;

	/**
	 * Создаёт объект хода с указанными стартовым и конечным индексами.
	 *
	 * @param startIndex индекс начальной клетки (0–31).
	 * @param endIndex   индекс конечной клетки (0–31).
	 */
	public Move(int startIndex, int endIndex) {
		setStartIndex(startIndex);
		setEndIndex(endIndex);
	}

	/**
	 * Возвращает индекс начальной клетки хода.
	 *
	 * @return индекс начальной клетки.
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * Устанавливает индекс начальной клетки хода.
	 *
	 * @param startIndex новый индекс начальной клетки.
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = (byte) startIndex;
	}

	/**
	 * Возвращает индекс конечной клетки хода.
	 *
	 * @return индекс конечной клетки.
	 */
	public int getEndIndex() {
		return endIndex;
	}

	/**
	 * Устанавливает индекс конечной клетки хода.
	 *
	 * @param endIndex новый индекс конечной клетки.
	 */
	public void setEndIndex(int endIndex) {
		this.endIndex = (byte) endIndex;
	}

	/**
	 * Возвращает координаты начальной клетки в виде {@link Point}.
	 *
	 * @return точка, соответствующая стартовой позиции хода.
	 */
	public Point getStart() {
		return Board.toPoint(startIndex);
	}

	/**
	 * Возвращает координаты конечной клетки в виде {@link Point}.
	 *
	 * @return точка, соответствующая конечной позиции хода.
	 */
	public Point getEnd() {
		return Board.toPoint(endIndex);
	}

	/**
	 * Возвращает текущий вес хода.
	 *
	 * @return вес хода.
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Устанавливает вес хода.
	 *
	 * @param weight новый вес.
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * Изменяет вес хода на заданную величину.
	 *
	 * @param delta приращение веса.
	 */
	public void changeWeight(double delta) {
		this.weight += delta;
	}

	/**
	 * Возвращает строковое представление объекта {@code Move},
	 * включающее стартовый и конечный индексы и текущий вес.
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName()
				+ "[startIndex=" + startIndex
				+ ", endIndex=" + endIndex
				+ ", weight=" + weight + "]";
	}
}