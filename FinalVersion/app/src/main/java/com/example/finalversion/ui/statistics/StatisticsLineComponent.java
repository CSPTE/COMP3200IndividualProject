package com.example.finalversion.ui.statistics;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.finalversion.R;

public class StatisticsLineComponent extends ConstraintLayout {
    private String instructions;

    private TextView instructionsTextView;
    private TextView valueTextView;

    public StatisticsLineComponent(Context context, String instr) {
        super(context);
        instructions = instr;
        init(context);
    }

    private void init(Context cont) {
        LayoutInflater inflater = (LayoutInflater) cont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.statistics_line_component, this);
        instructionsTextView = findViewById(R.id.statisticsLineInstructions);
        valueTextView = findViewById(R.id.statisticsLineValue);

        instructionsTextView.setText(instructions);
        //setLine();
    }

    public void setValue(String value){
        valueTextView.setText(value);
    }
}
