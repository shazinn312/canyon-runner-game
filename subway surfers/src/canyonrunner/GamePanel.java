package canyonrunner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener {
    public static final int WIDTH = 500;
    public static final int HEIGHT = 620;

    // 3 Lanes (Left, Center, Right X-coordinates)
    private final int[] laneX = {130, 250, 370};

    // Game states
    private enum State { START, PLAYING, GAME_OVER, QUESTS }
    private State state = State.START;
    private State prevState = State.START;

    // Core Game Objects
    private final Player player = new Player();
    private final Random rand = new Random();

    /**
     * OOP CONCEPT: POLYMORPHISM
     * A single List<Obstacle> stores different subclasses (Cactus, Tumbleweed, OverheadBeam).
     * The loop calls obstacle.update(), obstacle.draw(), and obstacle.checkCollision()
     * polymorphically without needing if-else type checks!
     */
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<Coin> coins = new ArrayList<>();

    // Gameplay numbers
    private int distance = 0;
    private int highScore = 0;
    private int coinCount = 0;
    private int gameSpeed = 7;
    private int spawnTimer = 0;
    private int roadDashOffset = 0;
    private int laneChanges = 0;

    // Quests completion flags
    private boolean[] questCompleted = new boolean[6];

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(225, 155, 95)); // Desert sand
        setFocusable(true);
        addKeyListener(new KeyHandler());

        // 60 FPS Game Loop (~16ms per frame)
        Timer timer = new Timer(16, this);
        timer.start();
    }

    private void resetGame() {
        player.reset();
        obstacles.clear();
        coins.clear();
        distance = 0;
        coinCount = 0;
        gameSpeed = 7;
        spawnTimer = 0;
        laneChanges = 0;
        state = State.PLAYING;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == State.PLAYING) {
            updateGame();
        }
        repaint();
    }

    private void updateGame() {
        distance++;
        if (distance > highScore) highScore = distance;

        // Speed increases gradually
        if (distance % 300 == 0 && gameSpeed < 14) {
            gameSpeed++;
        }

        // Road scroll effect
        roadDashOffset = (roadDashOffset + gameSpeed) % 40;

        player.update();

        // 1. Spawning Hazards & Coins
        spawnTimer++;
        if (spawnTimer > Math.max(35, 75 - gameSpeed * 2)) {
            spawnTimer = 0;
            int lane = rand.nextInt(3);
            int type = rand.nextInt(3);

            if (type == 0) obstacles.add(new Cactus(lane, -30));
            else if (type == 1) obstacles.add(new Tumbleweed(lane, -30));
            else obstacles.add(new OverheadBeam(lane, -30));

            // Occasionally spawn a coin in another lane
            if (rand.nextBoolean()) {
                int coinLane = (lane + 1 + rand.nextInt(2)) % 3;
                coins.add(new Coin(coinLane, -30));
            }
        }

        // 2. POLYMORPHISM IN ACTION: Update and test obstacles
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obs = obstacles.get(i);
            obs.setSpeed(gameSpeed);
            obs.update();

            // Check collision with player
            if (obs.checkCollision(player)) {
                state = State.GAME_OVER;
                return;
            }

            if (!obs.isActive()) obstacles.remove(i);
        }

        // 3. Update Coins
        for (int i = coins.size() - 1; i >= 0; i--) {
            Coin c = coins.get(i);
            c.setSpeed(gameSpeed);
            c.update();

            if (c.checkPickup(player)) {
                coinCount += c.getValue();
            }

            if (!c.isActive()) coins.remove(i);
        }

        // 4. Track Quests
        if (distance > 20) questCompleted[0] = true;   // Q1: Classes & Objects
        if (laneChanges >= 3) questCompleted[1] = true; // Q2: Encapsulation
        if (obstacles.size() > 0) questCompleted[2] = true; // Q3: Inheritance
        if (distance > 150) questCompleted[3] = true;  // Q4: Abstraction
        if (distance > 250) questCompleted[4] = true;  // Q5: Polymorphism
        if (coinCount >= 5) questCompleted[5] = true;  // Q6: Interfaces
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Sky & Sun
        g.setColor(new Color(195, 80, 50));
        g.fillRect(0, 0, WIDTH, 120);
        g.setColor(new Color(255, 220, 120));
        g.fillOval(WIDTH / 2 - 30, 40, 60, 60);

        // 2. Canyon Track (3 Lanes)
        g.setColor(new Color(210, 140, 80));
        g.fillRect(70, 120, 360, HEIGHT - 120);

        // Road Borders & Lane Dividers
        g.setColor(new Color(130, 75, 35));
        g.fillRect(66, 120, 6, HEIGHT - 120);
        g.fillRect(428, 120, 6, HEIGHT - 120);

        // Dashed lines between lanes
        g.setColor(new Color(175, 110, 60));
        for (int y = 120 + roadDashOffset; y < HEIGHT; y += 40) {
            g.fillRect(190, y, 4, 20); // Divider 1
            g.fillRect(310, y, 4, 20); // Divider 2
        }

        // 3. Side Scenery (Cacti outside road)
        g.setColor(new Color(40, 110, 50));
        g.fillRect(25, 260, 10, 35);
        g.fillRect(18, 270, 7, 5);
        g.fillRect(460, 380, 10, 35);
        g.fillRect(470, 390, 7, 5);

        // 4. Draw Coins & Obstacles
        for (Coin c : coins) c.draw(g, laneX);
        for (Obstacle obs : obstacles) obs.draw(g, laneX);

        // 5. Draw Player
        player.draw(g, laneX);

        // 6. Draw HUD
        drawHUD(g);

        // 7. State Overlays
        if (state == State.START) drawStartScreen(g);
        else if (state == State.GAME_OVER) drawGameOverScreen(g);
        else if (state == State.QUESTS) drawQuestScreen(g);
    }

    private void drawHUD(Graphics g) {
        g.setColor(new Color(30, 20, 15, 200));
        g.fillRoundRect(10, 10, WIDTH - 20, 40, 10, 10);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("DISTANCE: " + distance + "m", 25, 35);
        g.drawString("HIGH: " + highScore + "m", 160, 35);

        g.setColor(new Color(255, 215, 0));
        g.drawString("COINS: " + coinCount, 270, 35);

        g.setColor(new Color(100, 220, 120));
        g.drawString("[Q] Quests", 390, 35);
    }

    private void drawStartScreen(Graphics g) {
        drawBox(g, 50, 180, 400, 260);
        g.setColor(new Color(255, 220, 130));
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("WILD WEST RUNNER", 125, 225);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("Left / Right (A / D)  : Switch Lanes", 130, 265);
        g.drawString("Up Arrow (W / Space) : Jump Tumbleweeds", 130, 290);
        g.drawString("Down Arrow (S)       : Slide Under Beams", 130, 315);
        g.drawString("Avoid tall Cacti by switching lanes!", 130, 340);

        g.setColor(new Color(255, 100, 80));
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("Press SPACE to Start Riding!", 140, 395);
    }

    private void drawGameOverScreen(Graphics g) {
        drawBox(g, 60, 200, 380, 220);
        g.setColor(new Color(255, 80, 60));
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.drawString("GAME OVER!", 170, 245);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("Distance Survived : " + distance + " m", 150, 285);
        g.drawString("Coins Collected   : " + coinCount, 150, 310);
        g.drawString("Best Record       : " + highScore + " m", 150, 335);

        g.setColor(new Color(255, 215, 100));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("Press SPACE or [R] to Play Again", 130, 380);
    }

    private void drawQuestScreen(Graphics g) {
        drawBox(g, 35, 80, 430, 480);
        g.setColor(new Color(255, 215, 100));
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("OOP LEARNING QUESTS", 145, 115);

        String[] titles = {
            "1. Classes & Objects",
            "2. Encapsulation",
            "3. Inheritance",
            "4. Abstraction",
            "5. Polymorphism",
            "6. Interfaces"
        };
        String[] lessons = {
            "Player, Cactus, Coin created as objects from classes.",
            "private 'lane' safely modified by moveLeft/Right().",
            "Cactus and Tumbleweed both extend base Obstacle.",
            "Entity is an abstract class defining update() & draw().",
            "List<Obstacle> updates all hazard subclasses uniformly.",
            "Collectible interface implemented by Coin class."
        };

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        int y = 145;
        for (int i = 0; i < 6; i++) {
            g.setColor(questCompleted[i] ? new Color(100, 230, 120) : Color.LIGHT_GRAY);
            String mark = questCompleted[i] ? "[COMPLETED] " : "[IN PROGRESS] ";
            g.drawString(mark + titles[i], 55, y);

            g.setColor(Color.WHITE);
            g.drawString("   " + lessons[i], 55, y + 17);
            y += 48;
        }

        g.setColor(new Color(255, 200, 100));
        g.drawString("Press [Q] or ESC to return to game", 145, 535);
    }

    private void drawBox(Graphics g, int x, int y, int w, int h) {
        g.setColor(new Color(30, 15, 10, 230));
        g.fillRoundRect(x, y, w, h, 14, 14);
        g.setColor(new Color(180, 110, 50));
        g.drawRoundRect(x, y, w, h, 14, 14);
    }

    private class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int k = e.getKeyCode();

            if (k == KeyEvent.VK_Q) {
                if (state == State.QUESTS) state = prevState;
                else { prevState = state; state = State.QUESTS; }
                repaint();
                return;
            }

            if (state == State.QUESTS && k == KeyEvent.VK_ESCAPE) {
                state = prevState;
                repaint();
                return;
            }

            if (state == State.START || state == State.GAME_OVER) {
                if (k == KeyEvent.VK_SPACE || k == KeyEvent.VK_ENTER || k == KeyEvent.VK_R) {
                    resetGame();
                }
                return;
            }

            if (state == State.PLAYING) {
                if (k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A) {
                    player.moveLeft();
                    laneChanges++;
                } else if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) {
                    player.moveRight();
                    laneChanges++;
                } else if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W || k == KeyEvent.VK_SPACE) {
                    player.jump();
                } else if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) {
                    player.slide();
                }
            }
        }
    }
}
