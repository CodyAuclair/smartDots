package com.smartDots;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class DotActivity extends Activity {
    DotEngine dotEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        dotEngine = new DotEngine(this, size);

        setContentView(dotEngine);

    }

    @Override
    protected void onResume() {
        super.onResume();
        dotEngine.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dotEngine.pause();
    }
}
