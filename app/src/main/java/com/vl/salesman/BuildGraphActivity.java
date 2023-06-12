package com.vl.salesman;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.vl.salesman.bundlewrapper.GraphData;
import com.vl.salesman.databinding.ActivityBuildGraphBinding;
import com.vl.salesman.item.Point;
import com.vl.salesman.graphview.GraphSurfaceBuildingController;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildGraphActivity extends AppCompatActivity implements View.OnClickListener, GraphSurfaceBuildingController.GraphSurfaceCallback {
    private ActivityBuildGraphBinding binding;
    private GraphSurfaceBuildingController surfaceController;
    private boolean isPointChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuildGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        updateFloatingButtonIcon();
        Stream.of(binding.floatingButton, binding.buildGraphInfo, binding.apply)
                .forEach(b -> b.setOnClickListener(this));
        surfaceController = new GraphSurfaceBuildingController(binding.buildGraphSurface);
        surfaceController.setCallback(this);
    }

    @Override
    public void onPointPlaced(Point point) {
        binding.floatingButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPointChecked(Point point, boolean checked) {
        isPointChecked = checked;
        updateFloatingButtonIcon();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NotNull View view) {
        if (surfaceController.isGraphInIntermediateState())
            return;
        switch (view.getId()) {
            case R.id.floating_button:
                if (isPointChecked) {
                    if (surfaceController.requestRemovePoint()) {
                        isPointChecked = false;
                        updateFloatingButtonIcon();
                    }
                } else {
                    binding.floatingButton.setVisibility(View.GONE);
                    surfaceController.requestPointPlace();
                }
                break;
            case R.id.build_graph_info:
                // TODO
                break;
            case R.id.apply:
                Point checkedStartPoint = binding.buildGraphSurface.getPoints().stream()
                        .filter(Point::isChecked).findAny().orElse(null);
                if (checkedStartPoint == null)
                    new AlertDialog.Builder(this)
                            .setCancelable(true)
                            .setTitle("Выберите стартовую точку")
                            .setMessage("Коснитесь точки, чтобы выбрать её")
                            .setPositiveButton("Ок", (dI, i) -> {})
                            .show();
                else {
                    GraphData graph = new GraphData();
                    graph.setPoints(binding.buildGraphSurface.getPoints().stream()
                            .map(Point::getPoint).collect(Collectors.toSet()));
                    graph.setConnects(binding.buildGraphSurface.getContacts().stream()
                            .map(p -> new Pair<>(p.first[0].getPoint(), p.first[1].getPoint()))
                            .collect(Collectors.toSet()));
                    graph.setStartPoint(checkedStartPoint);
                    Intent intent = new Intent(this, GenLearningActivity.class);
                    intent.putExtras(graph.getBundle());
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

    private void updateFloatingButtonIcon() {
        binding.floatingButton.setImageResource(isPointChecked ? R.drawable.ic_delete : R.drawable.ic_add);
    }
}
