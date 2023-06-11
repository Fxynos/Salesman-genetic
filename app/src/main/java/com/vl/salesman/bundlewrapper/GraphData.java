package com.vl.salesman.bundlewrapper;

import android.graphics.PointF;
import android.os.Bundle;

import androidx.core.util.Pair;

import com.google.gson.reflect.TypeToken;

import java.util.LinkedList;
import java.util.Set;

public class GraphData extends BaseBundleWrapper {
    private final static String
            EXTRA_POINTS = "points",
            EXTRA_START_POINT = "startPoint",
            EXTRA_CONNECTS = "connects";

    public GraphData(Bundle bundle) {
        super(bundle);
    }

    public GraphData() {
        this(new Bundle());
    }

    public void setPoints(Set<PointF> points) {
        put(EXTRA_POINTS, points);
    }

    public Set<PointF> getPoints() {
        return get(EXTRA_POINTS, new TypeToken<Set<PointF>>(){});
    }

    public void setConnects(Set<Pair<PointF, PointF>> connects) {
        put(EXTRA_CONNECTS, connects);
    }

    public Set<Pair<PointF, PointF>> getConnects() {
        return get(EXTRA_CONNECTS, new TypeToken<Set<Pair<PointF, PointF>>>(){});
    }

    public void setStartPoint(PointF startPoint) {
        put(EXTRA_START_POINT, startPoint);
    }

    public PointF getStartPoint() {
        return get(EXTRA_START_POINT, PointF.class);
    }

    public Double[][] getDistances() {
        LinkedList<PointF> points = new LinkedList<>(getPoints());
        Double[][] distances = new Double[points.size()][points.size()];
        getConnects().forEach(pair -> {
            double distance = Math.sqrt(Math.pow(pair.first.x - pair.second.x, 2) + Math.pow(pair.first.y - pair.second.y, 2));
            int
                    i = points.indexOf(pair.first),
                    j = points.indexOf(pair.second);
            distances[i][j] = distance;
            distances[j][i] = distance;
        });
        return distances;
    }
}
