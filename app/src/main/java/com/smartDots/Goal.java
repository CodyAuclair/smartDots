package com.smartDots;

import android.graphics.Canvas;
import android.graphics.Paint;

import static com.smartDots.DotEngine.height;
import static com.smartDots.DotEngine.width;

/**
 * The goal that every Dot dreams of reaching one day. When a dot reaches the goal, they're
 * considered a winner. The best winners will eventually set the bar for future generations.
 */
public class Goal {
    final private double[] pos;
    final private int x = Dot.Coordinates.X.getIndex();
    final private int y = Dot.Coordinates.Y.getIndex();

    // The size of the goal, measured in pixels as a radius.
    private final int GOAL_SIZE = 100;

    /**
     * Default constructor that places the goal in the middle of the screen horizontally, and
     * 20 percent down from the top of the screen vertically.
     */
    Goal() {
        pos = new double[2];
        pos[x] = width / 2.0;
        pos[y] = height / 5.0;
    }

    /**
     * Constructor that allows a custom goal location to be set.
     * @param posx The x position of the goal.
     * @param posy The y position of the goal.
     */
    Goal(double posx, double posy) {
        pos = new double[2];
        pos[x] = posx;
        pos[y] = posy;
    }

    /**
     * Draws the goal onto the screen.
     * @param canvas The canvas to draw the goal on.
     * @param paint The Paint object that defines the styles of the goal.
     */
    void show(Canvas canvas, Paint paint) {
        canvas.drawCircle((float) pos[x], (float) pos[y], GOAL_SIZE, paint);
    }

    /**
     * Getters and setters
     */

    public double[] getLocation() {
        return pos;
    }

    public int getGoalSize () {
        return GOAL_SIZE;
    }
}