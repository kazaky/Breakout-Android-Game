package com.kazaky.breakout.objects;

/**
 * Created by Ahmed Elshahawi on 12/19/2015.
 */

import android.graphics.RectF;

public class Paddle {

    // Which ways can the paddle move
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;
    int scrX;
    // RectF is an object that holds four coordinates - just what we need
    private RectF rect;
    // How long and high our paddle will be
    private float length;
    private float height;
    // X is the far left of the rectangle which forms our paddle
    private float x;
    // Y is the top coordinate
    private float y;
    // This will hold the pixels per second speedthat the paddle will move
    private float paddleSpeed;
    // Is the paddle moving and in which direction
    private int paddleMoving = STOPPED;
    private int MYscreenDPI;

    // This the the constructor method
    // When we create an object from this class we will pass
    // in the screen width and height
    public Paddle(int screenX, int screenY, int screenDPI) {
        // Dynamic size based on each device DPI
        length = screenDPI / 2;
        height = screenDPI / 5;
        MYscreenDPI = screenDPI;
        scrX = screenX;
        // Start paddle in roughly the sceen centre
        x = screenX / 2;
        y = screenY - screenDPI / 4.50f;

        rect = new RectF(x, y, x + length, y + height);

        // How fast is the paddle in pixels per second
        paddleSpeed = 800;
    }

    // This is a getter method to make the rectangle that
    // defines our paddle available in BreakoutView class
    public RectF getRect() {
        return rect;
    }

    public int getMovementState() {
        return paddleMoving;
    }

    // This method will be used to change/set if the paddle is going left, right or nowhere
    public void setMovementState(int state) {
        paddleMoving = state;
    }

    // This update method will be called from update in BreakoutView
    // It determines if the paddle needs to move and changes the coordinates
    // contained in rect if necessary
    public void update(long fps) {
        if (paddleMoving == LEFT) {
            // to fix Paddle going off the Screen
            if (x >= -MYscreenDPI / 10)
                // Decrement position
                x = x - paddleSpeed / fps;
        }

        if (paddleMoving == RIGHT) {
            // to fix Paddle going off the Screen
            if (x <= scrX - length - MYscreenDPI / 14)
                // Increment position
                x = x + paddleSpeed / fps;
        }

        // Apply the New position
        rect.left = x;
        rect.right = x + length;
    }

}