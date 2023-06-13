package com.vl.salesman.graphbuild;

import androidx.core.util.Pair;

import com.vl.salesman.graphview.Point;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BuildGraphViewModel extends androidx.lifecycle.ViewModel {
    private final Set<Point> points = new HashSet<>();
    private final Set<Pair<Point[], Boolean>> connects = new HashSet<>();

    public Set<Point> getPoints() {
        return points;
    }

    public Set<Pair<Point[], Boolean>> getConnects() {
        return connects;
    }

    public void updatePoints(Collection<Point> points) {
        this.points.addAll(points);
    }

    public void updateConnects(Collection<Pair<Point[], Boolean>> connects) {
        this.connects.addAll(connects);
    }
}
