package com.example.finalversion.ui.statistics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.finalversion.R;
import com.example.finalversion.databinding.FragmentStatisticsBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatisticsFragment extends Fragment {
    private Context statisticsContext;
    private ArrayList<String> tasks = new ArrayList<String>();
    private ArrayList<String> subTasks = new ArrayList<String>();
    private LinearLayout tasksLayout;

    private FragmentStatisticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StatisticsViewModel statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        statisticsContext = this.getContext();

        //final TextView textView = binding.textSlideshow;
        //slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tasksLayout = view.findViewById(R.id.addStatisticsComponents);

        //Habit Selector
        Button bOpenAlertDialog = view.findViewById(R.id.selectHabitsStatistics);
        tasks.add("All");
        findTasks(statisticsContext.getFilesDir());
        int numberOfChoices = tasks.size();
        String[] listItems = new String[numberOfChoices];
        for (int i=0; i<numberOfChoices; i++){
            listItems[i] = tasks.get(i);
        }
        final boolean[] checkedItems = new boolean[listItems.length];
        final List<String> selectedItems = Arrays.asList(listItems);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            loadAllAsDefault();
        }

        bOpenAlertDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(statisticsContext);
                builder.setTitle("Select Tasks");
                builder.setIcon(R.mipmap.ic_launcher);

                builder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                        String currentItem = selectedItems.get(which);
                    }
                });
                builder.setCancelable(false);

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tasksLayout.removeAllViews();
                        boolean allSelected = false;
                        boolean doneOnce = false;
                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) {
                                //Statistics components
                                if(i == 0) {
                                    allSelected = true;
                                }
                                if (allSelected){
                                    if (!doneOnce){
                                        for(String task : listItems){
                                            if(!task.equals("All")) {
                                                StatisticsComponent sc = new StatisticsComponent(statisticsContext, task, false, null);
                                                tasksLayout.addView(sc);
                                                loadSubtasks(statisticsContext.getFilesDir(), task);
                                                for (String subTask : subTasks){
                                                    StatisticsComponent ssc = new StatisticsComponent(statisticsContext, subTask, true, task);
                                                    tasksLayout.addView(ssc);
                                                }
                                            }
                                        }
                                        doneOnce = true;
                                    }
                                } else {
                                    StatisticsComponent sc = new StatisticsComponent(statisticsContext, listItems[i], false, null);
                                    tasksLayout.addView(sc);
                                    loadSubtasks(statisticsContext.getFilesDir(), listItems[i]);
                                    for (String subTask : subTasks){
                                        StatisticsComponent ssc = new StatisticsComponent(statisticsContext, subTask, true, listItems[i]);
                                        tasksLayout.addView(ssc);
                                    }
                                }
                            }
                        }
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setNeutralButton("CLEAR ALL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < checkedItems.length; i++) {
                            checkedItems[i] = false;
                        }
                    }
                });

                builder.create();
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

    }

    private void loadSubtasks(File dir, String taskName){
        subTasks.clear();
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(".*" + taskName + "Subtazk.txt")){
                    String taskToAdd = file.getName();
                    String newString = taskToAdd.replace(taskName + "Subtazk", "");
                    subTasks.add(newString);
                }
            }
        }
    }

    private void findTasks(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if ((fileName.matches(".*Task.txt")) || (fileName.matches(".*FutureTakk.txt"))){
                    String taskToAdd = file.getName();
                    String newString = taskToAdd.replace("Task", "");
                    String otherNewString = newString.replace("FutureTakk", "");
                    tasks.add(otherNewString);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadAllAsDefault(){
        for(String task : tasks){
            if(!task.equals("All")) {
                StatisticsComponent sc = new StatisticsComponent(statisticsContext, task, false, null);
                tasksLayout.addView(sc);
                loadSubtasks(statisticsContext.getFilesDir(), task);
            }
            for (String subTask : subTasks){
                StatisticsComponent ssc = new StatisticsComponent(statisticsContext, subTask, true, task);
                tasksLayout.addView(ssc);
            }
        }
    }
}