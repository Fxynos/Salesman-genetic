package com.vl.salesman.graphview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;

import com.vl.salesman.item.Point;

@SuppressLint("ClickableViewAccessibility")
public class GraphSurfaceVisualisationController {

    private final GraphSurface graphSurface;
    private final PointF startPoint;
    private Visualizer visualizer;
    private Runnable onVisualizationEnd;

    public GraphSurfaceVisualisationController(GraphSurface graphSurface, PointF startPoint) {
        this.graphSurface = graphSurface;
        this.startPoint = startPoint;
        graphSurface.movePointerTo(startPoint);
        graphSurface.setOnTouchListener(new MovementHandler());
    }

    public void setOnVisualizationEnd(Runnable onVisualizationEnd) {
        this.onVisualizationEnd = onVisualizationEnd;
    }

    public void startVisualization(Point[] path) {
        visualizer = new Visualizer(path);
    }

    public void stopVisualization() {
        if (visualizer == null)
            return;
        visualizer.animator.pause();
        visualizer = null;
        graphSurface.movePointerTo(startPoint);
    }

    private void onVisualizationEnd() {
        visualizer = null;
        graphSurface.movePointerTo(startPoint);
        onVisualizationEnd.run();
    }

    private class MovementHandler implements View.OnTouchListener {
        private final PointF previousPosition = new PointF();

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    previousPosition.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    graphSurface.applyOffset(
                            event.getX() - previousPosition.x,
                            event.getY() - previousPosition.y
                    );
                    previousPosition.set(event.getX(), event.getY());
                    break;
            }
            return true;
        }
    }

    private class Visualizer extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {

        private final static int DURATION = 1000;

        private final Point[] path;
        private int pos = 0;
        private final ValueAnimator animator = new ValueAnimator();

        private Visualizer(Point[] path) {
            this.path = path;
            animator.setDuration(DURATION);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setFloatValues(0, 1);
            animator.addListener(this);
            animator.addUpdateListener(this);
            startAnimation();
        }

        private void startAnimation() {
            animator.start();
        }

        @Override
        public void onAnimationUpdate(@NonNull ValueAnimator animator) {
            Point
                    from = path[pos],
                    to = path[pos + 1];
            float percent = (float) animator.getAnimatedValue();
            graphSurface.movePointerTo(new PointF(
                    percent * (to.x - from.x) + from.x,
                    percent * (to.y - from.y) + from.y
            ));
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (++pos < path.length - 1)
                startAnimation();
            else
                onVisualizationEnd();
        }
    }
}
