package com.smartDots;

import java.util.Random;

import static java.lang.Math.PI;

/**
 * Every Dot will have a single Brain, and the Brain is the set of instructions the dot follows
 * throughout its life.
 */
public class Brain {

    final double[] directionsForce;
    final double[] directionsAngle;
    public int step;

    final static int MAX_FORCE_PER_STEP = 10;

    /**
     * Default constructor. Sets up our arrays to hold the moves that a dot will make.
     * @param maxStepCount The maximum number of moves a dot can make.
     */
    Brain(int maxStepCount) {
        directionsForce = new double[maxStepCount];
        directionsAngle = new double[maxStepCount];
        step = 0;
        randomize();
    }

    /**
     * Randomizes the force to apply and the angle to apply that force at for every step of journey.
     */
    void randomize() {
        for(int i = 0 ; i < directionsForce.length ; i++) {
            Random rF = new Random();
            double randomForce = MAX_FORCE_PER_STEP * rF.nextDouble();
            directionsForce[i] = randomForce;

            Random rA = new Random();
            double randomAngle = 2 * PI * rA.nextDouble();
            directionsAngle[i] = randomAngle;
        }
    }

}
