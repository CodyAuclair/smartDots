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

    public DotEngine(Context context, Point size) {
        super(context);

        context = context;

        width = size.x;
        height = size.y - (int) (size.y * 0.04);

        surfaceHolder = getHolder();
        paint = new Paint();

        smallestNumSteps = Dot.DEFAULT_STEP_COUNT;

        View view = getRootView();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSimulation();
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        startSimulation();
    }

    @Override
    public void run() {
        Log.d("RUN", "Inside run");
        while (true) {
            while (isRunning) {
                if(updateRequired()) {
                    for(int i = 0 ; i < numDots ; i++) {
                        if(!dotArray[i].isDead()) {
                            dotArray[i].move();
                        }
                    }
//                test.move();
                    draw();
                }
            }
        }
    }

    public void pause() {
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    public void resume() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    public void startSimulation() {
        isRunning = false;
        generationCount++;
        dotArray = readyNextGen(dotArray);
        Log.i("DOT ENGINE", "Current gen: " + generationCount);

        if(goal == null) {
            goal = new Goal();
        }

//        test = new Dot();
        isRunning = true;
        nextFrameTime = System.currentTimeMillis();
    }

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

            // color of the dead dot
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(255, 220, 220, 220));
            for(int i = 0; i < numDots ; i++) {
                if(dotArray[i].isDead()) {
                    dotArray[i].show(canvas, paint);
                }
            }

            // Color of the living dot
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(255, 0, 0, 0));
            for(int i = 0 ; i < numDots ; i++) {
                if(!dotArray[i].isDead() && !dotArray[i].isLivedButDidntMakeItToTheGoal()
                        && !dotArray[i].isAtGoal(goal) && dotArray[i].brain.step <= Dot.DEFAULT_STEP_COUNT) {
                    dotArray[i].show(canvas, paint);
//                    Log.i("Draw", "Drawing dot " + i + " for time # " + dotArray[i].brain.step);
                }
            }

            // Color of dots that survived but didn't make it to the goal
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(255, 0, 0, 255));
            for(int i = 0; i < numDots ; i++) {
                if(dotArray[i].isLivedButDidntMakeItToTheGoal()) {
                    dotArray[i].show(canvas, paint);
                }
            }

            // Color of the dots at goal
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(255, 0, 255, 0));
            for(int i = 0 ; i < numDots ; i++) {
                if(isGoal(goal, i)) {
                    dotArray[i].show(canvas, paint);
                }
            }

            // Color of the goal
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(255, 255, 0, 0));
            goal.show(canvas, paint);

//            test.show(canvas, paint);

            // Unlock the canvas and reveal the graphics for this frame
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired() {
        // Are we due to update the frame?
        if (nextFrameTime <= System.currentTimeMillis()) {
            // nextFrameTime has passed
            stepCount++;
            // Setup when the next update will be triggered
            nextFrameTime = System.currentTimeMillis() + (1000/FPS);
            return true;
        }
        return false;
    }

    public boolean isGoal(Goal goal, int i) {
        double posx = goal.getLocation()[0];
        double posy = goal.getLocation()[1];
        int size = goal.getGoalSize();
        return dotArray[i].isAtGoal(goal);
    }

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

    public Dot[] readyNextGen(@Nullable Dot[] lastGeneration) {
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
//            int shortestStep = Dot.DEFAULT_STEP_COUNT;
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

    private int getFewestNumberOfStepsForSuccessFromGeneration(Dot[] generation) {
        int lastGenFewestStepsToFinish = smallestNumSteps;
        for(int i = 0; i < succeederCount; i++) {
            if(generation[i].isAtGoal(goal) &&
                    generation[i].brain.step < lastGenFewestStepsToFinish) {
                lastGenFewestStepsToFinish = generation[i].brain.step;
            }
        }
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

    private int getMostNumberOfStepsForSuccessFromGeneration(Dot[] generation) {
        int lastGenMostStepsToFinish = -1;
        for(int i = 0; i < succeederCount; i++) {
            if(generation[i].isAtGoal(goal) &&
                    generation[i].brain.step > lastGenMostStepsToFinish) {
                lastGenMostStepsToFinish = generation[i].brain.step;
            }
        }
        if(lastGenMostStepsToFinish == -1) {
            lastGenMostStepsToFinish = Dot.DEFAULT_STEP_COUNT;
        }
        Log.d("LastGenMostSteps", "Last generation's weakest link took " + lastGenMostStepsToFinish
         + " to reach the goal.");
        return lastGenMostStepsToFinish;
    }

}
