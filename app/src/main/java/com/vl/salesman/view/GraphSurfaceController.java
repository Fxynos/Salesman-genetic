package com.vl.salesman.view;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.util.Pair;

import com.vl.salesman.item.Point;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@SuppressLint("ClickableViewAccessibility")
public class GraphSurfaceController {
    private final static float POINT_CLICK_RADIUS = 30f;

    private final GraphSurface surfaceView;
    private final TouchHandler touchHandler;
    private GraphSurfaceCallback listener;
    private boolean isPointPlacing = false;

    public GraphSurfaceController(GraphSurface surfaceView) {
        this.surfaceView = surfaceView;
        surfaceView.setOnTouchListener(touchHandler = new TouchHandler());
    }

    public void requestPointPlace() {
        isPointPlacing = true;
    }

    public boolean requestRemovePoint() {
        if (touchHandler.isMotionHandling || touchHandler.clickPoint == null)
            return false;
        surfaceView.getPoints().remove(touchHandler.clickPoint);
        surfaceView.getContacts().stream()
                .filter(pair -> Arrays.stream(pair.first).anyMatch(p -> p == touchHandler.clickPoint))
                .forEach(surfaceView.getContacts()::remove);
        touchHandler.clickPoint = null;
        return true;
    }

    public void setCallback(GraphSurfaceCallback listener) {
        this.listener = listener;
    }

    public interface GraphSurfaceCallback {
        /**
         * Called after new point was added or existing has moved
         */
        void onPointPlaced(Point point);
        void onPointChecked(Point point, boolean checked);
    }

    private class TouchHandler implements View.OnTouchListener {
        private boolean isMotionHandling = false;
        private Point clickPoint; // current "checked" point
        private Pair<Point[], Boolean> clickConnection; // current dragging connection

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isMotionHandling = true;

                    if (clickPoint != null && listener != null)
                        listener.onPointChecked(clickPoint, false);

                    if (isPointPlacing) {
                        if (clickPoint != null)
                            clickPoint.setChecked(false);
                        clickPoint = pointOf(event, true);
                        surfaceView.getPoints().add(clickPoint);
                    } else if (clickPoint != null) {
                        if (distance(event, clickPoint) > POINT_CLICK_RADIUS) {
                            clickPoint.setChecked(false);
                            clickPoint = null;
                        } else isPointPlacing = true;
                    }
                    if (clickPoint == null)
                        clickPoint = findAt(event);
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (isPointPlacing)
                        clickPoint.set(event.getX(), event.getY());
                    else if (clickPoint != null && distance(event, clickPoint) > POINT_CLICK_RADIUS) {
                        clickPoint.setChecked(true);
                        clickConnection = new Pair<>(new Point[]{clickPoint, pointOf(event, false)}, true);
                        clickPoint = null;
                        surfaceView.getContacts().add(clickConnection);
                    } else if (clickConnection != null) {
                        Point pointTo = findAt(event);
                        clickConnection.first[1].set(pointTo == null ? pointOf(event, false) : pointTo);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    isMotionHandling = false;

                    if (isPointPlacing) {
                        isPointPlacing = false;
                        clickPoint.setChecked(false);
                        if (listener != null)
                            listener.onPointPlaced(clickPoint);
                        clickPoint = null;
                    } else if (clickPoint != null) {
                        clickPoint.setChecked(true);
                        if (listener != null)
                            listener.onPointChecked(clickPoint, true);
                    } else if (clickConnection != null) {
                        Point
                                pointFrom = clickConnection.first[0],
                                pointTo = findAt(event);
                        pointFrom.setChecked(false);
                        surfaceView.getContacts().remove(clickConnection);
                        clickConnection = new Pair<>(new Point[]{pointFrom, pointTo}, false);
                        if (pointTo != null && pointFrom != pointTo && !isThereConnection(clickConnection))
                            surfaceView.getContacts().add(clickConnection);
                        clickConnection = null;
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

        @Nullable
        private Point findAt(MotionEvent event) {
            return surfaceView.getPoints().stream().filter(
                    p -> distance(event, p) <= POINT_CLICK_RADIUS
            ).min(
                    (p1, p2) -> Float.compare(distance(event, p1), distance(event, p2))
            ).orElse(null);
        }

        private Point pointOf(MotionEvent event, boolean checked) {
            Point point = new Point(event.getX(), event.getY());
            point.setChecked(checked);
            return point;
        }

        private boolean isThereConnection(Pair<Point[], Boolean> connection) {
            return surfaceView.getContacts().stream().map(pair -> pair.first).anyMatch(
                    points -> Arrays.stream(points)
                            .filter(p -> p == connection.first[0] || p == connection.first[1])
                            .count() == 2
            );
        }
    }
}
