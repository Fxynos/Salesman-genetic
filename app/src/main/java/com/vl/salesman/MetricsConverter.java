package com.vl.salesman;

import android.util.DisplayMetrics;

public class MetricsConverter {
    public static double fromCoordinatesToInches(DisplayMetrics metrics, double coordinate) {
        return coordinate / metrics.densityDpi;
    }

    public static double fromCoordinatesToCentimeters(DisplayMetrics metrics, double coordinate) {
        return fromCoordinatesToInches(metrics, coordinate) * 2.54;
    }
}
