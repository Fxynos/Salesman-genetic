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
import androidx.core.util.Pair;

import com.vl.salesman.item.Point;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressLint("ClickableViewAccessibility")
public class GraphSurfaceVisualisationController {

    private final GraphSurface graphSurface;
    private final PointF startPoint;
    private Visualizer visualizer;
    private Runnable onVisualizationEnd;

    public GraphSurfaceVisualisationController(GraphSurface graphSurface, PointF startPoint) {
        this.graphSurface = graphSurface;
        this.startPoint = startPoint;
        graphSurface.getPointer().set(startPoint);
        graphSurface.getPointer().setChecked(true);
        graphSurface.setOnTouchListener(new MovementHandler());
        graphSurface.getPoints().stream()
                .filter(p -> p.x == startPoint.x && p.y == startPoint.y).findAny()
                .orElseThrow(() -> new RuntimeException("Graph must contain startPoint")).setChecked(true);
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
        visualizer.interruptAnimation();
        reset();
    }

    private void onVisualizationEnd() {
        reset();
        onVisualizationEnd.run();
    }

    private void reset() {
        visualizer = null;
        graphSurface.getPointer().set(startPoint);
        graphSurface.getPoints().stream().filter(p -> p.x != startPoint.x || p.y != startPoint.y)
                .forEach(p -> p.setChecked(false));
        Set<Pair<Point[], Boolean>> connections = graphSurface.getContacts().stream().map(
                pair -> new Pair<>(pair.first, false)
        ).collect(Collectors.toSet());
        graphSurface.getContacts().clear();
        graphSurface.getContacts().addAll(connections);
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

        private final static int DURATION = 2000;

        private final Point[] path;
        private int pos = 0;
        private final ValueAnimator animator = new ValueAnimator();
        private Pair<Point[], Boolean> currentConnection;

        private Visualizer(Point[] path) {
            this.path = path;
            animator.setDuration(DURATION);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setFloatValues(0, 1);
            animator.addListener(this);
            animator.addUpdateListener(this);
            startAnimation();
        }

        private void interruptAnimation() {
            animator.pause();
            graphSurface.getContacts().remove(currentConnection);
            currentConnection = null;
        }

        private void startAnimation() {
            currentConnection = new Pair<>(new Point[]{path[pos], graphSurface.getPointer()}, true);
            graphSurface.getContacts().add(currentConnection);
            animator.start();
        }

        private void setCurrentConnectionChecked() {
            Pair<Point[], Boolean> checkedConnection = graphSurface.getContacts().stream().filter(
                    pair -> Stream.of(pair.first).filter(p -> p.equals(path[pos]) || p.equals(path[pos + 1])).count() == 2
            ).findAny().orElseThrow(NoSuchElementException::new);
            graphSurface.getContacts().remove(checkedConnection);
            graphSurface.getContacts().add(new Pair<>(checkedConnection.first, true));
        }

        @Override
        public void onAnimationUpdate(@NonNull ValueAnimator animator) {
            Point
                    from = path[pos],
                    to = path[pos + 1];
            float percent = (float) animator.getAnimatedValue();
            graphSurface.getPointer().set(
                    percent * (to.x - from.x) + from.x,
                    percent * (to.y - from.y) + from.y
            );
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            graphSurface.getContacts().remove(currentConnection);
            setCurrentConnectionChecked();
            path[++pos].setChecked(true);
            if (pos < path.length - 1)
                startAnimation();
            else {
                currentConnection = null;
                onVisualizationEnd();
            }
        }
    }
}
