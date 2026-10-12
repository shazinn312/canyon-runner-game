package canyonrunner;

import java.awt.Color;
import java.awt.Graphics;

/**
 * ============================================================================
 * OOP CONCEPT: ABSTRACTION
 * 'Entity' is an abstract class. We cannot make a raw 'new Entity()'.
 * Instead, it defines common properties (lane, y, speed) and requires subclasses
 * to implement update() and draw().
 * ============================================================================
 */
abstract class Entity {
    protected int lane;
    protected int y;
    protected int speed = 7;
    protected boolean active = true;

    public Entity(int lane, int y) {
        this.lane = lane;
        this.y = y;
    }

    public void update() {
        y += speed;
        if (y > 650) active = false; // Off screen
    }

    public abstract void draw(Graphics g, int[] laneX);

    public boolean isActive() { return active; }
    public void setSpeed(int speed) { this.speed = speed; }
}

/**
 * ============================================================================
 * OOP CONCEPT: INTERFACES
 * 'Collectible' defines a contract. Any entity implementing it can be collected
 * for score/coins without the game needing to know its exact class.
 * ============================================================================
 */
interface Collectible {
    int getValue();
    void collect();
    boolean isCollected();
}

/**
 * ============================================================================
 * OOP CONCEPT: INHERITANCE
 * 'Obstacle' extends 'Entity', inheriting lane, y, and movement.
 * Subclasses (Cactus, Tumbleweed, OverheadBeam) inherit shared logic and add
 * their own unique collision check!
 * ============================================================================
 */
abstract class Obstacle extends Entity {
    public Obstacle(int lane, int y) {
        super(lane, y);
    }

    // Polymorphic method: each obstacle subclass decides if it hit the player!
    public abstract boolean checkCollision(Player p);
}

/**
 * Obstacle Subclass 1: Saguaro Cactus (Lane Blocker - must switch lanes!)
 */
class Cactus extends Obstacle {
    public Cactus(int lane, int y) { super(lane, y); }

    @Override
    public boolean checkCollision(Player p) {
        // Collides if player is in the same lane and within Y range (cannot jump over)
        return p.getLane() == lane && y >= 450 && y <= 510;
    }

    @Override
    public void draw(Graphics g, int[] laneX) {
        int cx = laneX[lane];
        g.setColor(new Color(35, 115, 55));
        // Main cactus trunk
        g.fillRect(cx - 7, y - 25, 14, 50);
        // Left arm
        g.fillRect(cx - 18, y - 10, 11, 6);
        g.fillRect(cx - 18, y - 20, 6, 12);
        // Right arm
        g.fillRect(cx + 7, y - 5, 11, 6);
        g.fillRect(cx + 12, y - 15, 6, 12);
    }
}

/**
 * Obstacle Subclass 2: Tumbleweed (Jumpable - jump with Up arrow to clear!)
 */
class Tumbleweed extends Obstacle {
    public Tumbleweed(int lane, int y) { super(lane, y); }

    @Override
    public boolean checkCollision(Player p) {
        // Safe if player jumped at least 20 pixels high!
        if (p.getLane() == lane && y >= 455 && y <= 505) {
            return !p.isJumping() || p.getJumpOffset() < 20;
        }
        return false;
    }

    @Override
    public void draw(Graphics g, int[] laneX) {
        int cx = laneX[lane];
        g.setColor(new Color(170, 115, 60)); // Dry desert brown
        g.fillOval(cx - 15, y - 15, 30, 30);
        g.setColor(new Color(130, 85, 40));
        g.drawOval(cx - 11, y - 11, 22, 22);
    }
}

/**
 * Obstacle Subclass 3: Overhead Timber Beam (Slidable - slide with Down arrow!)
 */
class OverheadBeam extends Obstacle {
    public OverheadBeam(int lane, int y) { super(lane, y); }

    @Override
    public boolean checkCollision(Player p) {
        // Safe if player is sliding low!
        if (p.getLane() == lane && y >= 450 && y <= 500) {
            return !p.isSliding();
        }
        return false;
    }

    @Override
    public void draw(Graphics g, int[] laneX) {
        int cx = laneX[lane];
        g.setColor(new Color(90, 55, 30)); // Wood posts & beam
        g.fillRect(cx - 24, y - 30, 48, 10); // Overhead beam
        g.fillRect(cx - 22, y - 20, 6, 30);  // Left post
        g.fillRect(cx + 16, y - 20, 6, 30);  // Right post
        // Small sign
        g.setColor(new Color(160, 110, 60));
        g.fillRect(cx - 12, y - 20, 24, 12);
    }
}

/**
 * ============================================================================
 * OOP CONCEPT: CLASSES & OBJECTS + INTERFACES
 * 'Coin' is a concrete class implementing 'Collectible' and extending 'Entity'.
 * ============================================================================
 */
class Coin extends Entity implements Collectible {
    private boolean collected = false;

    public Coin(int lane, int y) { super(lane, y); }

    public boolean checkPickup(Player p) {
        if (!collected && p.getLane() == lane && y >= 460 && y <= 510) {
            collect();
            return true;
        }
        return false;
    }

    @Override public int getValue() { return 1; }
    @Override public void collect() { collected = true; active = false; }
    @Override public boolean isCollected() { return collected; }

    @Override
    public void draw(Graphics g, int[] laneX) {
        if (collected) return;
        int cx = laneX[lane];
        g.setColor(new Color(255, 215, 0)); // Gold
        g.fillOval(cx - 10, y - 10, 20, 20);
        g.setColor(new Color(180, 130, 10));
        g.drawOval(cx - 10, y - 10, 20, 20);
        g.drawString("$", cx - 4, y + 5);
    }
}
