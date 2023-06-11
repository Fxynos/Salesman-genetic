package com.vl.salesman;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.vl.salesman.bundlewrapper.GraphData;
import com.vl.salesman.databinding.ActivityBuildGraphBinding;
import com.vl.salesman.item.Point;
import com.vl.salesman.view.GraphSurfaceController;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildGraphActivity extends AppCompatActivity implements View.OnClickListener, GraphSurfaceController.GraphSurfaceCallback {
    private ActivityBuildGraphBinding binding;
    private GraphSurfaceController surfaceController;
    private boolean isPointChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuildGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        updateFloatingButtonIcon();
        Stream.of(binding.floatingButton, binding.buildGraphInfo, binding.apply)
                .forEach(b -> b.setOnClickListener(this));
        surfaceController = new GraphSurfaceController(binding.buildGraphSurface);
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
                GraphData graph = new GraphData();
                graph.setPoints(binding.buildGraphSurface.getPoints().stream()
                        .map(Point::getPoint).collect(Collectors.toSet()));
                graph.setConnects(binding.buildGraphSurface.getContacts().stream()
                        .map(p -> new Pair<>(p.first[0].getPoint(), p.first[1].getPoint()))
                        .collect(Collectors.toSet()));
                graph.setStartPoint(binding.buildGraphSurface.getPoints().stream()
                        .findAny().orElseThrow(RuntimeException::new));
                Intent intent = new Intent(this, GenLearningActivity.class);
                intent.putExtras(graph.getBundle());
                startActivity(intent);
                finish();
                break;
        }
    }

    private void updateFloatingButtonIcon() {
        binding.floatingButton.setImageResource(isPointChecked ? R.drawable.ic_delete : R.drawable.ic_add);
    }
}
