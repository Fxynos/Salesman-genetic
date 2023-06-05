package com.vl.salesman;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.vl.salesman.databinding.ActivityBuildGraphBinding;
import com.vl.salesman.item.Point;
import com.vl.salesman.view.GraphSurfaceController;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

public class BuildGraphActivity extends AppCompatActivity implements View.OnClickListener, GraphSurfaceController.GraphSurfaceCallback {
    private ActivityBuildGraphBinding binding;
    private GraphSurfaceController surfaceController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuildGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Stream.of(binding.addPoint, binding.info, binding.apply)
                .forEach(b -> b.setOnClickListener(this));
        demo();
        surfaceController = new GraphSurfaceController(binding.buildGraphSurface);
        surfaceController.setCallback(this);
    }

    @Override
    public void onPointPlaced(Point point) {
        binding.addPoint.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPointChecked(Point point) {
        // TODO
    }

    private void demo() {
        Point
                p1 = new Point(200f, 200f),
                p2 = new Point(200f, 400f),
                p3 = new Point(400f, 200f),
                p4 = new Point(400f, 400f);
        binding.buildGraphSurface.getPoints().addAll(Arrays.asList(p1, p2, p3, p4));
        binding.buildGraphSurface.getContacts().addAll(Arrays.asList(
                new Pair<>(new Point[] {p1, p2}, false),
                new Pair<>(new Point[] {p3, p4}, true)
        ));
    }

    @Override
    public void onClick(@NotNull View view) {
        switch (view.getId()) {
            case R.id.add_point:
                binding.addPoint.setVisibility(View.GONE);
                surfaceController.requestPointPlace();
                break;
            case R.id.info:
                // TODO
                break;
            case R.id.apply:
                // TODO
                break;
        }
    }
}
