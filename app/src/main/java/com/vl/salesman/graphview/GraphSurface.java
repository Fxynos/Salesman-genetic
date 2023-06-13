package com.vl.salesman.graphview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.vl.salesman.R;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class GraphSurface extends SurfaceView implements SurfaceHolder.Callback {
    private final static int MAX_FRAMES_PER_SECOND = 60;
    private final static int MIN_DELAY_BETWEEN_FRAMES = 1000 / MAX_FRAMES_PER_SECOND;
    private final static float STROKE_WIDTH = 6f;
    private final static float
            RADIUS = 30f,
            POINTER_RADIUS = 35f;
    private final static float OFFSET_BORDER = 100f;

    private final Set<Point> points = new CopyOnWriteArraySet<>();
    private final Set<Pair<Point[], Boolean>> contacts = new CopyOnWriteArraySet<>();
    private final Point pointer = new Point(0, 0) {{ setChecked(false); }};

    @ColorInt
    private final int regularColor, activeColor;
    private Drawer drawer = null;
    private final PointF offsetVector = new PointF(0, 0);

    public GraphSurface(Context context, AttributeSet set) {
        super(context, set);
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setZOrderOnTop(true);
        TypedArray typed = context.getTheme().obtainStyledAttributes(set, R.styleable.GraphSurface, 0, 0);
        try {
            regularColor = typed.getColor(R.styleable.GraphSurface_regularColor, context.getColor(android.R.color.black));
            activeColor = typed.getColor(R.styleable.GraphSurface_activeColor, context.getColor(android.R.color.holo_green_dark));
        } finally {
            typed.recycle();
        }
    }

    @NonNull
    public Point getPointer() { // modifiable pointer; setChecked() to draw
        return pointer;
    }

    public PointF getOffsetVector() {
        return offsetVector;
    }

    public void applyOffset(float dx, float dy) {
        offsetVector.x = Math.min(getMaxXOffset(), Math.max(getMinXOffset(), offsetVector.x + dx));
        offsetVector.y = Math.min(getMaxYOffset(), Math.max(getMinYOffset(), offsetVector.y + dy));
    }

    private float getMaxYOffset() {
        return getHeight() - (float) points.stream().mapToDouble(p -> p.y)
                .min().orElse(0) - OFFSET_BORDER;
    }

    private float getMinYOffset() {
        return OFFSET_BORDER - (float) points.stream().mapToDouble(p -> p.y)
                .max().orElse(0);
    }

    private float getMaxXOffset() {
        return getWidth() - (float) points.stream().mapToDouble(p -> p.x)
                .min().orElse(0) - OFFSET_BORDER;
    }

    private float getMinXOffset() {
        return OFFSET_BORDER - (float) points.stream().mapToDouble(p -> p.x)
                .max().orElse(0);
    }

    public Set<Point> getPoints() {
        return points;
    }

    public Set<Pair<Point[], Boolean>> getContacts() {
        return contacts;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        if (drawer == null) {
            drawer = new Drawer();
            drawer.start();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {}

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {}

    private class Drawer extends Thread {
        final private Paint
                regularFill = new Paint(),
                activeFill = new Paint(),
                regularStroke = new Paint(),
                activeStroke = new Paint();

        private Drawer() {
            activeFill.setStyle(Paint.Style.FILL);
            regularFill.setStyle(Paint.Style.FILL);

            activeStroke.setStyle(Paint.Style.STROKE);
            regularStroke.setStyle(Paint.Style.STROKE);

            activeStroke.setStrokeWidth(STROKE_WIDTH);
            regularStroke.setStrokeWidth(STROKE_WIDTH);

            activeFill.setColor(activeColor);
            activeStroke.setColor(activeColor);
            regularFill.setColor(regularColor);
            regularStroke.setColor(regularColor);
        }

        @Override
        public void run() {
            Canvas canvas;
            long ms;

            while (!interrupted()) {
                ms = System.currentTimeMillis();
                canvas = getHolder().lockCanvas();
                if (canvas != null) {
                   draw(canvas);
                   getHolder().unlockCanvasAndPost(canvas);
                }
                ms = System.currentTimeMillis() - ms;
                if (ms < MIN_DELAY_BETWEEN_FRAMES) try {
                    Thread.sleep(MIN_DELAY_BETWEEN_FRAMES - ms); // this is not busy waiting
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void draw(Canvas canvas) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            for (Pair<Point[], Boolean> pair : contacts)
                canvas.drawLine(
                        pair.first[0].x + offsetVector.x,
                        pair.first[0].y + offsetVector.y,
                        pair.first[1].x + offsetVector.x,
                        pair.first[1].y + offsetVector.y,
                        pair.second ? activeStroke : regularStroke
                );
            for (Point point : points)
                canvas.drawCircle(
                        point.x + offsetVector.x,
                        point.y + offsetVector.y,
                        RADIUS,
                        point.isChecked() ? activeFill : regularFill
                );
            if (pointer.isChecked())
                canvas.drawCircle(
                        pointer.x + offsetVector.x,
                        pointer.y + offsetVector.y,
                        POINTER_RADIUS,
                        activeFill
                );
        }
    }
}
