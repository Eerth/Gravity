package com.example.eerth.myfirstapplication;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class UpdateThread extends Thread {
    private long time;
    private final int fps = 60;
    private boolean toRun = false;
    private BallView ballView;
    private SurfaceHolder surfaceHolder;


    public UpdateThread(BallView rBallView) {
        ballView = rBallView;
        surfaceHolder = ballView.getHolder();
    }

    public void setRunning(boolean run) {
        toRun = run;
    }

    @Override
    public void run() {

        Canvas c;
        while (toRun && !interrupted()) {

            long cTime = System.currentTimeMillis();

            if ((cTime - time) <= (1000 / fps)) {

                c = null;
                try {
                    c = surfaceHolder.lockCanvas(null);

                    ballView.updatePhysics();
                    ballView.updateObject();
                    ballView.doDraw(c);
                } finally {
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
            time = cTime;
        }
    }

}

