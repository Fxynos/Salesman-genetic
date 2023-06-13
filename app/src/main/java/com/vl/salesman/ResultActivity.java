package com.vl.salesman;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.AttrRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.vl.genmodel.salesman.Path;
import com.vl.genmodel.salesman.VerbosePath;
import com.vl.salesman.bundlewrapper.GraphData;
import com.vl.salesman.bundlewrapper.ResultData;
import com.vl.salesman.databinding.ActivityResultBinding;
import com.vl.salesman.databinding.ResultFieldsBinding;
import com.vl.salesman.genlearning.GenLearningActivity;
import com.vl.salesman.graphview.GraphSurfaceVisualisationController;
import com.vl.salesman.graphview.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityResultBinding binding;
    private ResultFieldsBinding resultBinding;
    private GraphSurfaceVisualisationController graphController;
    private VerbosePath path;
    private Point[] pathOfPoints;
    private ResultData result;
    private GraphData graph;
    private boolean visualization = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        resultBinding = ResultFieldsBinding.bind(binding.getRoot());
        setContentView(binding.getRoot());
        updateFloatingButtonIcon();
        result = new ResultData(getIntent().getExtras());
        graph = new GraphData(getIntent().getExtras());
        path = new VerbosePath(result.getPath(), graph.getDistances());
        if (path.getPoints().length == 1) {
            binding.play.setEnabled(false);
            binding.play.setVisibility(View.GONE);
        }
        initResultViews();
        initGraph();
        pathOfPoints = obtainPath(path);
        Stream.of(binding.back, binding.play, binding.resultInfo).forEach(b -> b.setOnClickListener(this));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, GenLearningActivity.class);
        intent.putExtras(graph.getBundle());
        startActivity(intent);
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.result_info:
                InfoDialog.show(this, getString(R.string.hint), getString(R.string.result_hint));
                break;
            case R.id.play:
                if (visualization)
                    graphController.stopVisualization();
                else
                    graphController.startVisualization(pathOfPoints);
                visualization = !visualization;
                updateFloatingButtonIcon();
                break;
        }
    }

    private void onVisualizationEnd() {
        visualization = false;
        updateFloatingButtonIcon();
    }

    private void updateFloatingButtonIcon() {
        binding.play.setImageResource(visualization ? R.drawable.ic_stop : R.drawable.ic_play);
    }

    private void initResultViews() {
        resultBinding.length.setText(String.format(Locale.getDefault(), "%.1f", MetricsConverter.fromCoordinatesToCentimeters(getResources().getDisplayMetrics(), path.getLength())));
        resultBinding.pointsCount.setText(String.format(Locale.getDefault(), "%d", path.getPoints().length));
        if (path.getPoints()[0] == path.getPoints()[path.getPoints().length - 1])
            checkmark(resultBinding.returnCheck);
        if (Arrays.stream(path.getPoints()).distinct().count() == graph.getPoints().size())
            checkmark(resultBinding.visitCheck);
        resultBinding.iteration.setText(String.format(Locale.getDefault(), "%d", result.getIterationsCount()));
    }

    private void initGraph() { // init surface and controller
        binding.graphSurface.getPoints().addAll(graph.getPoints().stream()
                .map(Point::new).collect(Collectors.toSet()));
        binding.graphSurface.getContacts().addAll(graph.getConnects().stream()
                .map(p -> new Pair<>(
                        new Point[]{new Point(p.first), new Point(p.second)},
                        false
                )).collect(Collectors.toSet()));
        graphController = new GraphSurfaceVisualisationController(
                binding.graphSurface,
                binding.graphSurface.getPoints().stream()
                        .filter(p -> p.equals(new Point(graph.getStartPoint())))
                        .findAny().orElseThrow(RuntimeException::new)
        );
        graphController.setOnVisualizationEnd(this::onVisualizationEnd);
    }

    private Point[] obtainPath(Path path) {
        List<Point> points = new ArrayList<>(binding.graphSurface.getPoints());
        return IntStream.of(path.getPoints()).mapToObj(points::get).toArray(Point[]::new);
    }

    private int obtainThemeColor(@AttrRes int color) {
        TypedValue typed = new TypedValue();
        getTheme().resolveAttribute(color, typed, true);
        return typed.data;
    }

    private void checkmark(ImageView icon) {
        icon.setColorFilter(obtainThemeColor(com.google.android.material.R.attr.colorSecondary));
        icon.setImageResource(R.drawable.ic_check);
    }
}
