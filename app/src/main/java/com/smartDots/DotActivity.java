package com.smartDots;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

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
//        registerForContextMenu(dotEngine);

    }

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, "Settings");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Settings") {

        } else {
            return false;
        }
        return true;
    }*/


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
