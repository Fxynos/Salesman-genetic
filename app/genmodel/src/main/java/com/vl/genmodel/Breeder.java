package com.vl.genmodel;

import java.util.function.BiFunction;

public interface Breeder<T extends Path> extends BiFunction<T, T, T> {}
