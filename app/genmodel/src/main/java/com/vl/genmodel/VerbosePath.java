package com.vl.genmodel;

import java.util.stream.IntStream;

public class VerbosePath extends Path {
    private final double length;

    private static double calculateLength(int[] points, Double[][] distances) {
        return IntStream.range(0, points.length - 1)
                .mapToDouble(i -> distances[points[i]][points[i + 1]]).sum();
    }

    public VerbosePath(int[] points, Double[][] distances) {
        super(points);
        this.length = calculateLength(points, distances);
    }

    public double getLength() {
        return length;
    }
}
