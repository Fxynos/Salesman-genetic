package com.vl.genmodel.salesman;

import com.vl.genmodel.Breeder;
import com.vl.genmodel.util.Pair;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class BreederImpl implements Breeder<VerbosePath> {
    private final Double[][] distances;
    private final Random rand = new Random(System.currentTimeMillis());

    public BreederImpl(Double[][] distances) {
        this.distances = distances;
    }

    @Override
    public VerbosePath apply(VerbosePath p1, VerbosePath p2) {
        int[] commonPoints = Arrays.stream(p1.getPoints()).distinct().filter(
                point -> Arrays.stream(p2.getPoints()).anyMatch(i -> i == point)
        ).toArray();
        if (commonPoints.length == 0)
            throw new IllegalArgumentException("Paths must have at least 1 common point (usually start is common)");
        int chosenPoint = commonPoints[rand.nextInt(commonPoints.length)];
        int[]
                p1Indexes = indexesOf(p1, chosenPoint),
                p2Indexes = indexesOf(p2, chosenPoint);
        Pair<int[], int[]> parts = (rand.nextBoolean()) ? Pair.of(
                Arrays.copyOfRange(p1.getPoints(), 0, p1Indexes[rand.nextInt(p1Indexes.length)]),
                Arrays.copyOfRange(p2.getPoints(), p2Indexes[rand.nextInt(p2Indexes.length)], p2.getPoints().length)
        ) : Pair.of(
                Arrays.copyOfRange(p2.getPoints(), 0, p2Indexes[rand.nextInt(p2Indexes.length)]),
                Arrays.copyOfRange(p1.getPoints(), p1Indexes[rand.nextInt(p1Indexes.length)], p1.getPoints().length)
        );
        return new VerbosePath(
                IntStream.concat(Arrays.stream(parts.getFirst()), Arrays.stream(parts.getSecond())).toArray(),
                distances
        );
    }

    private static int[] indexesOf(Path path, int point) {
        return IntStream.range(0, path.getPoints().length)
                .filter(i -> path.getPoints()[i] == point).toArray();
    }
}
