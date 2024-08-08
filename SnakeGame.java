import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class SnakeGame extends JFrame {
    private static final int UNIT_SIZE = 25;
    private static final int GAME_UNITS = 1200;
    private static final int FPS = 60;
    private static final int UPDATE_RATE = 10; // Lower means faster snake
    private static final int INITIAL_DELAY = 150;

    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];
    private int bodyParts = 6;
    private int applesEaten;
    private int appleX;
    private int appleY;
    private char direction = 'R';
    private boolean running = false;
    private boolean paused = false;
    private Timer timer;
    private final Random random;
    private int updateCounter = 0;
    private JPanel gamePanel;

    public SnakeGame() {
        random = new Random();
        setupGUI();
        setFullScreen();
    }

    private void setupGUI() {
        this.setTitle("Snake Game");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        // Setup menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem startMenuItem = new JMenuItem("Start");
        JMenuItem exitMenuItem = new JMenuItem("Exit");

        startMenuItem.addActionListener(e -> startGame());
        exitMenuItem.addActionListener(e -> System.exit(0));

        gameMenu.add(startMenuItem);
        gameMenu.add(exitMenuItem);
        menuBar.add(gameMenu);
        this.setJMenuBar(menuBar);

        // Setup game panel
        gamePanel = new GamePanel();
        this.add(gamePanel);
    }

    private void setFullScreen() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        this.setUndecorated(true);
        gd.setFullScreenWindow(this);
        this.validate();
    }

    private void startGame() {
        newApple();
        running = true;
        paused = false;
        timer = new Timer(1000 / FPS, new GameCycle());
        timer.start();
    }

    private void pauseGame() {
        paused = !paused;
        if (paused) {
            timer.stop();
        } else {
            timer.start();
        }
    }

    private void newApple() {
        appleX = random.nextInt((int) (getWidth() / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (getHeight() / UNIT_SIZE)) * UNIT_SIZE;
    }

    private void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] -= UNIT_SIZE;
            case 'D' -> y[0] += UNIT_SIZE;
            case 'L' -> x[0] -= UNIT_SIZE;
            case 'R' -> x[0] += UNIT_SIZE;
        }
    }

    private void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
            if (applesEaten % 5 == 0 && updateCounter > 1) {
                updateCounter--;
            }
        }
    }

    private void checkCollisions() {
        // Check if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }

        // Check if head touches the borders
        if (x[0] < 0 || x[0] >= getWidth() || y[0] < 0 || y[0] >= getHeight()) {
            running = false;
        }

        if (!running) timer.stop();
    }

    private void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", (getWidth() - metrics.stringWidth("Game Over")) / 2, getHeight() / 2);

        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 20));
        g.drawString("Press ENTER to Restart", (getWidth() - metrics.stringWidth("Press ENTER to Restart")) / 2, getHeight() / 2 + 50);
    }

    private class GamePanel extends JPanel {
        GamePanel() {
            this.setBackground(Color.black);
            this.setFocusable(true);
            this.addKeyListener(new MyKeyAdapter());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            draw(g);
        }

        private void draw(Graphics g) {
            if (running) {
                g.setColor(Color.red);
                g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

                for (int i = 0; i < bodyParts; i++) {
                    if (i == 0) {
                        g.setColor(Color.green);
                    } else {
                        g.setColor(new Color(45, 180, 0));
                    }
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }

                g.setColor(Color.red);
                g.setFont(new Font("Ink Free", Font.BOLD, 25));
                FontMetrics metrics = getFontMetrics(g.getFont());
                g.drawString("Score: " + applesEaten, (getWidth() - metrics.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
            } else {
                gameOver(g);
            }
        }
    }

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (running) {
                if (updateCounter == 0) {
                    move();
                    checkApple();
                    checkCollisions();
                    updateCounter = UPDATE_RATE;
                } else {
                    updateCounter--;
                }
            }
            gamePanel.repaint();
        }
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') direction = 'R';
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') direction = 'U';
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') direction = 'D';
                    break;
                case KeyEvent.VK_P:
                    if (running) pauseGame();
                    break;
                case KeyEvent.VK_ENTER:
                    if (!running) {
                        resetGame();
                        startGame();
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);
                    break;
            }
        }
    }

    private void resetGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        updateCounter = UPDATE_RATE;
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 50 - i * UNIT_SIZE;
            y[i] = 50;
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(SnakeGame::new);
    }
}
