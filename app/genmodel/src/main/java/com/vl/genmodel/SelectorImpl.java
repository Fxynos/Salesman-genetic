package com.vl.genmodel;

import java.util.Arrays;

/**
 * Compares Paths by following features:
 * 1. Max points distinct visited
 * 2. Path returns to its origin
 * 3. Path length is min
 * 4. Overall visited points count is min
 */
public class SelectorImpl implements Selector<VerbosePath> {
    @Override
    public int compare(VerbosePath a, VerbosePath b) { // best - first, worst - last
        long
                aVisited = Arrays.stream(a.getPoints()).distinct().count(),
                bVisited = Arrays.stream(b.getPoints()).distinct().count();
        if (aVisited != bVisited)
            return Long.compare(bVisited, aVisited);
        boolean
                aReturned = (a.getPoints()[0] == a.getPoints()[a.getPoints().length - 1]),
                bReturned = (b.getPoints()[0] == b.getPoints()[b.getPoints().length - 1]);
        if (aReturned != bReturned)
            return Boolean.compare(bReturned, aReturned); // better off return to path origin
        if (a.getLength() != b.getLength())
            return Double.compare(a.getLength(), b.getLength());
        if (a.getPoints().length != b.getPoints().length)
            return Integer.compare(a.getPoints().length, b.getPoints().length);
        return 0;
    }
}
