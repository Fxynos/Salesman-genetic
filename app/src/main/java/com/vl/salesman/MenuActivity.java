package com.vl.salesman;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vl.salesman.databinding.ActivityMenuBinding;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMenuBinding binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Stream.of(binding.buildGraph, binding.info, binding.exit)
                .forEach(b -> b.setOnClickListener(this));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NotNull View view) {
        switch (view.getId()) {
            case R.id.build_graph:
                startActivity(new Intent(this, BuildGraphActivity.class));
                finish();
                break;
            case R.id.build_graph_info:
                // TODO
                break;
            case R.id.exit:
                finish();
                break;
        }
    }
}
