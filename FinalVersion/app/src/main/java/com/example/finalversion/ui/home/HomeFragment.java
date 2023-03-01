package com.example.finalversion.ui.home;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.finalversion.R;
import com.example.finalversion.databinding.FragmentHomeBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Context homeContext;

    private ArrayList<TaskComponent> tasks = new ArrayList<TaskComponent>();
    private LinearLayout tasksLayout;
    private boolean taskExists = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        homeContext = this.getContext();

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //.findNavController(HomeFragment.this)
                //        .navigate(R.id.action_nav_home_to_blankTaskSettingsFragment);
                boolean edit = false;
                //To whoever is reading this: this is the longest latin word with an additional "z" at the end. It means: the action of estimating something as worthless. Hope no one uses this as a task name lmao
                String previousName = "honorificabilitudinitatibusz";
                HomeFragmentDirections.ActionNavHomeToBlankTaskSettingsFragment action = HomeFragmentDirections.actionNavHomeToBlankTaskSettingsFragment(edit, previousName);
                NavHostFragment.findNavController(HomeFragment.this).navigate(action);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tasksLayout = view.findViewById(R.id.tasks);

/*
        Button checkFilesButton = view.findViewById(R.id.checkFilesButton);
        Button deleteFilesButton = view.findViewById(R.id.deleteFilesButton);
        TextView filesDirFilesTextView = view.findViewById(R.id.checkFilesTextView);

        checkFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayFiles(homeContext.getFilesDir(), filesDirFilesTextView);
                //addTasks(homeContext.getFilesDir(), filesDirFilesTextView);
            }
        });

        deleteFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFiles(homeContext.getFilesDir(), filesDirFilesTextView);
            }
        });
*/

        addTasks(homeContext.getFilesDir());

        if (!taskExists){
            //Welcome Tutorial
                AlertDialog.Builder warning = new AlertDialog.Builder(homeContext);
                warning.setIcon(R.drawable.ic_notification_icon);
                warning.setTitle("Welcome to Rise");
                String message = "Press the <b>'+'</b> button at the <b>bottom right</b> to add a new task <br> <br> Press the <b>three lines</b> on the <b>top left</b> to open the navigation menu <br><br>On the <i><b>'Calendar'</b></i> page you can track your progress and schedule future tasks <br>On the <i><b>'Leaderboard'</i></b> page you can compete with other users, and view their profiles <br>On the <i><b>'Statistics'</i></b> page, you can view a detailed breakdown of your tasks <br>While on the <i><b>'Profile'</i></b> page, you can set which tasks will be publicly viewable to other users from the leaderboard <br> <br> <b>Thank you for downloading the app, and good luck on your journey </b> <br> <br><b><i>We are what we repeatedly do. Excellence, then, is not an act, but a habit.</b></i> <br> - <b>Will Durant</b>";
                warning.setMessage(Html.fromHtml(message));
                warning.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                warning.create();
                AlertDialog alertDialog = warning.create();
                alertDialog.show();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        for (TaskComponent element : tasks) {
            element.setProgressBar();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void displayFiles (File dir, TextView tv) {
        File[] files = dir.listFiles();
        StringBuilder fileNames = new StringBuilder();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                fileNames.append("File Name:" + file.getName() + ".txt" + "\n");
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line = br.readLine();
                    //read each line
                    while (line != null) {
                        fileNames.append(line + "\n");
                        line = br.readLine();
                    }
                    br.close();
                } catch (IOException e) {
                    Log.d("Exception",e.toString());
                }
                fileNames.append("\n");
            }
        }
        tv.setText(fileNames);
    }

    private void deleteFiles (File dir, TextView tv) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                boolean isItDeleted = file.delete();
            }
        }
        displayFiles(dir, tv);
    }

    private void addTasks(File dir){
        File[] files = dir.listFiles();
        //StringBuilder foundFiles = new StringBuilder();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if ((fileName.matches(".*Task.txt")) || (fileName.matches(".*FutureTakk.txt"))){
                    //foundFiles.append(fileName);
                    taskExists = true;
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        String taskName = null;
                        String taskIncrement = null;
                        String taskColor = null;
                        boolean habitToggle = false;
                        ArrayList<String> habitDays = new ArrayList<String>();
                        String fileLastCompleted = null;
                        String dateOfFutureTask = null;
                        //read each line
                        while (line != null) {
                            if (line.matches("Task name =.*")){
                                String[] value = line.split("=");
                                taskName = value[1];
                            } else if (line.matches("Number Of Increments =.*")){
                                String[] value = line.split("=");
                                taskIncrement = value[1];
                            } else if (line.matches("Color Selected =.*")){
                                String[] value = line.split("=");
                                taskColor = "#" + value[1];
                            } else if (line.matches("Habit Toggled =.*")){
                                String[] value = line.split("=");
                                habitToggle = Boolean.parseBoolean(value[1]);
                            } else if (line.matches("Habit Days =.*")){
                                if (habitToggle){
                                    String[] value = line.split("=");
                                    String[] days = value[1].split(", ");
                                    for (String day : days){
                                        habitDays.add(day);
                                    }
                                }
                            } else if (line.matches("Last Completed =.*")){
                                String[] value = line.split("=");
                                if (value.length > 1){
                                    fileLastCompleted = value[1];
                                }
                            } else if (line.matches("Date Of Task=.*")){
                                String[] value = line.split("=");
                                dateOfFutureTask = value[1];
                            }
                            line = br.readLine();
                        }
                        if (habitToggle){
                            SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
                            Date d = new Date();
                            String dayOfTheWeek = sdf.format(d);
                            for(String day : habitDays){
                                if (dayOfTheWeek.equals(day)){
                                    /*
                                    SimpleDateFormat lastCompleted = new SimpleDateFormat("dd/MM/yyyy");
                                    Date date = new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24));
                                    String theDateThatIsToday = lastCompleted.format(date);
                                     */
                                    SimpleDateFormat lastCompleted = new SimpleDateFormat("dd-MM-yyyy");
                                    Date date = new Date();
                                    String theDateThatIsToday = lastCompleted.format(date);
                                    if (!theDateThatIsToday.equals(fileLastCompleted)){
                                        TaskComponent newTask = new TaskComponent(homeContext, taskName, taskColor, taskIncrement, this, false);
                                        tasksLayout.addView(newTask);
                                        tasks.add(newTask);


                                        newTask.getButton().setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                String sendText = newTask.getTaskName();
                                                boolean edit = false;
                                                String previousName = "honorificabilitudinitatibusz";
                                                HomeFragmentDirections.ActionNavHomeToHabitSubtaskSettingsFragment action = HomeFragmentDirections.actionNavHomeToHabitSubtaskSettingsFragment(sendText, edit, previousName);
                                                NavHostFragment.findNavController(HomeFragment.this).navigate(action);
                                                /*
                                                NavHostFragment.findNavController(HomeFragment.this)
                                                        .navigate(R.id.action_nav_home_to_subtaskSettingsFragment);
                                                 */
                                            }
                                        });
                                    }
                                }
                            }
                        } else {
                            if (dateOfFutureTask != null){
                                SimpleDateFormat day = new SimpleDateFormat("dd-M-yyyy");
                                Date date = new Date();
                                String currentDate = day.format(date);

                                SimpleDateFormat day2 = new SimpleDateFormat("dd");
                                Date date2 = new Date();
                                String currentDay = day2.format(date2);
                                SimpleDateFormat month = new SimpleDateFormat("M");
                                Date date3 = new Date();
                                String currentMonth = month.format(date3);
                                SimpleDateFormat year = new SimpleDateFormat("yyyy");
                                Date date4 = new Date();
                                String currentYear = year.format(date4);
                                String[] splitter = dateOfFutureTask.split("-");

                                if(currentDate.equals(dateOfFutureTask)){
                                    TaskComponent newTask = new TaskComponent(homeContext, taskName, taskColor, taskIncrement, this, true);
                                    tasksLayout.addView(newTask);
                                    tasks.add(newTask);
                                    newTask.getButton().setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String sendText = newTask.getTaskName();
                                            boolean edit = false;
                                            String previousName = "honorificabilitudinitatibusz";
                                            HomeFragmentDirections.ActionNavHomeToSubtaskSettingsFragment action = HomeFragmentDirections.actionNavHomeToSubtaskSettingsFragment(sendText, edit, previousName);
                                            NavHostFragment.findNavController(HomeFragment.this).navigate(action);
                                        }
                                    });
                                } if((Integer.parseInt(splitter[2]) < Integer.parseInt(currentYear)) ||
                                        ((Integer.parseInt(splitter[2]) == Integer.parseInt(currentYear)) && (Integer.parseInt(splitter[1]) < Integer.parseInt(currentMonth))) ||
                                        ((Integer.parseInt(splitter[2]) == Integer.parseInt(currentYear)) && (Integer.parseInt(splitter[1]) == Integer.parseInt(currentMonth)) && (Integer.parseInt(splitter[0]) < Integer.parseInt(currentDay)))) {
                                    deleteFutureTask(homeContext.getFilesDir(), taskName);
                                }
                            } else {
                                TaskComponent newTask = new TaskComponent(homeContext, taskName, taskColor, taskIncrement, this, false);
                                tasksLayout.addView(newTask);
                                tasks.add(newTask);
                                newTask.getButton().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String sendText = newTask.getTaskName();
                                        boolean edit = false;
                                        String previousName = "honorificabilitudinitatibusz";
                                        HomeFragmentDirections.ActionNavHomeToSubtaskSettingsFragment action = HomeFragmentDirections.actionNavHomeToSubtaskSettingsFragment(sendText, edit, previousName);
                                        NavHostFragment.findNavController(HomeFragment.this).navigate(action);
                                        //NavHostFragment.findNavController(HomeFragment.this)
                                        //        .navigate(R.id.action_nav_home_to_subtaskSettingsFragment);
                                    }
                                });
                            }
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
        //tv.setText(foundFiles);
    }

    private void deleteFutureTask(File dir, String taskText){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText +"FutureTakk.txt")) {
                    boolean isItDeleted = file.delete();
                }
                if (fileName.matches(".*" + taskText +"Subtazk.txt")){
                    boolean isItDeleted = file.delete();
                }
            }
        }
    }
}