package com.vl.genmodel;

import com.vl.genmodel.util.Pair;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class GeneticModel<T extends Path> implements Cloneable {
    private PopulationSupplier<T> populationSupplier;
    private Selector<T> selector;
    private Breeder<T> breeder;
    private Mutator<T> mutator;
    private int populationSize = 1000;
    private double mutationRate = 1.0;
    private BreedStrategy breedStrategy = BreedStrategy.BEST;
    private volatile Trainer trainer;
    private volatile Result<T> intermediateResult;

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public GeneticModel<T> clone() {
        try {
            return (GeneticModel<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // unreachable
        }
    }

    @Nullable
    public Result<T> getIntermediateResult() {
        if (!isStarted())
            throw new IllegalStateException("Learning process is not started");
        return intermediateResult;
    }

    /* Main Logic */

    public boolean isStarted() {
        return trainer != null && trainer.isAlive();
    }

    public void start(long iterations) {
        if (isStarted())
            throw new IllegalStateException("Training has already started before");
        intermediateResult = null;
        trainer = new Trainer(iterations);
        trainer.start();
    }

    public void start() {
        start(Long.MAX_VALUE);
    }

    public void requestResult(Callback<T> callback, boolean interrupt) {
        if (!isStarted())
            throw new IllegalStateException("Learning process is not started");
        trainer.requestResult(callback, interrupt);
    }

    @NotNull
    public Result<T> awaitResult(boolean interruptTraining) {
        if (!isStarted())
            throw new IllegalStateException("Learning process is not started");
        if (interruptTraining)
            trainer.interrupt();
        try {
            trainer.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("Calculation thread was terminated");
        }
        return trainer.pendingResult;
    }

    /* Builder */

    public static <T extends Path> GeneticModel<T>.Builder newBuilder() {
        return new GeneticModel<T>().new Builder();
    }

    public class Builder {
        public Builder setPopulationSupplier(PopulationSupplier<T> supplier) {
            GeneticModel.this.populationSupplier = supplier;
            return this;
        }

        public Builder setSelector(Selector<T> selector) {
            GeneticModel.this.selector = selector;
            return this;
        }

        public Builder setBreeder(Breeder<T> breeder) {
            GeneticModel.this.breeder = breeder;
            return this;
        }

        public Builder setMutator(Mutator<T> mutator) {
            GeneticModel.this.mutator = mutator;
            return this;
        }

        public Builder setPopulationSize(int size) {
            GeneticModel.this.populationSize = size;
            return this;
        }

        public Builder setMutationRate(double rate) {
            GeneticModel.this.mutationRate = rate;
            return this;
        }

        public Builder setBreedStrategy(BreedStrategy strategy) {
            GeneticModel.this.breedStrategy = strategy;
            return this;
        }

        public GeneticModel<T> build() {
            return GeneticModel.this.clone();
        }
    }

    public enum BreedStrategy {
        BEST,
        ALL,
        BEST_TO_ALL
    }

    private class Trainer extends Thread {
        private volatile T[] population;
        private final Random rand = new Random(System.currentTimeMillis());
        private Callback<T> callback = null;
        private Result<T> pendingResult = null;
        private final long iterations; // training completed automatically after this iterations count

        public Trainer(long iterations) {
            this.iterations = iterations;
        }

        @Override
        public void run() {
            long iterations = 0;
            population = getInitialPopulation();
            Arrays.sort(population, selector);
            intermediateResult = new Result<>(0, population);
            while (!interrupted() && iterations < this.iterations) {
                Pair<T, T> parents = getParents();
                T child = breeder.apply(parents.getFirst(), parents.getSecond());
                insert(rand.nextDouble() < mutationRate ? mutator.apply(child) : child);
                intermediateResult = new Result<>(iterations, population);
                iterations++;
            }
            pendingResult = new Result<>(iterations, population);
            if (callback != null)
                callback.onResult(pendingResult);
        }

        /**
         * Interrupts thread if needed and registers disposable callback for result.<br/>
         * If it's already interrupted, immediately returns result via callback.
         */
        public void requestResult(Callback<T> callback, boolean interrupt) {
            if (pendingResult != null)
                callback.onResult(pendingResult);
            else {
                this.callback = callback;
                if (interrupt)
                    interrupt();
            }
        }

        @SuppressWarnings("unchecked")
        private T[] getInitialPopulation() {
            return IntStream.range(0, populationSize)
                    .mapToObj(i -> populationSupplier.get()).toArray(size ->
                            (T[]) Array.newInstance(populationSupplier.getElementClass(), size)
                    );
        }

        private void insert(T path) {
            int index = populationSize - 1;
            while (index >= 0 && selector.compare(path, population[index]) <= 0) {
                if (index > 0)
                    population[index] = population[index - 1];
                index--;
            }
            if (++index < populationSize)
                population[index] = path;
        }

        private Pair<T, T> getParents() {
            switch (breedStrategy) {
                case ALL: return Pair.of(
                        population[rand.nextInt(populationSize)],
                        population[rand.nextInt(populationSize)]
                );
                case BEST: return Pair.of(population[0], population[1]);
                case BEST_TO_ALL: return Pair.of(
                        population[0],
                        population[rand.nextInt(populationSize)]
                );
                default: throw new RuntimeException(); // unreachable
            }
        }
    }

    public interface Callback<T extends Path> {
        void onResult(Result<T> result);
    }

    public static class Result<T extends Path> {
        public final long iterations;
        public final T[] population;

        public Result(long iterations, T[] population) {
            this.iterations = iterations;
            this.population = population;
        }
    }
}
