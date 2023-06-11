package com.vl.salesman;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.vl.genmodel.Path;
import com.vl.genmodel.VerbosePath;
import com.vl.salesman.bundlewrapper.GraphData;
import com.vl.salesman.bundlewrapper.ResultData;
import com.vl.salesman.databinding.ActivityResultBinding;
import com.vl.salesman.databinding.ResultFieldsBinding;
import com.vl.salesman.graphview.GraphSurfaceVisualisationController;
import com.vl.salesman.item.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ResultActivity extends AppCompatActivity {

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
        initResultViews();
        initGraph();
        pathOfPoints = obtainPath(path);
        binding.play.setOnClickListener(this::onPlayClicked);
    }

    private void onPlayClicked(View view) {
        if (visualization)
            graphController.stopVisualization();
        else
            graphController.startVisualization(pathOfPoints);
        visualization = !visualization;
        updateFloatingButtonIcon();
    }

    private void onVisualizationEnd() {
        visualization = false;
        updateFloatingButtonIcon();
    }

    private void updateFloatingButtonIcon() {
        binding.play.setImageResource(visualization ? R.drawable.ic_stop : R.drawable.ic_play);
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
