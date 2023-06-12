package com.vl.genmodel;

import java.util.function.BiFunction;

public interface Breeder<T> extends BiFunction<T, T, T> {}
