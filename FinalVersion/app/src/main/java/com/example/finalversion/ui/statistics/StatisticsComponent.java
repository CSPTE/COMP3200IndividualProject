package com.example.finalversion.ui.statistics;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.finalversion.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class StatisticsComponent extends ConstraintLayout {
    private boolean isSubtask;
    private String parentName;

    private LinearLayout smallBoy;
    private TextView nameTextView;
    private TextView typeTextView;
    private TextView colorTextView;
    private TextView incrementTextView;

    //From task files
    private String name;
    private String type;
    private boolean isItHabitual = false;
    private String color;
    private String increment;
    private String days = null;
    private ArrayList<String> daysArray = new ArrayList<String>();
    private String dueOn;

    //From subtask files
    private ArrayList<String> subTasks = new ArrayList<String>();

    //From log files
    private ArrayList<String> taskDates = new ArrayList<String>();
    private ArrayList<String> idealDates = new ArrayList<String>();
    private int streakInt = 0;
    private int percentage;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public StatisticsComponent(Context context, String taskName, boolean subtask, String parent) {
        super(context);
        isSubtask = subtask;
        parentName = parent;
        init(context, taskName);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void init(Context cont, String taskName) {
        LayoutInflater inflater = (LayoutInflater) cont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.statistics_component, this);
        smallBoy = findViewById(R.id.smallerBoy);
        nameTextView = findViewById(R.id.taskNameStatistics);
        typeTextView = findViewById(R.id.taskTypeStatistics);
        colorTextView = findViewById(R.id.taskColorStatistics);
        incrementTextView = findViewById(R.id.taskIncrementStatistics);

        //Load values
        name = taskName; //Name
        if (!isSubtask){
            getTaskType(cont.getFilesDir(), taskName);
        } else {
            getSubtaskType(cont.getFilesDir());
        }

        if(type.equals("Habitual Task")){
            getTaskData(cont.getFilesDir(), taskName); //Increment, Color, Days
            getSubtasks(cont.getFilesDir(), taskName); //Subtasks
            daysToDaysArray(); //Days To Array
            getLogFile(cont.getFilesDir(), taskName); //Dates From LogFile
            if (!taskDates.isEmpty()){
                getIdealDates(); //Perfect Performance
                calculateStreak(); //Streak
                calculatePercentage(); //Percentage
            } else {
                streakInt = 0;
            }
            setHabitualView(cont);
        } else if (type.equals("Non-Habitual Task")){
            getTaskData(cont.getFilesDir(), taskName); //Increment, Color, Days
            getSubtasks(cont.getFilesDir(), taskName); //Subtasks
            setNonHabitualView(cont);
        } else if (type.equals("Future Task")){
            getTaskData(cont.getFilesDir(), taskName); //Increment, Color, Days, Due
            getSubtasks(cont.getFilesDir(), taskName); //Subtasks
            setFutureView(cont);
        } else if (type.equals("Habitual Subtask")) {
            getTaskData(cont.getFilesDir(), taskName); //Increment, Color, Days
            getSubtaskColor(cont.getFilesDir()); //Subtask Color
            setHabitualSubtaskView(cont);
        } else if (type.equals("Non-Habitual Subtask")) {
            getTaskData(cont.getFilesDir(), taskName); //Increment, Color, Days
            getSubtaskColor(cont.getFilesDir()); //Subtask Color
            setNonHabitualSubtaskView(cont);
        } else if (type.equals("Future Subtask")) {
            getTaskData(cont.getFilesDir(), taskName); //Increment, Color, Days
            getSubtaskColor(cont.getFilesDir()); //Subtask Color
            getSubtaskDueOn(cont.getFilesDir()); //Subtask Due
            setFutureSubtaskView(cont);
        }
    }

    private void setHabitualView(Context cont){
        //Default
        smallBoy.setBackgroundResource(R.drawable.border);
        nameTextView.setText(name);
        typeTextView.setText(type);
        incrementTextView.setText(increment);
        colorTextView.setText(color);

        //Days
        StatisticsLineComponent daysLine = new StatisticsLineComponent(cont, "Days:");
        daysLine.setValue(days);
        smallBoy.addView(daysLine);

        //Subtasks
        StatisticsLineComponent subtasksLine = new StatisticsLineComponent(cont, "Subtasks:");
        setSubtasksTextView(subtasksLine);
        smallBoy.addView(subtasksLine);

        //Streak
        StatisticsLineComponent streakLine = new StatisticsLineComponent(cont, "Streak:");
        streakLine.setValue(Integer.toString(streakInt));
        smallBoy.addView(streakLine);

        //Percentage
        StatisticsLineComponent percentageLine = new StatisticsLineComponent(cont, "Completion Rate:");
        percentageLine.setValue(percentage + "%");
        smallBoy.addView(percentageLine);
    }

    private void setNonHabitualView(Context cont){
        //Default
        smallBoy.setBackgroundResource(R.drawable.border);
        nameTextView.setText(name);
        typeTextView.setText(type);
        incrementTextView.setText(increment);
        colorTextView.setText(color);

        //Subtasks
        StatisticsLineComponent subtasksLine = new StatisticsLineComponent(cont, "Subtasks:");
        setSubtasksTextView(subtasksLine);
        smallBoy.addView(subtasksLine);
    }

    private void setHabitualSubtaskView(Context cont){
        //Default
        smallBoy.setPadding(75,0,0,0);
        smallBoy.setBackgroundResource(R.drawable.border_statistics_subtask);
        nameTextView.setText(name);
        typeTextView.setText(type);
        incrementTextView.setText(increment);
        colorTextView.setText(color);

        //Days
        StatisticsLineComponent daysLine = new StatisticsLineComponent(cont, "Days:");
        daysLine.setValue(days);
        smallBoy.addView(daysLine);
    }

    private void setNonHabitualSubtaskView(Context cont){
        //Default
        smallBoy.setPadding(100,0,0,0);
        smallBoy.setBackgroundResource(R.drawable.border_statistics_subtask);
        nameTextView.setText(name);
        typeTextView.setText(type);
        incrementTextView.setText(increment);
        colorTextView.setText(color);
    }

    private void setFutureView(Context cont){
        //Default
        smallBoy.setBackgroundResource(R.drawable.border);
        nameTextView.setText(name);
        typeTextView.setText(type);
        incrementTextView.setText(increment);
        colorTextView.setText(color);

        //Subtasks
        StatisticsLineComponent subtasksLine = new StatisticsLineComponent(cont, "Subtasks:");
        setSubtasksTextView(subtasksLine);
        smallBoy.addView(subtasksLine);

        //Due Date
        StatisticsLineComponent dueLine = new StatisticsLineComponent(cont, "Due On:");
        dueLine.setValue(dueOn);
        smallBoy.addView(dueLine);
    }

    private void setFutureSubtaskView(Context cont){
        //Default
        smallBoy.setPadding(100,0,0,0);
        smallBoy.setBackgroundResource(R.drawable.border_statistics_subtask);
        nameTextView.setText(name);
        typeTextView.setText(type);
        incrementTextView.setText(increment);
        colorTextView.setText(color);

        //Due Date
        StatisticsLineComponent dueLine = new StatisticsLineComponent(cont, "Due On:");
        dueLine.setValue(dueOn);
        smallBoy.addView(dueLine);
    }

    private void getSubtaskDueOn(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(parentName +"FutureTakk.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();

                        //read each line
                        while (line != null) {
                            if (line.matches("Date Of Task=.*")){
                                String[] value = line.split("=");
                                dueOn = value[1];
                            }
                            line = br.readLine();
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    private void getSubtaskColor(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(parentName +"Task.txt") || (fileName.matches(parentName +"FutureTakk.txt"))){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();

                        //read each line
                        while (line != null) {
                            if (line.matches("Color Selected =.*")){
                                String[] value = line.split("=");
                                String tempColor = "#" + value[1];
                                color = turnHexToString(tempColor);
                            }
                            line = br.readLine();
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    private void calculatePercentage(){
        float divideBy = idealDates.size();
        float divide = taskDates.size();
        float result = (divide / divideBy) * 100;
        percentage = Math.round(result);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void calculateStreak(){
        for (String idealLocalDate : idealDates){
            String[] splitter = idealLocalDate.split("-");
            String idealDate = splitter[2] + "-" + splitter[1] + "-" + splitter[0];
            if (taskDates.contains(idealDate)){
                streakInt++;
            } else {
                streakInt = 0;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getIdealDates(){
        //Strings to LocalDates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String firstDateString = taskDates.get(0);
        LocalDate firstDate = LocalDate.parse(firstDateString, formatter);

        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date currentDateTemp = new Date();
        String currentDateString = currentDateFormat.format(currentDateTemp);
        LocalDate currentDate = LocalDate.parse(currentDateString, formatter);

        //String currentDateString = "15-12-2022";
        //LocalDate currentDate = LocalDate.parse(currentDateString, formatter);

        for (LocalDate date = firstDate; date.isBefore(currentDate.plusDays(1)); date = date.plusDays(1)) {
            DayOfWeek dayNameUpperCase = date.getDayOfWeek();
            String dayNameLowerCase = dayNameUpperCase.toString().toLowerCase();
            String dayName = dayNameLowerCase.substring(0, 1).toUpperCase() + dayNameLowerCase.substring(1);
            if (daysArray.contains(dayName)){
                idealDates.add(date.toString());
            }
        }
    }

    private void getLogFile(File dir, String taskName){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskName + "Log.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (!line.matches("FullLog")){
                                taskDates.add(line);
                            }
                            line = br.readLine();
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    private void daysToDaysArray(){
        String[] value = days.split(", ");
        daysArray.addAll(Arrays.asList(value));
    }

    private void setSubtasksTextView(StatisticsLineComponent slc){
        String submit = "";
        boolean firstItem = true;
        if (subTasks.isEmpty()){
            submit = "None";
        } else {
            for(String item : subTasks){
                if (firstItem){
                    submit = item;
                    firstItem = false;
                } else {
                    submit = submit + ", " + item;
                }
            }
        }
        slc.setValue(submit);
    }

    private void getSubtasks(File dir, String taskName){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches( ".*" + taskName + "Subtazk.txt")){
                    String toAdd = file.getName();
                    String removeSubtazk = toAdd.replace("Subtazk", "");
                    String removeParent = removeSubtazk.replace(taskName, "");
                    subTasks.add(removeParent);
                }
            }
        }
    }

    private void getTaskData(File dir, String taskName){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if ((fileName.matches(taskName +"Task.txt")) || (fileName.matches(taskName +"FutureTakk.txt")) || (fileName.matches(taskName + parentName +"Subtazk.txt"))){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();

                        //read each line
                        while (line != null) {
                            if (line.matches("Number Of Increments =.*")){
                                String[] value = line.split("=");
                                String[] total = value[1].split("/");
                                increment = total[1];
                            } else if (line.matches("Color Selected =.*")){
                                String[] value = line.split("=");
                                String tempColor = "#" + value[1];
                                color = turnHexToString(tempColor);
                            } else if (line.matches("Habit Toggled =.*")){
                                String[] value = line.split("=");
                                isItHabitual = Boolean.parseBoolean(value[1]);
                            } else if (line.matches("Habit Days =.*")){
                                if (isItHabitual || parentName != null){
                                    String[] value = line.split("=");
                                    days = value[1];
                                } else {
                                    days = "Non-Habitual";
                                }
                            } else if (line.matches("Date Of Task=.*")){
                                String[] value = line.split("=");
                                dueOn = value[1];
                            }
                            line = br.readLine();
                        }
                        br.close();
                        if (days == null){
                            days = "Non-Habitual";
                        }
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    private void getSubtaskType (File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(parentName +"Task.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();

                        //read each line
                        while (line != null) {
                            if (line.matches("Habit Toggled =.*")){
                                String[] value = line.split("=");
                                isItHabitual = Boolean.parseBoolean(value[1]);
                            }
                            line = br.readLine();
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                    if (isItHabitual){
                        type = "Habitual Subtask";
                    } else {
                        type = "Non-Habitual Subtask";
                    }
                } else if(fileName.matches(parentName +"FutureTakk.txt")){
                    type = "Future Subtask";
                }
            }
        }
    }

    private void getTaskType(File dir, String taskName){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskName +"Task.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();

                        //read each line
                        while (line != null) {
                            if (line.matches("Habit Toggled =.*")){
                                String[] value = line.split("=");
                                isItHabitual = Boolean.parseBoolean(value[1]);
                            }
                            line = br.readLine();
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                    if (isItHabitual){
                        type = "Habitual Task";
                    } else {
                        type = "Non-Habitual Task";
                    }
                } else if(fileName.matches(taskName +"FutureTakk.txt")){
                    type = "Future Task";
                }
            }
        }
    }

    private String turnHexToString(String hex){
        String colorString = null;
        if (hex.equals("#C0C0C0")){
            colorString = "Silver";
        } else if (hex.equals("#FE828C")){
            colorString = "Blush Pink";
        } else if (hex.equals("#FF6961")){
            colorString = "Pastel Red";
        } else if (hex.equals("#FF00FF")){
            colorString = "Fuchsia";
        } else if (hex.equals("#E0B0FF")){
            colorString = "Mauve";
        } else if (hex.equals("#6495ED")){
            colorString = "Cornflower Blue";
        } else if (hex.equals("#007fff")){
            colorString = "Azure";
        } else if (hex.equals("#00FFFF")){
            colorString = "Aqua";
        } else if (hex.equals("#7FFFD4")){
            colorString = "Aquamarine";
        } else if (hex.equals("#00FF00")){
            colorString = "Lime";
        } else if (hex.equals("#50C878")){
            colorString = "Emerald Green";
        } else if (hex.equals("#BAB86C")){
            colorString = "Olive Green";
        } else if (hex.equals("#F5DEB3")){
            colorString = "Wheat";
        } else if (hex.equals("#FFFF00")){
            colorString = "Yellow";
        } else if (hex.equals("#FFA500")){
            colorString = "Orange";
        }
        return colorString;
    }
}
