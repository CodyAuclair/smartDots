package com.smartDots;

import android.graphics.Paint;
import android.graphics.RectF;

import java.lang.reflect.Array;
import java.util.Vector;
import android.graphics.Canvas;
import android.util.Log;


/***
 * Class that handles our Dot object.
 */
public class Dot implements Cloneable {
    private double[] pos;
    private double[] vel;
    private double[] acc;
    private double[] goal;
    public Brain brain;
    private boolean dead = false;
    private boolean survived = false;
    private boolean atGoal = false;

    public static int DOT_SIZE = 8;
    public static final int MAX_VELOCITY = 10;

    private int x = 0;
    private int y = 1;

    /***
     * Constructor for the Dot.
     */
    Dot() {
        brain = new Brain(1000);

        pos = new double[2];
        pos[x] = DotEngine.width / 2.0;
        pos[y] = DotEngine.height / 2.0;

        vel = new double[2];
        vel[x] = 0.0;
        vel[y] = 0.0;

        acc = new double[2];
        acc[x] = 0.0;
        acc[y] = 0.0;

        goal = new double[2];
    }

    Dot(int size) {
        brain = new Brain(size);

        pos = new double[2];
        pos[x] = 0.0;
        pos[y] = 0.0;

        vel = new double[2];
        vel[x] = 0.0;
        vel[y] = 0.0;

        acc = new double[2];
        acc[x] = 0.0;
        acc[y] = 0.0;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /***
     * Method to show the dot on the screen.
     */
    void show(Canvas canvas, Paint paint) {
        canvas.drawCircle((float) pos[x], (float) pos[y], DOT_SIZE, paint);
    }

    void move() {
        if(!dead && !isAtGoal(DotEngine.goal)) {
            // Here, we're setting the acceleration generated from the Brain class.
            if (brain.directionsForce.length > brain.step) {
                acc[x] = brain.directionsForce[brain.step] * Math.cos(brain.directionsAngle[brain.step]);
//              Log.i("ACC_X", "Acc_X is: " + acc[x]);
//              Log.i("FORCE_X", "Force X is: " + brain.directionsForce[brain.step]);
//              Log.i("COMPONENT_X", "Component X is: " + brain.directionsAngle[brain.step]);
//              Log.i("COS_X", "COS value is: " + Math.cos(brain.directionsAngle[brain.step]));
                acc[y] = brain.directionsForce[brain.step] * Math.sin(brain.directionsAngle[brain.step]);
//              Log.i("ACC_Y", "Acc_Y is: " + acc[y]);
//              Log.i("FORCE_Y", "Force Y is: " + brain.directionsForce[brain.step]);
//              Log.i("COMPONENT_Y", "Component Y is: " + brain.directionsAngle[brain.step]);
//              Log.i("SIN_X", "SIN value is: " + Math.sin(brain.directionsAngle[brain.step]));
                brain.step++;
            } else {
                acc[x] = 0;
                acc[y] = 0;
                vel[x] = 0;
                vel[y] = 0;
                survived = true;
            }

//        Log.i("VEL_X_BEF", "Vel x before: " + vel[x]);
            vel[x] += acc[x];
//        Log.i("VEL_X_AFT", "Vel x after: " + vel[x]);
            if(vel[x] > MAX_VELOCITY) {
                vel[x] = MAX_VELOCITY;
            }
            if(vel[x] < -MAX_VELOCITY) {
                vel[x] = -MAX_VELOCITY;
            }
//        Log.i("VEL_Y_BEF", "Vel y before: " + vel[y]);
            vel[y] += acc[y];
//        Log.i("VEL_Y_AFT", "Vel y after: " + vel[y]);
            if(vel[y] > MAX_VELOCITY) {
                vel[y] = MAX_VELOCITY;
            }
            if(vel[y] < -MAX_VELOCITY) {
                vel[y] = -MAX_VELOCITY;
            }

            pos[x] += vel[x];
            pos[y] += vel[y];
            isDead();
        }

    }

    boolean isDead() {
        if(pos[x] <= (2*DOT_SIZE) || pos[x] >= (DotEngine.width - 2*DOT_SIZE) ||
                pos[y] <= (2*DOT_SIZE) || pos[y] >= (DotEngine.height - 2*DOT_SIZE)) {
            dead = true;
        }
        return dead;
    }

    boolean isAtGoal(Goal goal) {
        double posGoalX = goal.getLocation()[0];
        double posGoalY = goal.getLocation()[1];
        double sizeOfGoal = goal.getGoalSize();
        double distanceBetweenCenters = Math.sqrt( Math.pow((posGoalX - pos[x]), 2) + Math.pow((posGoalY - pos[y]), 2));
        if(distanceBetweenCenters < (sizeOfGoal + DOT_SIZE)) {
            survived = true;
            atGoal = true;
            return true;
        }
        return false;
    }

    boolean isSurvived() {
        return survived;
    }


    /***
     * Getters and setters
     */

    public static int getDotSize() {
        return DOT_SIZE;
    }

    public static void setDotSize(int size) {
        DOT_SIZE = size;
    }

    public double[] getPos() {
        double[] getter = new double[2];
        getter[0] = pos[x];
        getter[1] = pos[y];
        return getter;
    }

    public double[] getVel() {
        double[] getter = new double[2];
        getter[0] = vel[x];
        getter[1] = vel[y];
        return getter;
    }

    public double[] getAcc() {
        double[] getter = new double[2];
        getter[0] = acc[x];
        getter[1] = acc[y];
        return getter;
    }

}
