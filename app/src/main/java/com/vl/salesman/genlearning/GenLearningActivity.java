package com.vl.salesman.genlearning;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.vl.genmodel.salesman.BreederImpl;
import com.vl.genmodel.GeneticModel;
import com.vl.genmodel.salesman.MutatorImpl;
import com.vl.genmodel.salesman.PopulationSupplierImpl;
import com.vl.genmodel.salesman.SelectorImpl;
import com.vl.genmodel.salesman.VerbosePath;
import com.vl.salesman.InfoDialog;
import com.vl.salesman.R;
import com.vl.salesman.ResultActivity;
import com.vl.salesman.bundlewrapper.GraphData;
import com.vl.salesman.bundlewrapper.ResultData;
import com.vl.salesman.databinding.ActivityGenlearningBinding;
import com.vl.salesman.graphbuild.BuildGraphActivity;

import java.util.LinkedList;
import java.util.Locale;
import java.util.stream.Stream;

public class GenLearningActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private ActivityGenlearningBinding binding;
    private StrategyAdapter adapter;
    private GraphData graphData;
    private GenLearningViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGenlearningBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        graphData = new GraphData(getIntent().getExtras());
        binding.strategies.setAdapter(adapter = new StrategyAdapter(this));
        binding.strategies.setSelection(0);
        Stream.of(binding.launch, binding.back, binding.learningInfo).forEach(b -> b.setOnClickListener(this));
        binding.mutationChance.setOnSeekBarChangeListener(this);
        binding.mutationChance.setProgress(50);
        binding.iterations.setOnEditorActionListener((textView, i, keyEvent) -> {
            textView.clearFocus();
            return false;
        });
        viewModel = new ViewModelProvider(this).get(GenLearningViewModel.class);
        viewModel.setResultCallback(this::onResult);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GenModelController controller = viewModel.getLearningController();
        if (controller != null)
            controller.showDialog(getSupportFragmentManager());
    }

    @Override
    protected void onStop() {
        super.onStop();
        GenModelController controller = viewModel.getLearningController();
        if (controller != null)
            controller.hideDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.setResultCallback(null); // to prevent memory leaks
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
            case R.id.learning_info:
                InfoDialog.show(this, getString(R.string.hint), getString(R.string.learning_hint));
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
        viewModel.onLearningLaunch(
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
                        .build(), iterations, distances.length, getResources().getDisplayMetrics()
                )
        );
        viewModel.getLearningController().showDialog(getSupportFragmentManager());
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