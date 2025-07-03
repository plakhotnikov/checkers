package model;

/**
 * Класс {@code HumanPlayer} представляет человека-игрока в игре в шашки,
 * который обновляет состояние игры через взаимодействие с пользовательским интерфейсом.
 */
public class HumanPlayer extends Player {

	/**
	 * Этот игрок — человек.
	 *
	 * @return всегда {@code true}.
	 */
	@Override
	public boolean isHuman() {
		return true;
	}

	/**
	 * Не выполняет изменений в состоянии игры,
	 * поскольку ходы осуществляются пользователем через интерфейс.
	 *
	 * @param game текущая игра (не используется).
	 */
	@Override
	public void updateGame(Game game) {}
}