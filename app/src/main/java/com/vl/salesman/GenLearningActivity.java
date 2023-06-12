package com.vl.salesman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.vl.genmodel.salesman.BreederImpl;
import com.vl.genmodel.GeneticModel;
import com.vl.genmodel.salesman.MutatorImpl;
import com.vl.genmodel.salesman.PopulationSupplierImpl;
import com.vl.genmodel.salesman.SelectorImpl;
import com.vl.genmodel.salesman.VerbosePath;
import com.vl.salesman.bundlewrapper.GraphData;
import com.vl.salesman.bundlewrapper.ResultData;
import com.vl.salesman.databinding.ActivityGenlearningBinding;
import com.vl.salesman.databinding.SpinnerStrategyBinding;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GenLearningActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private ActivityGenlearningBinding binding;
    private StrategyAdapter adapter;
    private GraphData graphData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGenlearningBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        graphData = new GraphData(getIntent().getExtras());
        binding.strategies.setAdapter(adapter = new StrategyAdapter(this));
        binding.strategies.setSelection(0);
        Stream.of(binding.launch, binding.back).forEach(b -> b.setOnClickListener(this));
        binding.mutationChance.setOnSeekBarChangeListener(this);
        binding.mutationChance.setProgress(50);
        binding.iterations.setOnEditorActionListener((textView, i, keyEvent) -> {
            textView.clearFocus();
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, BuildGraphActivity.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.launch:
                onLaunchLearning();
                break;
        }
    }

    private void onLaunchLearning() {
        if (Stream.of(
                binding.genFrom,
                binding.genTo,
                binding.mutationFrom,
                binding.mutationTo,
                binding.population,
                binding.iterations
        ).map(it -> it.getText().toString()).anyMatch(String::isEmpty)) {
            onMalformedInput("Заполните все поля");
            return;
        }
        int
                mutationFrom = Integer.parseInt(binding.mutationFrom.getText().toString()),
                mutationTo = Integer.parseInt(binding.mutationTo.getText().toString()),
                genFrom = Integer.parseInt(binding.genFrom.getText().toString()),
                genTo = Integer.parseInt(binding.genTo.getText().toString()),
                population = Integer.parseInt(binding.population.getText().toString()),
                iterations = Integer.parseInt(binding.iterations.getText().toString());

        if (genFrom < 1 || mutationFrom < 1) {
            onMalformedInput("Значения \"От\" должны быть не меньше 1");
            return;
        }
        if (genFrom > genTo || mutationFrom > mutationTo) {
            onMalformedInput("Значения \"От\" должны быть не больше значений \"До\"");
            return;
        }
        if (population < 2) {
            onMalformedInput("Численность популяции должна быть не меньше 2");
            return;
        }

        Double[][] distances = graphData.getDistances();
        int startPoint = new LinkedList<>(graphData.getPoints()).indexOf(graphData.getStartPoint());
        new GenModelController(
                GeneticModel.<VerbosePath>newBuilder()
                        .setBreeder(
                                new BreederImpl(distances)
                        ).setBreedStrategy(
                                adapter.getItem(binding.strategies.getSelectedItemPosition())
                        ).setMutator(
                                new MutatorImpl(distances, mutationFrom, mutationTo)
                        ).setMutationRate(
                                binding.mutationChance.getProgress() / (double) binding.mutationChance.getMax()
                        ).setSelector(
                                new SelectorImpl()
                        ).setPopulationSupplier(
                                new PopulationSupplierImpl(genFrom, genTo, distances, startPoint)
                        ).setPopulationSize(population)
                        .build(), iterations, distances.length, getSupportFragmentManager(), this::onResult
        );
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        binding.mutationChanceText.setText(String.format(Locale.getDefault(), "%d%%", i));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    private void onMalformedInput(String message) {
        InfoDialog.show(this, "Проверьте правильность ввода", message);
    }

    private void onResult(GeneticModel.Result<VerbosePath> result) {
        ResultData data = new ResultData();
        data.setIterationsCount(result.iterations);
        data.setPath(result.population[0].getPoints());
        graphData.merge(data.getBundle());
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtras(graphData.getBundle());
        startActivity(intent);
        finish();
    }
}

class StrategyAdapter extends ArrayAdapter<GeneticModel.BreedStrategy> {

    public StrategyAdapter(@NonNull Context context) {
        super(context, R.layout.spinner_strategy, GeneticModel.BreedStrategy.values());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SpinnerStrategyBinding binding = convertView == null ?
                SpinnerStrategyBinding.inflate(LayoutInflater.from(getContext())) :
                SpinnerStrategyBinding.bind(convertView);
        switch (getItem(position)) {
            case BEST: binding.strategy.setText("Только двое лучших"); break;
            case BEST_TO_ALL: binding.strategy.setText("Лучший с любым из популяции"); break;
            case ALL: binding.strategy.setText("Любые двое из популяции"); break;
            default: throw new RuntimeException(); // unreachable
        }
        return binding.getRoot();
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}

class GenModelController extends Timer {

    private final static int DELAY = 50;
    private final int pointsCount;
    private final long iterations;
    private final GeneticModel<VerbosePath> geneticModel;
    private final LearningProcessDialog dialog;
    private final Consumer<GeneticModel.Result<VerbosePath>> onComplete;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean
            visitedAll = false,
            returnedToOrigin = false;

    public GenModelController(
            GeneticModel<VerbosePath> geneticModel,
            long iterations,
            int pointsCount, // to check whether path contains all points
            FragmentManager fragmentManager,
            Consumer<GeneticModel.Result<VerbosePath>> onComplete) {
        this.geneticModel = geneticModel;
        this.iterations = iterations;
        this.pointsCount = pointsCount;
        this.dialog = new LearningProcessDialog();
        this.onComplete = onComplete;
        dialog.setOnCompleteClickListener(this::onCompleteClick);
        dialog.show(fragmentManager, null);
        geneticModel.start(iterations);
        geneticModel.requestResult(this::onResult, false);
        schedule(new Task(), DELAY, DELAY);
    }

    public void onResult(GeneticModel.Result<VerbosePath> result) {
        cancel();
        mainHandler.post(() -> {
            dialog.dismiss();
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
                dialog.setLength(result.population[0].getLength());
                dialog.setPathPointsCount(points.length);
                dialog.setProgress(result.iterations / (double) iterations);
                dialog.setIterationsCount(result.iterations);
            });
        }
    }
}