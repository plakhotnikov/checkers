package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.List;

import javax.swing.JButton;
import javax.swing.Timer;

import logic.MoveGenerator;
import model.Board;
import model.Game;
import model.HumanPlayer;
import model.Player;

/**
 * Класс {@code CheckerBoard} представляет графический компонент пользовательского интерфейса,
 * способный отображать текущее состояние игры в шашки. Также обрабатывает ходы игроков:
 * для человека — взаимодействие и выбор клеток на доске; для не-человеческого игрока —
 * автоматический ход через логику объекта {@link Player}.
 */
public class CheckerBoard extends JButton {

	@Serial
	private static final long serialVersionUID = -6014690893709316364L;

	/** Количество миллисекунд до хода компьютерного игрока. */
	private static final int TIMER_DELAY = 1000;

	/** Отступ в пикселях между границей компонента и самой доской. */
	private static final int PADDING = 16;

	/** Игра в шашки, отображаемая этим компонентом. */
	private final Game game;

	/** Игрок, управляющий чёрными шашками. */
	private Player player1;

	/** Игрок, управляющий белыми шашками. */
	private Player player2;

	/** Последняя точка, выбранная текущим игроком на доске. */
	private Point selected;

	/**
	 * Флаг для определения цвета выделения: зелёный, если выделение валидно,
	 * или красный, если нет.
	 */
	private boolean selectionValid;

	/** Цвет светлых клеток (по умолчанию белый). */
	private final Color lightTile;

	/** Цвет тёмных клеток (по умолчанию чёрно-коричневый). */
	private final Color darkTile;

	/** Флаг, указывающий, окончена ли игра. */
	private boolean isGameOver;

	/** Таймер для контроля задержки хода компьютерного игрока. */
	private Timer timer;

	public CheckerBoard() {
		this(new Game(), null, null);
	}

	public CheckerBoard(Game game,
						Player player1, Player player2) {

		// Настройка компонента
		super.setBorderPainted(false);
		super.setFocusPainted(false);
		super.setContentAreaFilled(false);
		super.setBackground(Color.GRAY);
		this.addActionListener(new ClickListener());

		// Настройка игры
		this.game = (game == null) ? new Game() : game;
		this.lightTile = Color.WHITE;
		this.darkTile = new Color(115, 70, 28);
		setPlayer1(player1);
		setPlayer2(player2);
	}

	/**
	 * Проверяет, окончена ли игра, и перерисовывает компонент.
	 */
	public void update() {
		runPlayer();
		this.isGameOver = game.isGameOver();
		repaint();
	}

	private void runPlayer() {
		Player player = getCurrentPlayer();
		if (player == null || player.isHuman()) {
			return;
		}
		this.timer = new Timer(TIMER_DELAY, _ -> {
			getCurrentPlayer().updateGame(game);
			timer.stop();
			update();
		});
		this.timer.start();
	}

	public synchronized void setGameState(boolean testValue,
										  String newState, String expected) {
		if (testValue && !game.getGameState().equals(expected)) {
			return;
		}
		this.game.setGameState(newState);
		repaint();
	}

