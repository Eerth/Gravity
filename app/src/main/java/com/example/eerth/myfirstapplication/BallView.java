package com.example.eerth.myfirstapplication;


import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class BallView extends SurfaceView implements SurfaceHolder.Callback{

    public static double yPosValue;

    private double yPos, yPosOld;
    private double yPosReal, yPosOldReal;
    private double xPos, xPosOld;
    private double xPosReal, xPosOldReal;

    private double Avector, Bvector;   // x" + Ax' + B = 0

    private long timeOld;
    private long dtLong;

    private int width, height;
    private double realWidth, realHeight;
    private double MeterPerPixel;

    private int circleRadius;
    private double circleRadiusMeter;

    private double ballMass;     // kg
    private double rhoAir;       // kg/m^3
    private double Cd;
    private double Surface;

    private double dt;

    private double vy, vyOld, vInf, ay;
    private double vx, vxOld, ax;

    private Paint circlePaint;
    private int CanvasColor = Color.LTGRAY;

    private Rect FloatRect;
    private int FloatRectx, FloatRectxOld, FloatRecty, FloatRectyOld;
    private double FloatVxReal, FloatVyReal;
    private int FloatLeft, FloatTop, FloatRight, FloatBottom;
    private int FloatVx, FloatVy;
    private int FloatSize;

    public static int nPoints;

    UpdateThread updateThread;

    public BallView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        getHolder().addCallback(this);

        // Ball constants
        realHeight = 3; //      [m]

        circleRadius = 30;  //  [p]
        circlePaint = new Paint();
        circlePaint.setColor(Color.BLACK);

        FloatRectx = 50;
        FloatRecty = 100;
        FloatVx = 10; //[pixels/n]
        FloatVy = 0;

        nPoints = 10;
    }

    //@Override
    protected void doDraw(Canvas canvas) {

        // you can always experiment by removing this line if you wish!
        canvas.drawColor(CanvasColor);
        //canvas.drawColor(CanvasColor, PorterDuff.Mode.CLEAR );
        canvas.drawCircle((float) xPos, (float) yPos, circleRadius, circlePaint);

        Rect FloatRect = new Rect();
        FloatRect.set(FloatLeft, FloatTop, FloatRight, FloatBottom);

        canvas.drawRect(FloatRect, circlePaint);
    }

    public void updateObject(){
        FloatRectx = FloatRectx - FloatVx;
        FloatVxReal = MeterPerPixel*(FloatRectx - FloatRectxOld)/dt;
        FloatVyReal = MeterPerPixel*(FloatRecty - FloatRectyOld)/dt;
        FloatRectxOld = FloatRectx;
        FloatRectyOld = FloatRecty;

        if (FloatRectx < 0){
            FloatRectx = width;
            FloatSize = (int)(200+Math.random()*200);
            FloatRecty = (int) (Math.random()*(height-FloatSize));
        }

        FloatLeft = FloatRectx;
        FloatTop = FloatRecty;
        FloatRight = FloatRectx + 20;
        FloatBottom = FloatRecty + FloatSize;
    }

    public void updatePhysics() {

        if (yPos - circleRadius <= 0 || yPos + circleRadius >= height) {
            // the ball has hit the top or the bottom of the canvas
            vyOld = -0.8*vy;
            //CanvasColor = Color.DKGRAY;

            if (yPos - circleRadius <= 0)      yPos = circleRadius;
            if (yPos + circleRadius >= height) yPos = height - circleRadius;

        }else{
            vyOld = vy;
            //CanvasColor = Color.LTGRAY;
        }
        if (xPos - circleRadius <= 0 || xPos + circleRadius >= width) {
            // the ball has hit the sides of the canvas
            vxOld = -0.8*vx;

            if (xPos - circleRadius <= 0){
                xPos = circleRadius;
                CanvasColor = Color.RED;
                if(nPoints>0){
                    nPoints = nPoints - 1;
                }

            }
            if (xPos + circleRadius >= width){
                xPos = width - circleRadius;
            }

        }else{
            vxOld = vx;
            CanvasColor = Color.LTGRAY;
        }
        // position is inside object
        if ( yPos - circleRadius <= FloatBottom && yPos + circleRadius >= FloatTop && xPos - circleRadius <= FloatRight && xPos + circleRadius >= FloatLeft){

            if ( yPosOld >= FloatBottom){
                yPos = FloatBottom + circleRadius;
                vyOld = -0.8*vy + FloatVyReal;
            }
            if(yPosOld <= FloatTop){
                yPos = FloatTop - circleRadius;
                vyOld = -0.8*vy + FloatVyReal;
            }
            if (xPosOld >= FloatRight){
                xPos = FloatRight + circleRadius;
                vxOld = -0.8*vx + FloatVxReal;
            }
            if (xPosOld <= FloatLeft){
                xPos = FloatLeft - circleRadius;
                vxOld = -0.8*vx + FloatVxReal;
            }
        }

        yPosOld = yPos;
        xPosOld = xPos;

        // Time
        dtLong = (System.nanoTime() - timeOld);     // [ns]
        dt = (double) dtLong/1000000000;            // [s]

        // Real values (pixel to meter)
        yPosOldReal = yPosOld * MeterPerPixel;      // [m]
        yPosReal = yPos * MeterPerPixel;
        xPosOldReal = xPosOld * MeterPerPixel;      // [m]
        xPosReal = xPos * MeterPerPixel;

        // Acceleration
        ay =  MainActivity.gravity[1] - 0.5*rhoAir*Surface*Cd*vyOld*vyOld/ballMass;//*0.2;  // [m/s^2]
        ax =  -MainActivity.gravity[0] - 0.5*rhoAir*Surface*Cd*vxOld*vxOld/ballMass;

        // Velocity
        vy = vyOld + ay*dt;                         //[m/s]
        vx = vxOld + ax*dt;

        // Position
        yPosReal = yPosOldReal + vyOld*dt + 0.5*ay*dt*dt;   // [m]
        xPosReal = xPosOldReal + vxOld*dt + 0.5*ax*dt*dt;

        // pixel values (meter to pixel)
        yPos = yPosReal / MeterPerPixel;
        xPos = xPosReal / MeterPerPixel;

        // View yPos
        yPosValue = yPos;
        //Log.i("MyActivity", Boolean.toString(HW_acc));

        timeOld = System.nanoTime();                   // [ns]

    }

    public void surfaceCreated(SurfaceHolder holder) {

        Rect surfaceFrame = holder.getSurfaceFrame();
        width = surfaceFrame.width();
        height = surfaceFrame.height();     // [pixels]

        MeterPerPixel = realHeight/height;  // [m/pixel]
        realWidth = width * MeterPerPixel;  // [m]

        circleRadiusMeter = MeterPerPixel*circleRadius; // [m]

        rhoAir =    1.293;                  // [kg/m^3]
        Cd =        0.5;
        Surface =   4*3.1415*circleRadiusMeter; // [m]
        ballMass =  1;    // [kg]

        //Avector = ( rhoAir*Cd*Surface/(2*ballMass) )/MeterPerPixel;
        //vInf = Math.sqrt(2*ballMass*9.81/(rhoAir*Cd*Surface));

        yPos =      90;     // circleRadius;
        yPosOld =   90;

        xPos = 200;
        xPosOld = 200;

        timeOld = System.nanoTime();

        updateThread = new UpdateThread(this);
        updateThread.setRunning(true);
        updateThread.start();
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

        boolean retry = true;

        updateThread.setRunning(false);
        while (retry) {
            try {
                updateThread.join();
                retry = false;
            } catch (InterruptedException e) {

            }
        }
    }

}
