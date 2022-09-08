package com.example.finalversion.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.finalversion.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SubtaskComponent extends ConstraintLayout {
    private Context homeSubtaskbarContext;
    private ProgressBar taskProgressBar;
    private ImageButton decreaseButton;
    private ImageButton increaseButton;
    private TextView nameAndIncrementTextView;

    private ConstraintLayout bigConstraintLayout;

    String taskText;
    String taskColor;
    String taskIncrement;
    int currentProgressPercentage;
    boolean isHabit = false;
    String parentName;
    TaskComponent papa;

    public SubtaskComponent(Context context) {
        super(context);
        init(context);
    }

    public SubtaskComponent(Context context, String name, String color, String increment, boolean habit, String parent, TaskComponent tc) {
        super(context);
        homeSubtaskbarContext = context;
        taskText = name;
        taskColor = color;
        taskIncrement = increment;
        isHabit = habit;
        parentName = parent;
        papa = tc;
        initWithParameters(context);
    }

    private void init(Context cont) {
        LayoutInflater inflater = (LayoutInflater) cont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.task_component, this);

        taskProgressBar = findViewById(R.id.taskProgressBar);
        decreaseButton = findViewById(R.id.minusButton);
        increaseButton = findViewById(R.id.plusButton);
        nameAndIncrementTextView = findViewById(R.id.taskNameTextView);
        bigConstraintLayout = findViewById(R.id.task_container);
    }

    private void initWithParameters(Context cont) {
        LayoutInflater inflater = (LayoutInflater) cont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.task_component, this);

        taskProgressBar = findViewById(R.id.taskProgressBar);
        decreaseButton = findViewById(R.id.minusButton);
        increaseButton = findViewById(R.id.plusButton);
        nameAndIncrementTextView = findViewById(R.id.taskNameTextView);
        bigConstraintLayout = findViewById(R.id.task_container);

        setText();
        setColor();
        setProgressBar();

        increaseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentProgressPercentage < 100){
                    incrementTask();
                    updateTaskFile(homeSubtaskbarContext.getFilesDir());
                    if (!(currentProgressPercentage < 100)) {
                        if (isHabit){
                            lastCompleted(homeSubtaskbarContext.getFilesDir());
                            bigConstraintLayout.removeAllViews();
                            resetTask();
                            currentProgressPercentage = 0;
                            updateTaskFile(homeSubtaskbarContext.getFilesDir());
                            checkHabitMainTask(homeSubtaskbarContext.getFilesDir());
                        } else {
                            checkNonHabitMainTask(homeSubtaskbarContext.getFilesDir());
                            deleteTask(homeSubtaskbarContext.getFilesDir());
                            bigConstraintLayout.removeAllViews();
                        }
                    }
                }
            }
        });

        decreaseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentProgressPercentage > 0){
                    decrementTask();
                    updateTaskFile(homeSubtaskbarContext.getFilesDir());
                }
            }
        });

        nameAndIncrementTextView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder delete = new AlertDialog.Builder(cont);
                delete.setTitle("Delete Subtask");
                delete.setMessage("Are you sure you want to delete this task?");
                delete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTask(homeSubtaskbarContext.getFilesDir());
                        bigConstraintLayout.removeAllViews();
                    }
                });
                delete.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                delete.create();
                AlertDialog alertDialog = delete.create();
                alertDialog.show();
                return false;
            }
        });

    }

    private void setText(){
        String displayName = taskText + " (" + taskIncrement + ")";
        nameAndIncrementTextView.setText(displayName);
    }

    public void setProgressBar(){
        currentProgressPercentage = calculateProgress(taskIncrement);
        taskProgressBar.setProgress(currentProgressPercentage);
    }

    private void setColor(){
        String[] splitter = taskColor.split("#");
        String background = "#30" + splitter[1];
        taskProgressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(taskColor)));
        taskProgressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor(background)));
    }

    private int calculateProgress(String increments){
        String[] splitter = increments.split("/");
        String part1 = splitter[0];
        String part2 = splitter[1];
        float number1 = Float.parseFloat(part1);
        float number2 = Float.parseFloat(part2);
        float percentage = ((number1 / number2) * 100);
        int returnValue = Math.round(percentage);
        return returnValue;
    }

    private void incrementTask(){
        String[] splitter = taskIncrement.split("/");
        String part1 = splitter[0];
        String part2 = splitter[1];
        int number1 = Integer.parseInt(part1);
        int incrementedNumber1 = number1 + 1;
        String incrementedPart1 = String.valueOf(incrementedNumber1);
        taskIncrement = incrementedPart1 + "/" + part2;
        setText();
        setProgressBar();
    }

    private void decrementTask(){
        String[] splitter = taskIncrement.split("/");
        String part1 = splitter[0];
        String part2 = splitter[1];
        int number1 = Integer.parseInt(part1);
        int incrementedNumber1 = number1 - 1;
        String incrementedPart1 = String.valueOf(incrementedNumber1);
        taskIncrement = incrementedPart1 + "/" + part2;
        setText();
        setProgressBar();
    }

    private void resetTask(){
        String[] splitter = taskIncrement.split("/");
        String part1 = splitter[0];
        String part2 = splitter[1];
        int number1 = Integer.parseInt(part1);
        int incrementedNumber1 = 0;
        String incrementedPart1 = String.valueOf(incrementedNumber1);
        taskIncrement = incrementedPart1 + "/" + part2;
        setText();
        setProgressBar();
    }

    private void updateTaskFile(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText + parentName + "Subtazk.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();

                        StringBuilder sb = new StringBuilder();
                        //read each line
                        while (line != null) {
                            if (line.matches("Number Of Increments =.*")){
                                sb.append("Number Of Increments =" + taskIncrement + "\n");
                                //taskIncrement has correct value
                            } else {
                                sb.append(line + "\n");
                            }
                            line = br.readLine();
                        }
                        br.close();
                        String output = sb.toString();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(output.getBytes(StandardCharsets.UTF_8));
                        fos.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    private void deleteTask(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText + parentName + "Subtazk.txt")){
                    boolean isItDeleted = file.delete();
                }
            }
        }
    }

    private void lastCompleted(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText + parentName + "Subtazk.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();

                        StringBuilder sb = new StringBuilder();
                        //read each line
                        while (line != null) {
                            if (line.matches("Last Completed =.*")){
                                SimpleDateFormat taskSimpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                Date taskDate = new Date();
                                String currentDate = taskSimpleDateFormat.format(taskDate);
                                sb.append("Last Completed =" + currentDate + "\n");
                            } else {
                                sb.append(line + "\n");
                            }
                            line = br.readLine();
                        }
                        br.close();
                        String output = sb.toString();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(output.getBytes(StandardCharsets.UTF_8));
                        fos.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    private void checkHabitMainTask(File dir){
        File[] files = dir.listFiles();
        boolean subtasksExist = false;
        boolean subtasksCompleted = true;
        //StringBuilder foundFiles = new StringBuilder();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(".*" + parentName + "Subtazk.txt")) {
                    //foundFiles.append(fileName);
                    subtasksExist = true;
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        String fileLastCompleted = null;
                        ArrayList<String> habitDays = new ArrayList<String>();
                        //read each line
                        while (line != null) {
                            if (line.matches("Last Completed =.*")){
                                String[] value = line.split("=");
                                if (value.length > 1){
                                    fileLastCompleted = value[1];
                                }
                            } else if (line.matches("Habit Days =.*")) {
                                String[] value = line.split("=");
                                String[] days = value[1].split(", ");
                                for (String day : days){
                                    habitDays.add(day);
                                }
                            }
                            line = br.readLine();
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
                        Date d = new Date();
                        String dayOfTheWeek = sdf.format(d);
                        for(String day : habitDays) {
                            if (dayOfTheWeek.equals(day)) {
                                SimpleDateFormat lastCompleted = new SimpleDateFormat("dd-MM-yyyy");
                                Date date = new Date();
                                String theDateThatIsToday = lastCompleted.format(date);
                                if (!theDateThatIsToday.equals(fileLastCompleted)){
                                    subtasksCompleted = false;
                                }
                            }
                        }

                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
        if (subtasksExist){
            if (subtasksCompleted) {
                papa.subtasksComplete();
                System.out.println("alma");
            }
        }
    }

    private void checkNonHabitMainTask(File dir){
        File[] files = dir.listFiles();
        int nrOfSubtasks = 0;
        //StringBuilder foundFiles = new StringBuilder();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(".*" + parentName + "Subtazk.txt")) {
                    //foundFiles.append(fileName);
                    nrOfSubtasks++;
                }
            }
        }
        if (nrOfSubtasks == 1){
            papa.subtasksComplete();
            System.out.println("korte");
        }
    }
}