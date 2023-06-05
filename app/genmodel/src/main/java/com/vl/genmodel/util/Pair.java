package com.vl.genmodel.util;

public interface Pair<T, U> {
    static <T, U> Pair<T, U> of(T first, U second) {
        return new Pair<T, U>() {
            @Override
            public T getFirst() {
                return first;
            }

            @Override
            public U getSecond() {
                return second;
            }
        };
    }

    T getFirst();

    U getSecond();
}
