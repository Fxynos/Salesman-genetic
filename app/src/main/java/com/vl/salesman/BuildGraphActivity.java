package com.vl.salesman;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.vl.salesman.databinding.ActivityBuildGraphBinding;
import com.vl.salesman.item.Point;
import com.vl.salesman.view.GraphSurfaceController;

import org.jetbrains.annotations.NotNull;

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
        Stream.of(binding.floatingButton, binding.info, binding.apply)
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
            case R.id.info:
                // TODO
                break;
            case R.id.apply:
                // TODO
                break;
        }
    }

    private void updateFloatingButtonIcon() {
        binding.floatingButton.setImageResource(isPointChecked ? R.drawable.ic_delete : R.drawable.ic_add);
    }
}
