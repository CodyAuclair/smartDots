package com.smartDots;

import android.graphics.Canvas;
import android.graphics.Paint;

import static com.smartDots.DotEngine.width;
import static com.smartDots.DotEngine.height;

public class Goal {
    private double[] pos;
    private int x = 0;
    private int y = 1;

    private final int GOAL_SIZE = 10;

    Goal() {
        pos = new double[2];
        pos[x] = width / 2.0;
        pos[y] = height / 5.0;
    }

    Goal(double posx, double posy) {
        pos = new double[2];
        pos[x] = posx;
        pos[y] = posy;
    }

    void show(Canvas canvas, Paint paint) {
        canvas.drawCircle((float) pos[x], (float) pos[y], GOAL_SIZE, paint);
    }

    public double[] getLocation() {
        return pos;
    }

    public int getGoalSize () {
        return GOAL_SIZE;
    }
}