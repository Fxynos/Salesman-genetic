package com.vl.salesman.bundlewrapper;

import android.graphics.PointF;
import android.os.Bundle;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.util.List;

public class ResultData extends BaseBundleWrapper {
    private static final String
            EXTRA_ITERATIONS = "iterations",
            EXTRA_PATH = "path";

    public ResultData(Bundle bundle) {
        super(bundle);
    }

    public ResultData() {
        this(new Bundle());
    }

    public void setIterationsCount(long iterations) {
        put(EXTRA_ITERATIONS, iterations);
    }

    public long getIterationsCount() {
        return get(EXTRA_ITERATIONS, Long.class);
    }

    public void setPath(int[] path) {
        put(EXTRA_PATH, path);
    }

    public int[] getPath() {
        return get(EXTRA_PATH, new TypeToken<List<Integer>>(){}).stream()
                .mapToInt(Integer::intValue)
                .toArray();
    }
}