	/**
	 * Отрисовывает текущее состояние игры в шашки.
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Game game = this.game.copy();

		// Вычисляем размеры
		final int BOX_PADDING = 4;
		final int W = getWidth(), H = getHeight();
		final int DIM = Math.min(W, H), BOX_SIZE = (DIM - 2 * PADDING) / 8;
		final int OFFSET_X = (W - BOX_SIZE * 8) / 2;
		final int OFFSET_Y = (H - BOX_SIZE * 8) / 2;
		final int CHECKER_SIZE = Math.max(0, BOX_SIZE - 2 * BOX_PADDING);

		// Рисуем поле
		g.setColor(Color.BLACK);
		g.drawRect(OFFSET_X - 1, OFFSET_Y - 1, BOX_SIZE * 8 + 1, BOX_SIZE * 8 + 1);
		g.setColor(lightTile);
		g.fillRect(OFFSET_X, OFFSET_Y, BOX_SIZE * 8, BOX_SIZE * 8);
		g.setColor(darkTile);
		for (int y = 0; y < 8; y++) {
			for (int x = (y + 1) % 2; x < 8; x += 2) {
				g.fillRect(OFFSET_X + x * BOX_SIZE, OFFSET_Y + y * BOX_SIZE,
						BOX_SIZE, BOX_SIZE);
			}
		}

		// Подсвечиваем выбранную клетку
		if (Board.isValidPoint(selected)) {
			g.setColor(selectionValid ? Color.GREEN : Color.RED);
			g.fillRect(OFFSET_X + selected.x * BOX_SIZE,
					OFFSET_Y + selected.y * BOX_SIZE,
					BOX_SIZE, BOX_SIZE);
		}

		// Рисуем шашки
		Board b = game.getBoard();
		for (int y = 0; y < 8; y++) {
			int cy = OFFSET_Y + y * BOX_SIZE + BOX_PADDING;
			for (int x = (y + 1) % 2; x < 8; x += 2) {
				int id = b.get(x, y);
				if (id == Board.EMPTY) {
					continue;
				}
				int cx = OFFSET_X + x * BOX_SIZE + BOX_PADDING;

				// Чёрная шашка
				if (id == Board.BLACK_CHECKER) {
					g.setColor(Color.DARK_GRAY);
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.LIGHT_GRAY);
					g.drawOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.BLACK);
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.LIGHT_GRAY);
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
				}
				// Чёрная дамка
				else if (id == Board.BLACK_KING) {
					g.setColor(Color.DARK_GRAY);
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.LIGHT_GRAY);
					g.drawOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.DARK_GRAY);
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.LIGHT_GRAY);
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.BLACK);
					g.fillOval(cx - 1, cy - 2, CHECKER_SIZE, CHECKER_SIZE);
				}
				// Белая шашка
				else if (id == Board.WHITE_CHECKER) {
					g.setColor(Color.LIGHT_GRAY);
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.DARK_GRAY);
					g.drawOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.WHITE);
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.DARK_GRAY);
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
				}
				// Белая дамка
				else if (id == Board.WHITE_KING) {
					g.setColor(Color.LIGHT_GRAY);
					g.fillOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.DARK_GRAY);
					g.drawOval(cx + 1, cy + 2, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.LIGHT_GRAY);
					g.fillOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.DARK_GRAY);
					g.drawOval(cx, cy, CHECKER_SIZE, CHECKER_SIZE);
					g.setColor(Color.WHITE);
					g.fillOval(cx - 1, cy - 2, CHECKER_SIZE, CHECKER_SIZE);
				}
				// Дополнительные обводки для дамок
				if (Board.isKingChecker(id)) {
					g.setColor(new Color(255, 240, 0));
					g.drawOval(cx - 1, cy - 2, CHECKER_SIZE, CHECKER_SIZE);
					g.drawOval(cx + 1, cy, CHECKER_SIZE - 4, CHECKER_SIZE - 4);
				}
			}
		}

		// Индикатор хода игрока
		String msg = game.isP1Turn() ? "Player 1's turn" : "Player 2's turn";
		int width = g.getFontMetrics().stringWidth(msg);
		Color back = game.isP1Turn() ? Color.BLACK : Color.WHITE;
		Color front = game.isP1Turn() ? Color.WHITE : Color.BLACK;
		g.setColor(back);
		g.fillRect(W / 2 - width / 2 - 5, OFFSET_Y + 8 * BOX_SIZE + 2,
				width + 10, 15);
		g.setColor(front);
		g.drawString(msg, W / 2 - width / 2, OFFSET_Y + 8 * BOX_SIZE + 2 + 12);

		// Надпись "Game Over!"
		if (isGameOver) {
			g.setFont(new Font("Arial", Font.BOLD, 20));
			msg = "Game Over!";
			width = g.getFontMetrics().stringWidth(msg);
			g.setColor(new Color(240, 240, 255));
			g.fillRoundRect(W / 2 - width / 2 - 5,
					OFFSET_Y + BOX_SIZE * 4 - 16,
					width + 10, 30, 10, 10);
			g.setColor(Color.RED);
			g.drawString(msg, W / 2 - width / 2, OFFSET_Y + BOX_SIZE * 4 + 7);
		}
	}

	public Game getGame() {
		return game;
	}

	public void setPlayer1(Player player1) {
		this.player1 = (player1 == null) ? new HumanPlayer() : player1;
		if (game.isP1Turn() && !this.player1.isHuman()) {
			this.selected = null;
		}
	}

	public void setPlayer2(Player player2) {
		this.player2 = (player2 == null) ? new HumanPlayer() : player2;
		if (!game.isP1Turn() && !this.player2.isHuman()) {
			this.selected = null;
		}
	}

	public Player getCurrentPlayer() {
		return game.isP1Turn() ? player1 : player2;
	}

	/**
	 * Обрабатывает клик по компоненту в указанных координатах.
	 * Если текущий игрок не человек, ничего не происходит.
	 * В противном случае обновляет выбранную точку и, если оба клика
	 * были по чёрным клеткам, пытается выполнить ход.
	 *
	 * @param x координата X клика внутри компонента.
	 * @param y координата Y клика внутри компонента.
	 */
	private void handleClick(int x, int y) {

		if (isGameOver || !getCurrentPlayer().isHuman()) {
			return;
		}

		Game copy = game.copy();

		final int W = getWidth(), H = getHeight();
		final int DIM = Math.min(W, H), BOX_SIZE = (DIM - 2 * PADDING) / 8;
		final int OFFSET_X = (W - BOX_SIZE * 8) / 2;
		final int OFFSET_Y = (H - BOX_SIZE * 8) / 2;
		x = (x - OFFSET_X) / BOX_SIZE;
		y = (y - OFFSET_Y) / BOX_SIZE;
		Point sel = new Point(x, y);

		if (Board.isValidPoint(sel) && Board.isValidPoint(selected)) {
			boolean change = copy.isP1Turn();
			String expected = copy.getGameState();
			boolean move = copy.move(selected, sel);
			if (move) {
				setGameState(true, copy.getGameState(), expected);
			}
			change = (copy.isP1Turn() != change);
			this.selected = change ? null : sel;
		} else {
			this.selected = sel;
		}

		this.selectionValid = isValidSelection(
				copy.getBoard(), copy.isP1Turn(), selected);

		update();
	}

