package com.vl.genmodel;

import java.util.function.Supplier;

public interface PopulationSupplier<T> extends Supplier<T> {
    @SuppressWarnings("unchecked")
    default Class<T> getElementClass() {
        return (Class<T>) get().getClass();
    }
}
