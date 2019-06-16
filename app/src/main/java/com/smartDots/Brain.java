package com.smartDots;

import java.util.Random;

import static java.lang.Math.PI;

/***
 * Creates an array of random angles from 0 to 2*PI
 * Each element in the array is a single angle.
 * Each angle will be applied to a single dot's velocity and acceleration vectors.
 * This means that every Dot will have a single Brain, and the Brain is how many instructions the
 * dot follows.
 */
public class Brain {

    double[] directionsForce;
    double[] directionsAngle;
    public int step = 0;

    int MAX_FORCE_PER_STEP = 10;

    Brain(int size) {
        directionsForce = new double[size];
        directionsAngle = new double[size];
        randomize();
    }

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
