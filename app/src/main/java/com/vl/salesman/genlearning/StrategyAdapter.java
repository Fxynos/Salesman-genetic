package com.vl.salesman.genlearning;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vl.genmodel.GeneticModel;
import com.vl.salesman.R;
import com.vl.salesman.databinding.SpinnerStrategyBinding;

class StrategyAdapter extends ArrayAdapter<GeneticModel.BreedStrategy> { // spinner adapter

    public StrategyAdapter(@NonNull Context context) {
        super(context, R.layout.spinner_strategy, GeneticModel.BreedStrategy.values());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SpinnerStrategyBinding binding = convertView == null ?
                SpinnerStrategyBinding.inflate(LayoutInflater.from(getContext())) :
                SpinnerStrategyBinding.bind(convertView);
        switch (getItem(position)) {
            case BEST:
                binding.strategy.setText(R.string.better_ones);
                break;
            case BEST_TO_ALL:
                binding.strategy.setText(R.string.better_with_any);
                break;
            case ALL:
                binding.strategy.setText(R.string.any_pair);
                break;
            default:
                throw new RuntimeException(); // unreachable
        }
        return binding.getRoot();
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
