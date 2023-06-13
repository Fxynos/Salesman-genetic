package com.vl.salesman.graphview;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

class MovementHandler implements View.OnTouchListener {
    private final GraphSurface graphSurface;
    private final PointF previousPosition = new PointF();

    public MovementHandler(GraphSurface graphSurface) {
        this.graphSurface = graphSurface;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        System.out.println(event.getAction());
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
