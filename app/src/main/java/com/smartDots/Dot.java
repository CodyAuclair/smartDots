package com.smartDots;

import android.graphics.Paint;

import android.graphics.Canvas;

import static com.smartDots.DotEngine.height;
import static com.smartDots.DotEngine.width;


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
    private boolean livedButDidntMakeItToTheGoal = false;
    private boolean atGoal = false;

    public static int DOT_SIZE = 8;
    public static final int MAX_VELOCITY = 10;
    public static final int DEFAULT_STEP_COUNT = 200;
    private static final double MAX_VELOCITY_SCALING = Math.sqrt(2*MAX_VELOCITY*MAX_VELOCITY);
    private int x = 0;
    private int y = 1;

    /***
     * Constructor for the Dot.
     */
    Dot() {
        brain = new Brain(DEFAULT_STEP_COUNT);
        pos = new double[2];
        vel = new double[2];
        acc = new double[2];

        resetDot(DEFAULT_STEP_COUNT);
    }

    Dot(int size) {
        brain = new Brain(size);
        pos = new double[2];
        vel = new double[2];
        acc = new double[2];

        resetDot(size);
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
                acc[y] = brain.directionsForce[brain.step] * Math.sin(brain.directionsAngle[brain.step]);
                brain.step++;
            } else {
                acc[x] = 0;
                acc[y] = 0;
                vel[x] = 0;
                vel[y] = 0;
                livedButDidntMakeItToTheGoal = true;
            }

//        Log.i("VEL_X_BEF", "Vel x before: " + vel[x]);
            vel[x] += acc[x];
//        Log.i("VEL_X_AFT", "Vel x after: " + vel[x]);
            vel[y] += acc[y];
            double magnitude = Math.hypot(vel[x],vel[y]);
            if (magnitude > MAX_VELOCITY){
//                Log.i("VEL_OVER_BEFORE", "Vel x: " + vel[x] + "\t Vel y: " + vel[y]);
                vel[x] = vel[x] * MAX_VELOCITY_SCALING / magnitude;
                vel[y] = vel[y] * MAX_VELOCITY_SCALING / magnitude;
//                Log.i("VEL_OVER_AFTER", "Vel x: " + vel[x] + "\t Vel y: " + vel[y]);
            }

            pos[x] += vel[x];
            pos[y] += vel[y];
            isDead();
        } else {
            acc[x] = 0;
            acc[y] = 0;
            vel[x] = 0;
            vel[y] = 0;
            livedButDidntMakeItToTheGoal = true;
        }

    }

    boolean isDead() {
        if(pos[x] <= (2*DOT_SIZE) || pos[x] >= (width - 2*DOT_SIZE) ||
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
//            livedButDidntMakeItToTheGoal = true;
            atGoal = true;
            return true;
        }
        return false;
    }

    boolean isLivedButDidntMakeItToTheGoal() {
        return livedButDidntMakeItToTheGoal;
    }

    void resetDot(int newBrainSize) {
        pos[x] = (1.0 * width) / 2;
        pos[y] = (1.0 * height) / 2;

        vel[x] = 0.0;
        vel[y] = 0.0;

        acc[x] = 0.0;
        acc[y] = 0.0;

        brain.step = 0;
        // When we reset a dot, we want it to have a new brain with a shorter time to reach.
        // Otherwise our survivors will always survive and always get their full survival time.
        Brain newBrain = new Brain(newBrainSize);
//        if (newBrainSize >= 0) {
//            System.arraycopy(brain.directionsForce, 0, newBrain.directionsForce, 0, newBrainSize);
//            System.arraycopy(brain.directionsAngle, 0, newBrain.directionsAngle, 0, newBrainSize);
//        }
        brain = newBrain;

        dead = false;
        livedButDidntMakeItToTheGoal = false;
        atGoal = false;
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
