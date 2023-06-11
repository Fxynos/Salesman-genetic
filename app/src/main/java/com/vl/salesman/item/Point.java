package com.vl.salesman.item;

import android.graphics.PointF;

public class Point extends PointF {
    private boolean isChecked = false;

    public Point(float x, float y) {
        super(x, y);
    }

    public Point(PointF point) {
        this(point.x, point.y);
    }

    public PointF getPoint() {
        PointF pointF = new PointF();
        pointF.set(this);
        return pointF;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
