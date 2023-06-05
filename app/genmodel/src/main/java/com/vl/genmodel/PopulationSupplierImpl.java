package com.vl.genmodel;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

public class PopulationSupplierImpl implements PopulationSupplier<VerbosePath> {
    private final int minLength, maxLength, startPoint;
    private final Double[][] distances;
    private final Random rand = new Random(System.currentTimeMillis());

    public PopulationSupplierImpl(int minLength, int maxLength, Double[][] distances, int startPoint) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.startPoint = startPoint;
        this.distances = Arrays.copyOf(distances, distances.length);
        if (distances.length == 0 || distances.length != distances[0].length)
            throw new IllegalArgumentException("malformed distances matrix");
        for (int i = 0; i < distances.length; i++)
            this.distances[i][i] = null;
        IntStream.range(0, distances.length).filter(i ->
                Arrays.stream(distances[i], 0, distances.length).allMatch(Objects::isNull)
        ).findAny().ifPresent(i -> {
            throw new IllegalArgumentException(String.format("deadlock in point at index %d", i));
        });
    }

    public PopulationSupplierImpl(int minLength, int maxLength, Double[][] distances) {
        this(minLength, maxLength, distances, 0);
    }

    @Override
    @NotNull
    public VerbosePath get() {
        int[] points = new int[rand.nextInt(maxLength - minLength + 1) + minLength];
        points[0] = startPoint;
        for (int i = 1; i < points.length; i++) {
            int finalIterator = i;
            int[] variants = IntStream.range(0, distances.length)
                    .filter(j -> distances[points[finalIterator - 1]][j] != null).toArray();
            points[i] = variants[rand.nextInt(variants.length)];
        }
        return new VerbosePath(points, distances);
    }
}
