package com.vl.salesman;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.vl.salesman.databinding.FragmentLearningProcessBinding;
import com.vl.salesman.databinding.ResultFieldsBinding;

import java.util.Locale;

public class LearningProcessDialog extends DialogFragment {

    private FragmentLearningProcessBinding binding;
    private ResultFieldsBinding resultBinding;
    private View.OnClickListener bufListener = null; // for case setOnCompleteClickListener() before onCreateView()
    private int checkedColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireDialog().setCancelable(false);
        binding = FragmentLearningProcessBinding.inflate(inflater);
        resultBinding = ResultFieldsBinding.bind(binding.getRoot());
        binding.progress.setProgress(0);
        resultBinding.length.setText("0");
        resultBinding.pointsCount.setText("1");
        resultBinding.iteration.setText("0");
        if (bufListener != null) {
            binding.complete.setOnClickListener(bufListener);
            bufListener = null;
        }
        checkedColor = obtainSecondaryColor();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = requireDialog().getWindow();
        window.setBackgroundDrawable(null);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void setOnCompleteClickListener(View.OnClickListener listener) {
        if (binding == null)
            this.bufListener = listener;
        else
            binding.complete.setOnClickListener(listener);
    }

    public void markVisitedAllPoints() {
        resultBinding.visitCheck.setColorFilter(checkedColor);
        resultBinding.visitCheck.setImageResource(R.drawable.ic_check);
    }

    public void markReturnedToOrigin() {
        resultBinding.returnCheck.setColorFilter(checkedColor);
        resultBinding.returnCheck.setImageResource(R.drawable.ic_check);
    }

    public void setLength(double length) {
        resultBinding.length.setText(String.format(Locale.getDefault(), "%.1f", length));
    }

    public void setProgress(double percent) {
        binding.progress.setProgress((int) (binding.progress.getMax() * percent), true);
    }

    public void setPathPointsCount(int count) {
        resultBinding.pointsCount.setText(String.format(Locale.getDefault(), "%d", count));
    }

    public void setIterationsCount(long iterations) {
        resultBinding.iteration.setText(String.format(Locale.getDefault(), "%d", iterations));
    }

    private int obtainSecondaryColor() {
        TypedValue typed = new TypedValue();
        requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typed, true);
        return typed.data;
    }
}
