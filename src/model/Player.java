package model;

/**
 * Абстрактный класс {@code Player} представляет игрока в игре в шашки.
 */
public abstract class Player {

	/**
	 * Определяет способ выполнения хода.
	 *
	 * @return {@code true}, если игрок — человек и ход выполняется через
	 *         пользовательский интерфейс; {@code false}, если ход
	 *         автоматически выполняется методом {@link #updateGame(Game)}.
	 */
	public abstract boolean isHuman();

	/**
	 * Обновляет состояние игры, выполняя ход за данного игрока.
	 * Может включать серию обязательных взятий за один вызов.
	 *
	 * @param game игра для обновления.
	 */
	public abstract void updateGame(Game game);

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[isHuman=" + isHuman() + "]";
	}
}