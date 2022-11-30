package com.example.finalversion.ui.gallery;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.finalversion.R;

import java.util.ArrayList;

public class ColorBox extends LinearLayout {
    private int nrOfTasks;
    private Context cont;
    private ArrayList<LinearLayout> whole = new ArrayList<LinearLayout>();
    private ArrayList<LinearLayout> halves = new ArrayList<LinearLayout>();
    private ArrayList<LinearLayout> thirds = new ArrayList<LinearLayout>();
    private ArrayList<LinearLayout> fourths = new ArrayList<LinearLayout>();

    public ColorBox(Context context) {
        super(context);
    }

    public ColorBox(Context context, int nr) {
        super(context);
        cont = context;
        nrOfTasks = nr;
        if (nrOfTasks == 0){
            initTaskZero();
        } else if (nrOfTasks == 1){
            initTaskOne();
        } else if (nrOfTasks == 2){
            initTaskTwo();
        } else if (nrOfTasks == 3){
            initTaskThree();
        } else if (nrOfTasks == 4){
            initTaskFour();
        }
    }

    private void initTaskZero() {
    }

    private void initTaskOne() {
        LayoutInflater inflater = (LayoutInflater) cont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.calendar_square_one, this);
        whole.add(findViewById(R.id.fullCircle));
    }

    public void initTaskOneSectors(String color1){
        whole.get(0).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color1)));
    }

    private void initTaskTwo() {
        LayoutInflater inflater = (LayoutInflater) cont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.calendar_square_two, this);
        halves.add(findViewById(R.id.firstHalf));
        halves.add(findViewById(R.id.secondHalf));
        //initTaskTwoSectors("#00FF00", "#FFFF00");
    }

    public void initTaskTwoSectors(String color1, String color2){
        halves.get(0).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color1)));
        halves.get(1).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color2)));
    }

    private void initTaskThree() {
        LayoutInflater inflater = (LayoutInflater) cont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.calendar_square_three, this);
        thirds.add(findViewById(R.id.firstThird));
        thirds.add(findViewById(R.id.secondThird));
        thirds.add(findViewById(R.id.thirdThird));
    }

    public void initTaskThreeSectors(String color1, String color2, String color3){
        thirds.get(0).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color1)));
        thirds.get(1).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color2)));
        thirds.get(2).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color3)));
    }

    private void initTaskFour() {
        LayoutInflater inflater = (LayoutInflater) cont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.calendar_square_four, this);
        fourths.add(findViewById(R.id.firstQuarter));
        fourths.add(findViewById(R.id.secondQuarter));
        fourths.add(findViewById(R.id.thirdQuarter));
        fourths.add(findViewById(R.id.fourthQuarter));
    }

    public void initTaskFourSectors(String color1, String color2, String color3, String color4){
        fourths.get(0).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color1)));
        fourths.get(1).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color2)));
        fourths.get(2).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color3)));
        fourths.get(3).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color4)));
    }
}
