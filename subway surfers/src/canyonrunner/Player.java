package canyonrunner;

import java.awt.Color;
import java.awt.Graphics;

/**
 * OOP CONCEPT: ENCAPSULATION
 * - The field 'lane' is private so outside classes cannot set it to an invalid number.
 * - 'moveLeft()' and 'moveRight()' guard the boundaries (only lanes 0, 1, and 2 are allowed).
 */
public class Player {
    private int lane = 1;          // 0 = Left, 1 = Center, 2 = Right
    private int y = 480;           // Base vertical position
    private int jumpOffset = 0;    // Upward offset during jump
    private int jumpSpeed = 0;
    private boolean jumping = false;
    private boolean sliding = false;
    private int slideTimer = 0;

    public void moveLeft() {
        if (lane > 0) lane--;
    }

    public void moveRight() {
        if (lane < 2) lane++;
    }

    public void jump() {
        if (!jumping) {
            jumping = true;
            jumpSpeed = 15;
            sliding = false;
        }
    }

    public void slide() {
        if (!jumping && !sliding) {
            sliding = true;
            slideTimer = 25; // Slide lasts 25 frames
        }
    }

    public void update() {
        // Jump physics
        if (jumping) {
            jumpOffset += jumpSpeed;
            jumpSpeed -= 1; // Gravity
            if (jumpOffset <= 0) {
                jumpOffset = 0;
                jumping = false;
            }
        }
        // Slide duration
        if (sliding) {
            slideTimer--;
            if (slideTimer <= 0) sliding = false;
        }
    }

    public void draw(Graphics g, int[] laneX) {
        int px = laneX[lane];
        int py = y - jumpOffset;

        // Shadow on ground
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(px - 18, y + 40, 36, 10);

        if (sliding) {
            // Sliding pose (low to ground)
            g.setColor(new Color(90, 50, 20)); // Brown duster coat
            g.fillRect(px - 18, py + 20, 36, 20);
            g.setColor(Color.RED);              // Bandana
            g.fillRect(px - 14, py + 16, 28, 6);
            g.setColor(new Color(60, 35, 15));  // Hat
            g.fillRect(px - 22, py + 10, 44, 7);
        } else {
            // Standing/Jumping Cowboy
            g.setColor(new Color(50, 40, 35));  // Boots & Pants
            g.fillRect(px - 12, py + 24, 10, 20);
            g.fillRect(px + 2, py + 24, 10, 20);

            g.setColor(new Color(110, 60, 25)); // Leather Coat / Vest
            g.fillRect(px - 14, py - 4, 28, 28);

            g.setColor(Color.RED);              // Red Bandana
            g.fillRect(px - 10, py - 10, 20, 6);

            g.setColor(new Color(220, 175, 140)); // Face
            g.fillRect(px - 8, py - 20, 16, 10);

            // Cowboy Hat (Crown + Brim)
            g.setColor(new Color(70, 40, 15));
            g.fillRect(px - 10, py - 32, 20, 12); // Crown
            g.fillRect(px - 20, py - 22, 40, 6);  // Brim
        }
    }

    public int getLane() { return lane; }
    public boolean isJumping() { return jumping; }
    public boolean isSliding() { return sliding; }
    public int getJumpOffset() { return jumpOffset; }
    public void reset() {
        lane = 1;
        jumping = false;
        sliding = false;
        jumpOffset = 0;
    }
}
