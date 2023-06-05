package com.vl.salesman.view;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import com.vl.salesman.item.Point;

import org.jetbrains.annotations.NotNull;

@SuppressLint("ClickableViewAccessibility")
public class GraphSurfaceController {
    private final static float POINT_CLICK_RADIUS = 30f;

    private final GraphSurface surfaceView;
    private GraphSurfaceCallback listener;
    private volatile boolean isPointPlacing = false;

    public GraphSurfaceController(GraphSurface surfaceView) {
        this.surfaceView = surfaceView;
        surfaceView.setOnTouchListener(new TouchHandler());
    }

    public void requestPointPlace() {
        isPointPlacing = true;
    }

    public void setCallback(GraphSurfaceCallback listener) {
        this.listener = listener;
    }

    public interface GraphSurfaceCallback {
        /**
         * Called after new point was added or existing has moved
         */
        void onPointPlaced(Point point);
        void onPointChecked(Point point);
    }

    private class TouchHandler implements View.OnTouchListener {
        private Point clickPoint; // current "checked" point

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isPointPlacing) {
                        if (clickPoint != null)
                            clickPoint.setChecked(false);
                        clickPoint = new Point(event.getX(), event.getY());
                        clickPoint.setChecked(true);
                        surfaceView.getPoints().add(clickPoint);
                    } else if (clickPoint != null) {
                        if (distance(event, clickPoint) > POINT_CLICK_RADIUS) {
                            clickPoint.setChecked(false);
                            clickPoint = null;
                        } else isPointPlacing = true;
                    }
                    if (clickPoint == null) {
                        clickPoint = surfaceView.getPoints().stream().filter(
                                p -> distance(event, p) <= POINT_CLICK_RADIUS
                        ).min(
                                (p1, p2) -> Float.compare(distance(event, p1), distance(event, p2))
                        ).orElse(null);
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (isPointPlacing)
                        clickPoint.set(event.getX(), event.getY());
                    else if (clickPoint != null && distance(event, clickPoint) > POINT_CLICK_RADIUS)
                        clickPoint = null;
                    break;

                case MotionEvent.ACTION_UP:
                    if (isPointPlacing) {
                        isPointPlacing = false;
                        clickPoint.setChecked(false);
                        if (listener != null)
                            listener.onPointPlaced(clickPoint);
                        clickPoint = null;
                    } else if (clickPoint != null) {
                        clickPoint.setChecked(true);
                        if (listener != null)
                            listener.onPointChecked(clickPoint);
                    }
                    break;
            }
            return true;
        }

        private float distance(float x1, float y1, float x2, float y2) {
            return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        }

        private float distance(@NotNull MotionEvent event, @NotNull Point point) {
            return distance(event.getX(), event.getY(), point.x, point.y);
        }
    }
}
