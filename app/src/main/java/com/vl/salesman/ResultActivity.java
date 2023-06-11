package com.vl.salesman;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.vl.genmodel.VerbosePath;
import com.vl.salesman.bundlewrapper.GraphData;
import com.vl.salesman.bundlewrapper.ResultData;
import com.vl.salesman.databinding.ActivityResultBinding;
import com.vl.salesman.databinding.ResultFieldsBinding;
import com.vl.salesman.item.Point;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class ResultActivity extends AppCompatActivity {

    private ActivityResultBinding binding;
    private ResultFieldsBinding resultBinding;
    private VerbosePath path;
    private ResultData result;
    private GraphData graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        resultBinding = ResultFieldsBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());
        result = new ResultData(getIntent().getExtras());
        graph = new GraphData(getIntent().getExtras());
        path = new VerbosePath(result.getPath(), graph.getDistances());
        initResultViews();
        binding.graphSurface.getPoints().addAll(graph.getPoints().stream()
                .map(Point::new).collect(Collectors.toSet()));
        binding.graphSurface.getContacts().addAll(graph.getConnects().stream()
                .map(p -> new Pair<>(
                        new Point[]{new Point(p.first), new Point(p.second)},
                        false
                )).collect(Collectors.toSet()));
        Point startPoint = new Point(graph.getStartPoint());
        binding.graphSurface.getPoints().stream().filter(p -> p.equals(startPoint)).findAny()
                .orElseThrow(RuntimeException::new).setChecked(true);
    }

    private void initResultViews() {
        resultBinding.length.setText(String.format(Locale.getDefault(), "%.1f", path.getLength()));
        resultBinding.pointsCount.setText(String.format(Locale.getDefault(), "%d", path.getPoints().length));
        if (path.getPoints()[0] == path.getPoints()[path.getPoints().length - 1])
            checkmark(resultBinding.returnCheck);
        if (Arrays.stream(path.getPoints()).distinct().count() == graph.getPoints().size())
            checkmark(resultBinding.visitCheck);
        resultBinding.iteration.setText(String.format(Locale.getDefault(), "%d", result.getIterationsCount()));
    }

    private int obtainColorSecondary() {
        TypedValue typed = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typed, true);
        return typed.data;
    }

    private void checkmark(ImageView icon) {
        icon.setColorFilter(obtainColorSecondary());
        icon.setImageResource(R.drawable.ic_check);
    }
}
