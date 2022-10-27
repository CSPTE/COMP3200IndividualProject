package com.example.finalversion.ui.statistics;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.example.finalversion.R;
import com.example.finalversion.databinding.FragmentStatisticsBinding;
import com.example.finalversion.taskSettings.ColorList;
import com.example.finalversion.taskSettings.ColorObject;
import com.example.finalversion.taskSettings.ColorSpinnerAdapter;
import com.example.finalversion.ui.slideshow.SlideshowViewModel;

public class StatisticsFragment extends Fragment {
    private ColorObject selectedColor;

    private FragmentStatisticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StatisticsViewModel statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textSlideshow;
        //slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //ColorSpinner
        ColorList cl = new ColorList();
        selectedColor = cl.getDefaultColor();
        Spinner spinner = (Spinner) view.findViewById(R.id.colorSpinnerFutureTask);
        ColorSpinnerAdapter customAdapter = new ColorSpinnerAdapter(this.getContext(), cl.basicColors());
        spinner.setAdapter(customAdapter);
        spinner.setSelection(cl.colorPosition(selectedColor),false);
        AdapterView.OnItemSelectedListener selectedItemListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedColor = cl.basicColors().get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        spinner.setOnItemSelectedListener(selectedItemListener);
    }
}