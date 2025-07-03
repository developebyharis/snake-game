package src.main.java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class TwoPlayerSnakeGame extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int UNIT_SIZE = 25;
    private static final int DELAY = 180;
    private static final Color[] FOOD_COLORS = { Color.RED, Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK,
            Color.YELLOW };
    private static final Color[] SNAKE_COLORS = { new Color(76, 175, 80), new Color(33, 150, 243) };
    private static final Color BORDER_COLOR = new Color(255, 215, 0);
    private static final Color PANEL_BG = new Color(40, 40, 60);
    private static final Color SCORE_BG = new Color(0, 0, 0, 120);

    private final List<Point> food = new ArrayList<>();
    private int foodCount;
    private final Random random = new Random();
    private final List<Point>[] snakes = new ArrayList[2];
    private final int[] directions = new int[2]; // 0=up, 1=right, 2=down, 3=left
    private boolean[] alive = { true, true };
    private boolean running = false;
    private boolean paused = false;
    private boolean started = false;
    private javax.swing.Timer timer;
    private int[] scores = { 0, 0 };
    private int[] highScores = { 0, 0 };
    private JButton startButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton historyButton;
    private boolean resultSaved = false;
    private String player1Name = "Player 1";
    private String player2Name = "Player 2";
    private JButton restartButton;
    private JButton exitButton;

    public TwoPlayerSnakeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(PANEL_BG);
        setFocusable(true);
        addKeyListener(this);
        showStartScreen();
    }

    private void showStartScreen() {
        removeRestartExitButtons();
        started = false;
        running = false;
        paused = false;
        if (startButton == null) {
            startButton = new JButton("Start");
            startButton.setFont(new Font("Arial", Font.BOLD, 28));
            startButton.setFocusPainted(false);
            startButton.setBackground(new Color(60, 120, 200));
            startButton.setForeground(Color.WHITE);
            startButton.setBounds(WIDTH / 2 - 80, HEIGHT / 2 + 80, 160, 50);
            startButton.addActionListener(e -> {
                remove(startButton);
                startButton = null;
                if (historyButton != null) {
                    remove(historyButton);
                    historyButton = null;
                }
                startGame();
            });
            setLayout(null);
            add(startButton);
        }
        if (historyButton == null) {
            historyButton = new JButton("History");
            historyButton.setFont(new Font("Arial", Font.BOLD, 20));
            historyButton.setFocusPainted(false);
            historyButton.setBackground(new Color(100, 200, 100));
            historyButton.setForeground(Color.WHITE);
            historyButton.setBounds(WIDTH / 2 - 80, HEIGHT / 2 + 140, 160, 40);
            historyButton.addActionListener(e -> showHistoryDialog());
            setLayout(null);
            add(historyButton);
        }
        if (pauseButton != null) {
            remove(pauseButton);
            pauseButton = null;
        }
        repaint();
        requestFocusInWindow(); // if not use this then key will not listen
    }

    private void showPauseButton() {
        if (pauseButton == null) {
            pauseButton = new JButton("Pause");
            pauseButton.setFont(new Font("Arial", Font.BOLD, 20));
            pauseButton.setFocusPainted(false);
            pauseButton.setBackground(new Color(255, 193, 7));
            pauseButton.setForeground(Color.BLACK);
            pauseButton.setBounds(WIDTH / 2 - 60, 10, 120, 40);
            pauseButton.addActionListener(e -> {
                paused = true;
                removePauseButton();
                showResumeButton();
                repaint();
            });
            setLayout(null);
            add(pauseButton);
        }
    }

    private void removePauseButton() {
        if (pauseButton != null) {
            remove(pauseButton);
            pauseButton = null;
        }
    }

    private void showResumeButton() {
        if (resumeButton == null) {
            resumeButton = new JButton("Resume");
            resumeButton.setFont(new Font("Arial", Font.BOLD, 20));
            resumeButton.setFocusPainted(false);
            resumeButton.setBackground(new Color(255, 193, 7));
            resumeButton.setForeground(Color.BLACK);
            resumeButton.setBounds(WIDTH / 2 - 60, 10, 120, 40);
            resumeButton.addActionListener(e -> {
                paused = false;
                removeResumeButton();
                showPauseButton();
                repaint();
            });
            setLayout(null);
            add(resumeButton);
        }
    }

    private void removeResumeButton() {
        if (resumeButton != null) {
            remove(resumeButton);
            resumeButton = null;
        }
    }

    public void startGame() {
        removeRestartExitButtons();
        if (startButton != null) {
            remove(startButton);
            startButton = null;
        }
        if (historyButton != null) {
            remove(historyButton);
            historyButton = null;
        }
        // Prompt for player names
        player1Name = JOptionPane.showInputDialog(this, "Enter Player 1 Name:", player1Name);
        if (player1Name == null || player1Name.trim().isEmpty())
            player1Name = "Player 1";
        player2Name = JOptionPane.showInputDialog(this, "Enter Player 2 Name:", player2Name);
        if (player2Name == null || player2Name.trim().isEmpty())
            player2Name = "Player 2";
        snakes[0] = new ArrayList<>();
        snakes[1] = new ArrayList<>();
        snakes[0].add(new Point(WIDTH / (4 * UNIT_SIZE) * UNIT_SIZE, HEIGHT / 2));
        snakes[1].add(new Point(3 * WIDTH / (4 * UNIT_SIZE) * UNIT_SIZE, HEIGHT / 2));
        directions[0] = 1; // right
        directions[1] = 3; // left
        alive[0] = alive[1] = true;
        scores[0] = scores[1] = 0;
        food.clear();
        foodCount = 5 + random.nextInt(6); // 5-10 food
        for (int i = 0; i < foodCount; i++) {
            spawnFood();
        }
        running = true;
        started = true;
        paused = false;
        // Start with slow speed
        timer = new javax.swing.Timer(DELAY, this); // if remove this it will give timer(int) undefined error and the
                                                    // game will not start
        timer.start();
        showPauseButton();
        resultSaved = false;
        repaint();
        requestFocusInWindow();
    }

    private void spawnFood() {
        Point p;
        do {
            p = new Point(random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE,
                    random.nextInt(HEIGHT / UNIT_SIZE) * UNIT_SIZE);
        } while (food.contains(p) || snakes[0].contains(p) || snakes[1].contains(p));
        food.add(p);
    }

    // Main paint method
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!started) {
            drawStartScreen(g);
        } else if (paused) {
            drawPauseScreen(g);
        } else {
            drawGame(g);
        }
    }

    private void drawStartScreen(Graphics g) {
        drawGradientBackground(g);
        g.setFont(new Font("Arial", Font.BOLD, 54));
        g.setColor(Color.YELLOW);
        g.drawString("2-Player Snake!", WIDTH / 2 - 220, HEIGHT / 2 - 60);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        g.drawString("Player 1: WASD", WIDTH / 2 - 120, HEIGHT / 2);
        g.drawString("Player 2: Arrow Keys", WIDTH / 2 - 120, HEIGHT / 2 + 40);
        g.setColor(Color.CYAN);
    }

    private void drawPauseScreen(Graphics g) {
        drawGame(g);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.setColor(Color.ORANGE);
        g.drawString("Paused", WIDTH / 2 - 90, HEIGHT / 2);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        g.drawString("Press Resume to continue", WIDTH / 2 - 140, HEIGHT / 2 + 40);
    }

    // Draw the main game
    private void drawGame(Graphics g) {
        drawGradientBackground(g);
        drawBorder(g);
        drawScorePanel(g);
        long time = System.currentTimeMillis();
        for (int i = 0; i < food.size(); i++) {
            int pulse = (int) (6 * Math.abs(Math.sin((time + i * 100) / 300.0)));
            g.setColor(FOOD_COLORS[i % FOOD_COLORS.length]);
            Point f = food.get(i);
            g.fillOval(f.x - pulse / 2, f.y - pulse / 2, UNIT_SIZE + pulse, UNIT_SIZE + pulse);
        }
        for (int s = 0; s < 2; s++) {
            for (int i = 0; i < snakes[s].size(); i++) {
                if (i == 0) {
                    g.setColor(SNAKE_COLORS[s].darker()); // Head
                } else {
                    g.setColor(SNAKE_COLORS[s]); // Body
                }
                Point p = snakes[s].get(i);
                g.fillRoundRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE, 10, 10);
            }
        }
        if (!alive[0] || !alive[1]) {
            removePauseButton();
            removeResumeButton();
            showRestartExitButtons();
            if (!resultSaved) {
                String winner;
                if (!alive[0] && !alive[1])
                    winner = "Draw";
                else if (!alive[0])
                    winner = player2Name;
                else
                    winner = player1Name;
                DatabaseUtil.saveGameResult(player1Name, player2Name, scores[0], scores[1], winner);
                resultSaved = true;
            }
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(Color.YELLOW);
            g.drawString("Game Over!", WIDTH / 2 - 140, HEIGHT / 2 - 20);

            g.setFont(new Font("Arial", Font.BOLD, 32));
            if (!alive[0] && !alive[1]) {
                g.setColor(Color.LIGHT_GRAY);
                g.drawString("It's a Draw!", WIDTH / 2 - 100, HEIGHT / 2 + 20);
            } else if (!alive[0]) {
                g.setColor(new Color(33, 150, 243));
                g.drawString(player2Name + " Wins!", WIDTH / 2 - 120, HEIGHT / 2 + 20);
            } else if (!alive[1]) {
                g.setColor(new Color(76, 175, 80));
                g.drawString(player1Name + " Wins!", WIDTH / 2 - 120, HEIGHT / 2 + 20);
            }
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.setColor(Color.CYAN);
        }
    }

    private void drawGradientBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, new Color(60, 60, 120), WIDTH, HEIGHT, new Color(30, 30, 30));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawBorder(Graphics g) {
        g.setColor(BORDER_COLOR);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
        g.drawRect(1, 1, WIDTH - 3, HEIGHT - 3);
    }

    private void drawScorePanel(Graphics g) {
        g.setColor(SCORE_BG);
        g.fillRoundRect(10, 10, 260, 50, 20, 20);
        g.fillRoundRect(WIDTH - 270, 10, 260, 50, 20, 20);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString(player1Name + ": " + scores[0], 30, 45);
        g.drawString(player2Name + ": " + scores[1], WIDTH - 250, 45);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.YELLOW);
        g.drawString("High: " + highScores[0], 180, 45);
        g.drawString("High: " + highScores[1], WIDTH - 90, 45);
    }

    private int getCurrentDelay() {
        int maxScore = Math.max(scores[0], scores[1]);
        return Math.max(40, DELAY - maxScore * 5);
    }

    private void move(int s) {
        if (!alive[s])
            return;
        Point head = new Point(snakes[s].get(0));
        switch (directions[s]) {
            case 0:
                head.y -= UNIT_SIZE;
                break;
            case 1:
                head.x += UNIT_SIZE;
                break;
            case 2:
                head.y += UNIT_SIZE;
                break;
            case 3:
                head.x -= UNIT_SIZE;
                break;
        }
        // Check wall collision
        if (head.x < 0 || head.x >= WIDTH || head.y < 0 || head.y >= HEIGHT) {
            alive[s] = false;
            return;
        }
        // Check self collision
        if (snakes[s].contains(head)) {
            alive[s] = false;
            return;
        }
        // Check collision with other snake
        if (snakes[1 - s].contains(head)) {
            alive[s] = false;
            return;
        }
        // Move
        snakes[s].add(0, head);
        if (food.contains(head)) {
            food.remove(head);
            scores[s]++;
            if (scores[s] > highScores[s])
                highScores[s] = scores[s];
            spawnFood();
            int newDelay = getCurrentDelay();
            if (timer.getDelay() != newDelay) {
                timer.setDelay(newDelay);
            }
        } else {
            snakes[s].remove(snakes[s].size() - 1); // removing the last segment to make sure that snake length is
                                                    // remain same when moving foward which add one segment to the snake
                                                    // head to follow the snake head direction
        }
    }

    // Main game loop
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !paused) {
            if (alive[0])
                move(0);
            if (alive[1])
                move(1);
            if (!alive[0] || !alive[1]) {
                running = false;
                timer.stop();
            }
        }
        repaint();
    }

    // Handle key presses
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (!started) {
            if (key == KeyEvent.VK_ENTER) {
                if (startButton != null) {
                    remove(startButton);
                    startButton = null;
                }
                startGame();
            }
            return;
        }
        if (!running && key == KeyEvent.VK_ENTER) {
            if (startButton != null) {
                remove(startButton);
                startButton = null;
            }
            startGame();
            return;
        }
        if (key == KeyEvent.VK_P && running) {
            paused = !paused;
            if (paused) {
                removePauseButton();
                showResumeButton();
            } else {
                removeResumeButton();
                showPauseButton();
            }
            repaint();
            return;
        }
        if (paused)
            return;
        // Player 1: WASD
        if (key == KeyEvent.VK_W && directions[0] != 2)
            directions[0] = 0;
        if (key == KeyEvent.VK_D && directions[0] != 3)
            directions[0] = 1;
        if (key == KeyEvent.VK_S && directions[0] != 0)
            directions[0] = 2;
        if (key == KeyEvent.VK_A && directions[0] != 1)
            directions[0] = 3;
        // Player 2: Arrow keys
        if (key == KeyEvent.VK_UP && directions[1] != 2)
            directions[1] = 0;
        if (key == KeyEvent.VK_RIGHT && directions[1] != 3)
            directions[1] = 1;
        if (key == KeyEvent.VK_DOWN && directions[1] != 0)
            directions[1] = 2;
        if (key == KeyEvent.VK_LEFT && directions[1] != 1)
            directions[1] = 3;
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("2-Player Snake Game");
        TwoPlayerSnakeGame gamePanel = new TwoPlayerSnakeGame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Add this method to show history dialog
    private void showHistoryDialog() {
        List<String[]> history = DatabaseUtil.fetchGameHistory();
        String[] columns = { "Player 1", "Player 2", "P1 Score", "P2 Score", "Winner", "Played At" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (String[] row : history) {
            model.addRow(row);
        }
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "Game History", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showRestartExitButtons() {
        if (restartButton == null) {
            restartButton = new JButton("Restart");
            restartButton.setFont(new Font("Arial", Font.BOLD, 20));
            restartButton.setFocusPainted(false);
            restartButton.setBackground(new Color(60, 120, 200));
            restartButton.setForeground(Color.WHITE);
            restartButton.setBounds(WIDTH / 2 - 170, HEIGHT / 2 + 80, 140, 50);
            restartButton.addActionListener(e -> {
                removeRestartExitButtons();
                startGameWithSameNames();
            });
            setLayout(null);
            add(restartButton);
        }
        if (exitButton == null) {
            exitButton = new JButton("Exit");
            exitButton.setFont(new Font("Arial", Font.BOLD, 20));
            exitButton.setFocusPainted(false);
            exitButton.setBackground(new Color(200, 60, 60));
            exitButton.setForeground(Color.WHITE);
            exitButton.setBounds(WIDTH / 2 + 30, HEIGHT / 2 + 80, 140, 50);
            exitButton.addActionListener(e -> {
                removeRestartExitButtons();
                showStartScreen();
            });
            setLayout(null);
            add(exitButton);
        }
    }

    private void removeRestartExitButtons() {
        if (restartButton != null) {
            remove(restartButton);
            restartButton = null;
        }
        if (exitButton != null) {
            remove(exitButton);
            exitButton = null;
        }
    }

    private void startGameWithSameNames() {
        snakes[0] = new ArrayList<>();
        snakes[1] = new ArrayList<>();
        snakes[0].add(new Point(WIDTH / (4 * UNIT_SIZE) * UNIT_SIZE, HEIGHT / 2));
        snakes[1].add(new Point(3 * WIDTH / (4 * UNIT_SIZE) * UNIT_SIZE, HEIGHT / 2));
        directions[0] = 1;
        directions[1] = 3;
        alive[0] = alive[1] = true;
        scores[0] = scores[1] = 0;
        food.clear();
        foodCount = 5 + random.nextInt(6);
        for (int i = 0; i < foodCount; i++) {
            spawnFood();
        }
        running = true;
        started = true;
        paused = false;
        resultSaved = false;
        timer = new javax.swing.Timer(DELAY, this);
        timer.start();
        showPauseButton();
        removeResumeButton();
        repaint();
        requestFocusInWindow();
    }
}