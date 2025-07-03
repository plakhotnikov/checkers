package ui;

import java.awt.BorderLayout;
import java.io.Serial;

import javax.swing.JFrame;
import javax.swing.JPanel;

import model.Player;

/**
 * Класс {@code CheckersWindow} отвечает за создание и управление окном приложения.
 * Это окно содержит игру в шашки и панель настроек {@link OptionPanel}.
 */
public class CheckersWindow extends JFrame {

	@Serial
	private static final long serialVersionUID = 8782122389400590079L;

	/** Значение ширины окна по умолчанию. */
	public static final int DEFAULT_WIDTH = 1200;

	/** Значение высоты окна по умолчанию. */
	public static final int DEFAULT_HEIGHT = 1200;

	/** Заголовок окна по умолчанию. */
	public static final String DEFAULT_TITLE = "Шашки";

	/** Компонент доски, отображающий и обновляющий игру. */
	private final CheckerBoard board;

	public CheckersWindow() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_TITLE);
	}

	public CheckersWindow(int width, int height, String title) {

		// Настройка окна
		super(title);
		super.setSize(width, height);
		super.setLocationByPlatform(true);

		// Настройка компонентов
		JPanel layout = new JPanel(new BorderLayout());
		this.board = new CheckerBoard();
		OptionPanel opts = new OptionPanel(this);
		layout.add(board, BorderLayout.CENTER);
		layout.add(opts, BorderLayout.SOUTH);
		this.add(layout);
	}

	/**
	 * Устанавливает нового игрока для первого игрока и обновляет доску.
	 *
	 * @param player1 новый объект игрока для управления первым игроком.
	 */
	public void setPlayer1(Player player1) {
		this.board.setPlayer1(player1);
		this.board.update();
	}

	/**
	 * Устанавливает нового игрока для второго игрока и обновляет доску.
	 *
	 * @param player2 новый объект игрока для управления вторым игроком.
	 */
	public void setPlayer2(Player player2) {
		this.board.setPlayer2(player2);
		this.board.update();
	}

	/**
	 * Сбрасывает игру в начальное состояние и обновляет доску.
	 */
	public void restart() {
		this.board.getGame().restart();
		this.board.update();
	}
}