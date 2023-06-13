package com.vl.salesman.genlearning;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.vl.genmodel.GeneticModel;
import com.vl.genmodel.salesman.VerbosePath;

import java.util.function.Consumer;

public class GenLearningViewModel extends ViewModel {
    private GenModelController learningController;
    @Nullable
    private Consumer<GeneticModel.Result<VerbosePath>> activityCallback;
    @Nullable
    private GeneticModel.Result<VerbosePath> pendingResult;

    public void onLearningLaunch(GenModelController learningController) {
        this.learningController = learningController;
        learningController.setCallback(this::onResult);
    }

    public GenModelController getLearningController() {
        return learningController;
    }

    public void setResultCallback(Consumer<GeneticModel.Result<VerbosePath>> activityCallback) {
        if (pendingResult == null || activityCallback == null)
            this.activityCallback = activityCallback;
        else {
            activityCallback.accept(pendingResult);
            pendingResult = null;
        }
    }

    private void onResult(GeneticModel.Result<VerbosePath> result) {
        if (activityCallback == null)
            pendingResult = result;
        else
            activityCallback.accept(result);
    }
}
