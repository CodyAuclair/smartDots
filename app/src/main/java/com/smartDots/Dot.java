package com.smartDots;

import android.graphics.Canvas;
import android.graphics.Paint;

import static com.smartDots.DotEngine.height;
import static com.smartDots.DotEngine.width;


/***
 * Class that handles our Dot object.
 */
public class Dot {
    final private double[] pos;
    final private double[] vel;
    final private double[] acc;
    public Brain brain;
    private boolean dead = false;
    private boolean livedButDidNotMakeItToTheGoal = false;
    private boolean atGoal = false;

    public static int DOT_SIZE = 8;
    public static final int MAX_VELOCITY = 10;
    public static final int DEFAULT_MAX_STEP_COUNT = 200;
    // Pythagorean Theorem so our max velocity is actually the max. Otherwise we could get
    // MAX_VELOCITY in both the X and Y directions, which would add to be more than MAX_VELOCITY.
    private static final double MAX_VELOCITY_SCALING = Math.sqrt(2*MAX_VELOCITY*MAX_VELOCITY);

    /**
     * Set the indices in the pos, vel, and acc arrays that will be our X and Y values.
     * I'm just kinda trying to figure out the syntax behind Enums a bit here.
     */
    // TODO Make a Utility package or something to put things like this in.
    enum Coordinates {
        X(0), Y(1);

        final int index;
        Coordinates(int i) {
            index = i;
        }
        int getIndex() {
            return index;
        }
    }
    private final int x = Coordinates.X.getIndex();
    private final int y = Coordinates.Y.getIndex();

    /***
     * Constructor for the Dot.
     */
    Dot() {
        pos = new double[2];
        vel = new double[2];
        acc = new double[2];

        // Set up our Dot's new brain. Also handles creation of first brain.
        resetDot(DEFAULT_MAX_STEP_COUNT);
    }

    /**
     * Constructor that allows for a custom step count.
     * @param maxStepCount The maximum number of steps this dot can take before timing out and dying.
     */
    Dot(int maxStepCount) {
        pos = new double[2];
        vel = new double[2];
        acc = new double[2];

        resetDot(maxStepCount);
    }

    /**
     * Method to show the dot on the screen.
     * @param canvas The canvas to draw the dot on.
     * @param paint The paint object to determine the look of the dot.
     */
    void show(Canvas canvas, Paint paint) {
        canvas.drawCircle((float) pos[x], (float) pos[y], DOT_SIZE, paint);
    }

    /**
     * Handles the movement of the dot.
     * Uses the brain's generated Force and Angle to apply a vector to the dot that changes its
     * acceleration. This acts as the "legs" of the dot, deciding which way to move.
     * Should only be called by the DotEngine.
     */
    void move() {
        // We're still alive and haven't reached our goal yet, so let's keep moving.
        if((brain.directionsForce.length > brain.step)) {
            // Take our force vector and break it down into X and Y components to accelerate.
            acc[x] = brain.directionsForce[brain.step] * Math.cos(brain.directionsAngle[brain.step]);
            acc[y] = brain.directionsForce[brain.step] * Math.sin(brain.directionsAngle[brain.step]);
            // Track our moves.
            brain.step++;

            // Velocity is just current velocity plus the acceleration. v = v. + at
            vel[x] += acc[x];
            vel[y] += acc[y];
            // Check that our actual velocity vector is less than our defined limit.
            double magnitude = Math.hypot(vel[x],vel[y]);
            if (magnitude > MAX_VELOCITY){
                // Scale our velocities down a bit.
                vel[x] = vel[x] * MAX_VELOCITY_SCALING / magnitude;
                vel[y] = vel[y] * MAX_VELOCITY_SCALING / magnitude;
            }

            // Position is just current position plus velocity. x = x. + vt
            pos[x] += vel[x];
            pos[y] += vel[y];
            // Check if we're out of bounds.
            isOutOfBounds();
        } else {
            // We're still alive, but we're out of moves, so we've failed.
            acc[x] = 0;
            acc[y] = 0;
            vel[x] = 0;
            vel[y] = 0;
            livedButDidNotMakeItToTheGoal = true;
        }

    }

    /**
     * Check if any part of our dot is outside of the screen's defined borders.
     * @return True iff we're at least partially outside of the screen.
     */
    boolean isOutOfBounds() {
        if(pos[x] <= (2*DOT_SIZE) || pos[x] >= (width - 2*DOT_SIZE) ||
                pos[y] <= (2*DOT_SIZE) || pos[y] >= (DotEngine.height - 2*DOT_SIZE)) {
            dead = true;
        }
        return dead;
    }

    /**
     * Check if our dot has made it to the goal. Done by checking if our goal and dot are touching
     * at all.
     * @param goal The goal object to check if we've reached.
     * @return True iff our dot is at the goal.
     */
    boolean isAtGoal(Goal goal) {
        // Store the goal info in slightly-more-readable variables.
        double posGoalX = goal.getLocation()[Coordinates.X.getIndex()];
        double posGoalY = goal.getLocation()[Coordinates.Y.getIndex()];
        double sizeOfGoal = goal.getGoalSize();
        // Find out how far the centers of our dot and circle are from each other. Simple geometry.
        double distanceBetweenCenters = Math.sqrt( Math.pow((posGoalX - pos[x]), 2) + Math.pow((posGoalY - pos[y]), 2));
        // Add the size of our goal and the dot together. Since they're circles, the two radii will
        // touch if they're ever within distance of each other.
        if(distanceBetweenCenters < (sizeOfGoal + DOT_SIZE)) {
            atGoal = true;
        }
        return atGoal;
    }

    boolean isLivedButDidNotMakeItToTheGoal() {
        return livedButDidNotMakeItToTheGoal;
    }

    /**
     * Resets our dot to starting position, with a given brain size for the new generation.
     * @param newBrainSize The maximum number of moves a dot is allowed to make.
     */
    void resetDot(int newBrainSize) {
        // Initial position. Currently the middle of the screen.
        pos[x] = (1.0 * width) / 2;
        pos[y] = (1.0 * height) / 2;

        // Start at a stand-still.
        vel[x] = 0.0;
        vel[y] = 0.0;

        // Start at a stand-still/
        acc[x] = 0.0;
        acc[y] = 0.0;

        // When we ready a dot, we want it to have a new brain with a potentially shorter time to
        // reach, based on previous generations. Otherwise all our survivors will always survive and
        // always get their full survival move count, and never try anything new.
        brain = new Brain(newBrainSize);

        // Flip all our flags back to false.
        dead = false;
        livedButDidNotMakeItToTheGoal = false;
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
