package com.smartDots;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

@SuppressWarnings("ALL")
public class DotEngine extends SurfaceView implements Runnable {

    private Thread thread = null;

    private Context context;

    public static Goal goal;

    public static int numDots = 200;
    private Dot[] dotArray;
    public Dot[] newDots = new Dot[numDots];
    int succeederCount = 0;
    int generationCount = -1;
    int smallestNumSteps;

    public static int width;
    public static int height;

    private long nextFrameTime;
    private int stepCount = 0;
    private final long FPS = 30;
    private final long MILLIS_PER_SECOND = 1000;

    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Paint paint;

    private volatile boolean isRunning = true;

    /**
     * Constructor for the DotEngine. This acts as the "game" engine, measuring out frames for
     * actions to take place on.
     * @param context The application's context.
     * @param size The size of the screen.
     */
    public DotEngine(Context context, Point size) {
        super(context);

        // Easy context access if needed.
        context = context;

        // Set the size of the playing field.
        width = size.x;
        height = size.y - (int) (size.y * 0.04);

        // Set up our surfaces.
        surfaceHolder = getHolder();
        paint = new Paint();

        // The current "high score" held by a dot.
        // TODO Maybe display this on screen somewhere?
        smallestNumSteps = Dot.DEFAULT_MAX_STEP_COUNT;

        // Clicking anywhere on the screen immediately kills the current generation and starts the
        // next.
        View view = getRootView();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEngine();
            }
        });
        // TODO Maybe toast the score or something?
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        // Let's get it started.
        startEngine();
    }

    /**
     * This thread will handle updating our game and telling time to move forward.
     */
    @Override
    @SuppressWarnings("InfiniteLoopStatement") // False positive. Infinite Loop is exactly what we want here.
    // TODO Figure out why this... works. The thread is never initialized (it's just null) but I can still run()? Must be understanding this wrong.
    public void run() {
        Log.d("RUN", "Inside run");
        while (true) {
            while (isRunning) {
                if(updateRequired()) {
                    for(int i = 0 ; i < numDots ; i++) {
                        // Only operate on dots that are still in play
                        if(!dotArray[i].isOutOfBounds()
                                && !dotArray[i].isAtGoal(DotEngine.goal)) {
                            dotArray[i].move();
                        }
                    }
                    draw();
                }
            }
        }
    }

    /**
     * Pause the app. Normally called when the app is sent to the background.
     */
    public void pause() {
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    /**
     * Resume the app. Normally called when the app is re-opened from the background.
     */
    // TODO Figure out why the app can't resume. Presumably the thread is killed and the run() can't be restarted on a new thread.
    public void resume() {
        thread = new Thread(this);
        isRunning = true;
        thread.start();
    }

    /**
     * Get our engine running so the "game" can start moving.
     */
    public void startEngine() {
        isRunning = false;
        dotArray = readyNextGen(dotArray);
        Log.i("DOT ENGINE", "Current gen: " + generationCount);

        // Create our goal if we haven't yet.
        if(goal == null) {
            goal = new Goal();
        }

        isRunning = true;
        nextFrameTime = System.currentTimeMillis();
    }

    /**
     * Handles all of the actual drawing. Everything that shows up on the canvas is drawn here.
     */
    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            // Color of the background
            paint.setStyle(Paint.Style.FILL);
            canvas.drawColor(Color.argb(255, 255, 255, 255));

            // Color of the border
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.argb(255, 220, 220, 220));
            paint.setStrokeWidth(20);
            canvas.drawRect(0, 0, width, height, paint);

            // color of dead/out of bounds dots
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(255, 220, 220, 220));
            for(int i = 0; i < numDots ; i++) {
                if(dotArray[i].isOutOfBounds()) {
                    dotArray[i].show(canvas, paint);
                }
            }

            // Color of living dots
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(255, 0, 0, 0));
            for(int i = 0 ; i < numDots ; i++) {
                if(!dotArray[i].isOutOfBounds() && !dotArray[i].isLivedButDidNotMakeItToTheGoal()
                        && !dotArray[i].isAtGoal(goal) && dotArray[i].brain.step <= Dot.DEFAULT_MAX_STEP_COUNT) {
                    dotArray[i].show(canvas, paint);
                }
            }

            // Color of dots that survived but didn't make it to the goal
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(255, 0, 0, 255));
            for(int i = 0; i < numDots ; i++) {
                if(dotArray[i].isLivedButDidNotMakeItToTheGoal()) {
                    dotArray[i].show(canvas, paint);
                }
            }

            // Color of the dots that made it to the goal.
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(255, 0, 255, 0));
            for(int i = 0 ; i < numDots ; i++) {
                if(dotArray[i].isAtGoal(goal)) {
                    dotArray[i].show(canvas, paint);
                }
            }

            // Color of the goal
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(255, 255, 0, 0));
            goal.show(canvas, paint);

            // Unlock the canvas and reveal the graphics for this frame
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * Lets the engine know when an update is required.
     * @return True iff we've exceeded the time since our last frame was executed.
     */
    public boolean updateRequired() {
        // Are we due to update the frame?
        if (nextFrameTime <= System.currentTimeMillis()) { // nextFrameTime has passed
            // Setup when the next update will be triggered
            nextFrameTime = System.currentTimeMillis() + (MILLIS_PER_SECOND/FPS);
            return true;
        }
        return false;
    }

    /**
     * Gets the number of dots that succeeded in reaching the goal in the last generation.
     * @param lastGeneration The generation to check.
     * @return A Dot Array containing all the Dot objects from the last generation that reached the
     *          goal.
     */
    public Dot[] getSucceeders(Dot[] lastGeneration) {
        succeederCount = 0;
        for(int i = 0; i < numDots; i++) {
            if(lastGeneration[i].isAtGoal(goal)) {
                succeederCount++;
            }
        }

        Dot[] succeeders = new Dot[succeederCount];

        int a = 0;
        for(int i = 0; i < numDots; i++) {
            if(lastGeneration[i].isAtGoal(goal)) {
                succeeders[a] = lastGeneration[i];
                a++;
            }
        }

        return succeeders;
    }

    /**
     * Prepares the next generation of Dots. This will eventually include minor mutations to the
     * succeeders, instead of just a complete over-write of their Brain.
     * @param lastGeneration The previous generation of Dots, to evolve from.
     * @return A new Dot Array that will be the next generation to try and survive.
     */
    public Dot[] readyNextGen(@Nullable Dot[] lastGeneration) {
        generationCount++;
        Dot[] nextGen;
        if (lastGeneration == null) {
            nextGen = new Dot[numDots];
            for(int i = 0; i < numDots; i++) {
                nextGen[i] = new Dot();
            }
        } else {
            Dot[] lastGenSucceeders = getSucceeders(lastGeneration);
            nextGen = new Dot[numDots];
            Log.d("Succeeders", "Last generation had " + succeederCount + " dots reach goal");
            // TODO Mutation should occur here. As it is, these dots will all lose their Brains.
            System.arraycopy(lastGenSucceeders, 0, nextGen, 0, succeederCount);
            int fewestSteps = getFewestNumberOfStepsForSuccessFromGeneration(lastGenSucceeders);
            for(int i = 0; i < succeederCount; i++) {
                nextGen[i].resetDot(fewestSteps);
            }
            for(int i = succeederCount; i < numDots; i++) {
                nextGen[i] = new Dot(fewestSteps);
            }
        }

        return nextGen;
    }

    /**
     * Gets the fewest number of steps that the last generation took to reach the goal. Eventually
     * will be used to help with the scoring system of how well a dot did, in order to favor
     * better performing dots' brains for future generations.
     * @param generation The generation to operate on.
     * @return The minimum number of turns it took a dot to reach the goal.
     */
    private int getFewestNumberOfStepsForSuccessFromGeneration(Dot[] generation) {
        int lastGenFewestStepsToFinish = smallestNumSteps;
        for(int i = 0; i < succeederCount; i++) {
            if(generation[i].isAtGoal(goal) &&
                    generation[i].brain.step < lastGenFewestStepsToFinish) {
                lastGenFewestStepsToFinish = generation[i].brain.step;
            }
        }
        // TODO Currently if no dots reach the end, we hit the "tied".
        if(lastGenFewestStepsToFinish < smallestNumSteps) {
            smallestNumSteps = lastGenFewestStepsToFinish;
            Log.d("getFewest", "This generation beat the best score and only took " +
                    smallestNumSteps + " to reach the goal.");
        } else if(lastGenFewestStepsToFinish > smallestNumSteps) {
            Log.d("getFewest", "This generation did not out-perform the best score of " +
                    smallestNumSteps);
        } else {
            Log.d("getFewest", "The best performer of this generation tied with the " +
                    "best score of " + smallestNumSteps + " to reach the goal.");
        }

        return smallestNumSteps;
    }

    /**
     * Gets the most number of steps that the last generation took to reach the goal. Eventually
     * will be used to help with the scoring system of how well a dot did, in order to disfavor
     * worse performing dots' brains for future generations.
     * @param generation The generation to operate on.
     * @return The minimum number of turns it took a dot to reach the goal.
     */
    private int getMostNumberOfStepsForSuccessFromGeneration(Dot[] generation) {
        int lastGenMostStepsToFinish = -1;
        for(int i = 0; i < succeederCount; i++) {
            if(generation[i].isAtGoal(goal) &&
                    generation[i].brain.step > lastGenMostStepsToFinish) {
                lastGenMostStepsToFinish = generation[i].brain.step;
            }
        }
        if(lastGenMostStepsToFinish == -1) {
            lastGenMostStepsToFinish = Dot.DEFAULT_MAX_STEP_COUNT;
        }
        Log.d("LastGenMostSteps", "Last generation's weakest link took " + lastGenMostStepsToFinish
         + " to reach the goal.");
        return lastGenMostStepsToFinish;
    }

}