	/**
	 * Проверяет, является ли выбранная точка допустимой в контексте текущего
	 * хода.
	 *
	 * @param b текущее состояние доски.
	 * @param isP1Turn флаг, указывающий, что сейчас ход игрока 1.
	 * @param selected проверяемая точка.
	 * @return {@code true}, если в выбранной точке находится шашка,
	 *         которая может совершить ход в текущем ходе.
	 */
	private boolean isValidSelection(Board b, boolean isP1Turn, Point selected) {

		int i = Board.toIndex(selected), id = b.get(i);
		if (id == Board.EMPTY || id == Board.INVALID) {
			return false;
		} else if (isP1Turn ^ Board.isBlackChecker(id)) {
			return false;
		} else if (!MoveGenerator.getSkips(b, i).isEmpty()) {
			return true;
		} else if (MoveGenerator.getMoves(b, i).isEmpty()) {
			return false;
		}

		List<Point> points = b.find(
				isP1Turn ? Board.BLACK_CHECKER : Board.WHITE_CHECKER);
		points.addAll(b.find(
				isP1Turn ? Board.BLACK_KING : Board.WHITE_KING));
		for (Point p : points) {
			int checker = Board.toIndex(p);
			if (checker == i) {
				continue;
			}
			if (!MoveGenerator.getSkips(b, checker).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Класс {@code ClickListener} отвечает за обработку событий клика
	 * по компоненту CheckerBoard. Использует координаты мыши относительно
	 * компонента.
	 */
	private class ClickListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Point m = CheckerBoard.this.getMousePosition();
			if (m != null) {
				handleClick(m.x, m.y);
			}
		}
	}
}