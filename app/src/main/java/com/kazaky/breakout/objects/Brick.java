package com.kazaky.breakout.objects;

/**
 * Created by Ahmed Elshahawi on 12/20/2015.
 */

import android.graphics.RectF;

public class Brick {

    private RectF rect;

    private boolean isVisible;

    public Brick(int row, int column, int width, int height) {

        isVisible = true;

        // Padding between bricks
        int padding = 0;

        rect = new RectF(column * width + padding,
                row * height + padding,
                column * width + width - padding,
                row * height + height - padding);
    }

    public RectF getRect() {
        return this.rect;
    }

    public void setInvisible() {
        isVisible = false;
    }

    public boolean getVisibility() {
        return isVisible;
    }
}