package com.vl.salesman.graphbuild;

import androidx.core.util.Pair;

import com.vl.salesman.graphview.Point;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
        this.points.clear();
        this.points.addAll(points.stream().map(p -> {
            Point point = new Point(p);
            point.setChecked(false);
            return point;
        }).collect(Collectors.toSet()));
    }

    public void updateConnects(Collection<Pair<Point[], Boolean>> connects) {
        this.connects.clear();
        this.connects.addAll(connects);
    }
}
