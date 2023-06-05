package com.vl.genmodel;

import java.util.function.Function;

public interface Mutator<T extends Path> extends Function<T, T> {}
