package com.vl.salesman.genlearning;

import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.vl.genmodel.GeneticModel;
import com.vl.genmodel.salesman.VerbosePath;
import com.vl.salesman.MetricsConverter;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

class GenModelController extends Timer {

    private final static int DELAY = 50;
    private final int pointsCount;
    private final long iterations;
    private final GeneticModel<VerbosePath> geneticModel;
    private final DisplayMetrics metrics;
    private LearningProcessDialog dialog;
    @Nullable
    private Consumer<GeneticModel.Result<VerbosePath>> onComplete;
    @Nullable
    private GeneticModel.Result<VerbosePath> pendingResult;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean
            visitedAll = false,
            returnedToOrigin = false;

    public GenModelController(
            GeneticModel<VerbosePath> geneticModel,
            long iterations,
            int pointsCount, // to check whether path contains all points
            DisplayMetrics metrics // to convert coordinates to cm
    ) {
        this.metrics = metrics;
        this.geneticModel = geneticModel;
        this.iterations = iterations;
        this.pointsCount = pointsCount;
        geneticModel.start(iterations);
        geneticModel.requestResult(false, this::onResult);
        schedule(new Task(), DELAY, DELAY);
    }

    public void showDialog(FragmentManager fragmentManager) {
        this.dialog = new LearningProcessDialog();
        dialog.setOnCompleteClickListener(this::onCompleteClick);
        dialog.show(fragmentManager, null);
    }

    public void hideDialog() {
        LearningProcessDialog dialog = this.dialog;
        this.dialog = null;
        dialog.dismissAllowingStateLoss();
        visitedAll = false;
        returnedToOrigin = false;
    }

    public void setCallback(Consumer<GeneticModel.Result<VerbosePath>> callback) {
        if (pendingResult == null)
            onComplete = callback;
        else {
            callback.accept(pendingResult);
            pendingResult = null;
        }
    }

    private void onResult(GeneticModel.Result<VerbosePath> result) {
        cancel();
        mainHandler.post(() -> {
            if (onComplete == null)
                pendingResult = result;
            else
                onComplete.accept(result);
        });
    }

    private void onCompleteClick(View view) {
        geneticModel.awaitResult(true);
    }

    private class Task extends TimerTask {
        @Override
        public void run() {
            GeneticModel.Result<VerbosePath> result = geneticModel.getIntermediateResult();
            if (result != null) mainHandler.post(() -> {
                if (dialog == null)
                    return;
                int[] points = result.population[0].getPoints();
                if (!visitedAll && Arrays.stream(points).distinct().count() == pointsCount) {
                    visitedAll = true;
                    dialog.markVisitedAllPoints();
                }
                if (!returnedToOrigin) {
                    if (points[0] == points[points.length - 1]) {
                        returnedToOrigin = true;
                        dialog.markReturnedToOrigin();
                    }
                }
                dialog.setLength(MetricsConverter.fromCoordinatesToCentimeters(metrics, result.population[0].getLength()));
                dialog.setPathPointsCount(points.length);
                dialog.setProgress(result.iterations / (double) iterations);
                dialog.setIterationsCount(result.iterations);
            });
        }
    }
}
