package ui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.ComputerPlayer;
import model.HumanPlayer;
import model.Player;

/**
 * Класс {@code OptionPanel} представляет панель настроек для игры в шашки,
 * позволяя выбирать типы игроков и перезапускать игру.
 */
public class OptionPanel extends JPanel {

	@Serial
	private static final long serialVersionUID = -4763875452164030755L;

	/** Окно с игрой, которое следует обновлять при изменении опций. */
	private final CheckersWindow window;

	/** Кнопка для перезапуска игры. */
	private final JButton restartBtn;

	/** Выпадающий список для выбора типа первого игрока. */
	private final JComboBox<String> player1Opts;

	/** Выпадающий список для выбора типа второго игрока. */
	private final JComboBox<String> player2Opts;

	/**
	 * Создаёт панель опций для указанного окна с игрой.
	 *
	 * @param window окно {@link CheckersWindow}, настройками которого будет управлять эта панель.
	 */
	public OptionPanel(CheckersWindow window) {
		super(new GridLayout(0, 1));
		this.window = window;

		// Инициализация компонентов
		OptionListener ol = new OptionListener();
		final String[] playerTypeOpts = {"Человек", "Бот"};
		this.restartBtn = new JButton("Перезапустить");
		this.player1Opts = new JComboBox<>(playerTypeOpts);
		this.player2Opts = new JComboBox<>(playerTypeOpts);
		this.restartBtn.addActionListener(ol);
		this.player1Opts.addActionListener(ol);
		this.player2Opts.addActionListener(ol);

		JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel middle = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));

		// Добавление компонентов на панель
		top.add(restartBtn);
		middle.add(new JLabel("(черные) Игрок 1: "));
		middle.add(player1Opts);
		bottom.add(new JLabel("(белые) Игрок 2: "));
		bottom.add(player2Opts);
		this.add(top);
		this.add(middle);
		this.add(bottom);
	}

	/**
	 * Создаёт новый экземпляр игрока в зависимости от выбранного варианта в списке.
	 *
	 * @param playerOpts выпадающий список типа игрока.
	 * @return новый объект {@link Player} в соответствии с выбором ("Человек" или "Бот").
	 */
	private static Player getPlayer(JComboBox<String> playerOpts) {
		Player player = new HumanPlayer();
		if (playerOpts == null) {
			return player;
		}
		String type = String.valueOf(playerOpts.getSelectedItem());
		if ("Бот".equals(type)) {
			player = new ComputerPlayer();
		}
		return player;
	}

	/**
	 * Внутренний класс-слушатель, обрабатывающий события от компонентов панели опций.
	 */
	private class OptionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (window == null) {
				return;
			}
			Object src = e.getSource();
			if (src == restartBtn) {
				window.restart();
			} else if (src == player1Opts) {
				window.setPlayer1(getPlayer(player1Opts));
			} else if (src == player2Opts) {
				window.setPlayer2(getPlayer(player2Opts));
			}
		}
	}
}