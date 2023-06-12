package com.vl.genmodel.salesman;

import com.vl.genmodel.Mutator;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class MutatorImpl implements Mutator<VerbosePath> {
    private final Double[][] distances;
    private final int minMutations, maxMutations;
    private final Random rand = new Random(System.currentTimeMillis());

    public MutatorImpl(Double[][] distances, int minMutations, int maxMutations) {
        this.distances = distances;
        this.minMutations = minMutations;
        this.maxMutations = maxMutations;
    }

    @Override
    public VerbosePath apply(VerbosePath verbosePath) {
        int pointsToChange = rand.nextInt(maxMutations - minMutations + 1) + minMutations;
        switch (rand.nextInt(3)) {
            case 0: return removeLast(verbosePath, pointsToChange);
            case 1: return addLast(verbosePath, pointsToChange);
            case 2: return changeLast(verbosePath, pointsToChange);
            default: throw new RuntimeException(); // unreachable
        }
    }

    private VerbosePath removeLast(VerbosePath path, int count) {
        return new VerbosePath(Arrays.copyOf(
                path.getPoints(),
                Math.max(path.getPoints().length - count, 1)
        ), distances);
    }

    private VerbosePath addLast(VerbosePath path, int count) {
        int[] points = Arrays.copyOf(path.getPoints(), path.getPoints().length + count);
        for (int i = path.getPoints().length; i < points.length; i++) {
            int finalIterator = i;
            int[] variants = IntStream.range(0, distances.length)
                    .filter(j -> distances[points[finalIterator - 1]][j] != null).toArray();
            points[i] = variants[rand.nextInt(variants.length)];
        }
        return new VerbosePath(points, distances);
    }

    private VerbosePath changeLast(VerbosePath path, int count) {
        return addLast(removeLast(path, count), count);
    }
}
