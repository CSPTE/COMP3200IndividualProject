package com.example.finalversion.ui.home;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.fragment.NavHostFragment;

import com.example.finalversion.R;
import com.example.finalversion.notification.Receiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class TaskComponent extends ConstraintLayout {
    private Context homeTaskbarContext;
    private ProgressBar taskProgressBar;
    private ImageButton decreaseButton;
    private ImageButton increaseButton;
    private TextView nameAndIncrementTextView;
    private LinearLayout subtasksLinearLayout;
    private ConstraintLayout bigConstraintLayout;

    private Button addNewSubtask;
    //private SubtaskComponent stc;
    private ArrayList<SubtaskComponent> subTasks = new ArrayList<SubtaskComponent>();

    private AlarmManager am;
    ArrayList<String> daysToAppear = new ArrayList<String>();

    String taskText;
    String taskColor;
    String taskIncrement;
    int currentProgressPercentage;
    boolean subtasksOpened = false;
    boolean isHabit = false;
    int notificationHour;
    int notificationMinute;
    boolean notificationToggle = false;

    private HomeFragment hf;
    private boolean isFuture;

    public TaskComponent(Context context){
        super(context);
        init(context);
    }
    public TaskComponent(Context context, String name, String color, String increment, HomeFragment frag, boolean ff) {
        super(context);
        homeTaskbarContext = context;
        taskText = name;
        taskColor = color;
        taskIncrement = increment;
        hf = frag;
        isFuture = ff;
        initWithParameters(context);
    }

    private void init(Context cont) {
        LayoutInflater inflater = (LayoutInflater) cont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.task_component, this);

        taskProgressBar = findViewById(R.id.taskProgressBar);
        decreaseButton = findViewById(R.id.minusButton);
        increaseButton = findViewById(R.id.plusButton);
        nameAndIncrementTextView = findViewById(R.id.taskNameTextView);
        subtasksLinearLayout = findViewById(R.id.subtaskLinearLayout);
        bigConstraintLayout = findViewById(R.id.task_container);
    }

    private void initWithParameters(Context cont) {
        LayoutInflater inflater = (LayoutInflater) cont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.task_component, this);

        taskProgressBar = findViewById(R.id.taskProgressBar);
        decreaseButton = findViewById(R.id.minusButton);
        increaseButton = findViewById(R.id.plusButton);
        nameAndIncrementTextView = findViewById(R.id.taskNameTextView);
        subtasksLinearLayout = findViewById(R.id.subtaskLinearLayout);
        bigConstraintLayout = findViewById(R.id.task_container);

        setText();
        setColor();
        setProgressBar();
        isAHabitualTask(homeTaskbarContext.getFilesDir());
        initialiseButton();
        //initialiseTestSubtask();
        initialiseSubtasks(homeTaskbarContext.getFilesDir());

        /*
        getNotificationTime(homeTaskbarContext.getFilesDir());
        if(notificationToggle){
            applyNotification(taskText, createID(), notificationHour, notificationMinute);
        }
        */


        increaseButton.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(currentProgressPercentage < 100){
                    incrementTask();
                    updateTaskFile(homeTaskbarContext.getFilesDir());
                    //applyNotification(taskText, createID(), notificationHour, notificationMinute);
                    if (!(currentProgressPercentage < 100)) {
                        if (isHabit){
                            lastCompleted(homeTaskbarContext.getFilesDir());
                            bigConstraintLayout.removeAllViews();
                            resetTask();
                            currentProgressPercentage = 0;
                            updateTaskFile(homeTaskbarContext.getFilesDir());
                            updateLogFile(homeTaskbarContext.getFilesDir());
                            updatePointcardFile();
                        } else {
                            updatePointcardFile();
                            deleteTask(homeTaskbarContext.getFilesDir());
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
                    updateTaskFile(homeTaskbarContext.getFilesDir());
                }
            }
        });

        nameAndIncrementTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!subtasksOpened) {
                    //subtasksLinearLayout.addView(stc);
                    for (SubtaskComponent sub : subTasks){
                        subtasksLinearLayout.addView(sub);
                    }
                    subtasksLinearLayout.addView(addNewSubtask);
                    subtasksOpened = true;
                } else {
                    subtasksLinearLayout.removeAllViews();
                    subtasksOpened = false;
                }
            }
        });

        nameAndIncrementTextView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder delete = new AlertDialog.Builder(cont);
                delete.setTitle("Delete Or Edit Task");
                delete.setMessage("Are you sure you want to delete this task? \nEditing the name of this task will result in all of its subtasks being lost.");
                delete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTask(homeTaskbarContext.getFilesDir());
                        deleteLogFile(homeTaskbarContext.getFilesDir());
                        bigConstraintLayout.removeAllViews();
                    }
                });
                delete.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                if (!isFuture) {
                    delete.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean edit = true;
                            String previousName = taskText;
                            HomeFragmentDirections.ActionNavHomeToBlankTaskSettingsFragment action = HomeFragmentDirections.actionNavHomeToBlankTaskSettingsFragment(edit, previousName);
                            NavHostFragment.findNavController(hf).navigate(action);
                        }
                    });
                }
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

    private void initialiseButton(){
        addNewSubtask = new Button(homeTaskbarContext);
        addNewSubtask.setHeight(55);
        addNewSubtask.setText("Add New Subtask");
        addNewSubtask.setTextSize(20);
        addNewSubtask.setTextColor(getContext().getResources().getColor(R.color.white));
        addNewSubtask.setBackgroundResource(R.drawable.add_new_subtask_button);
    }

    public Button getButton(){
        return addNewSubtask;
    }


    private void updateTaskFile(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if ((fileName.matches(taskText +"Task.txt")) || (fileName.matches(taskText +"FutureTakk.txt"))){
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
                if ((fileName.matches(taskText +"Task.txt")) || (fileName.matches(taskText +"FutureTakk.txt")) || (fileName.matches(taskText + "Log.txt")) || (fileName.matches(taskText + "Pointcard.txt"))){
                    boolean isItDeleted = file.delete();
                }
                if (fileName.matches(".*" + taskText +"Subtazk.txt")){
                    boolean isItDeleted = file.delete();
                }
            }
        }
    }

    private void isAHabitualTask(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText +"Task.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Habit Toggled =.*")){
                                String[] value = line.split("=");
                                isHabit = Boolean.parseBoolean(value[1]);
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

    private void lastCompleted(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText +"Task.txt")){
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

    private void initialiseTestSubtask() {
        SubtaskComponent stc;
        stc = new SubtaskComponent(homeTaskbarContext, "Cardio", "#FF6961", "0/5", isHabit, taskText, this, hf);
    }

    public String getTaskName(){
        return taskText;
    }

    private void initialiseSubtasks(File dir){
        File[] files = dir.listFiles();
        //StringBuilder foundFiles = new StringBuilder();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(".*" + taskText + "Subtazk.txt")) {
                    //foundFiles.append(fileName);
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        String subTaskName = null;
                        String subTaskIncrement = null;
                        String fileLastCompleted = null;
                        ArrayList<String> habitDays = new ArrayList<String>();
                        //read each line
                        while (line != null) {
                            if (line.matches("Task name =.*")) {
                                String[] value = line.split("=");
                                subTaskName = value[1];
                            } else if (line.matches("Number Of Increments =.*")){
                                String[] value = line.split("=");
                                subTaskIncrement = value[1];
                            } else if (line.matches("Last Completed =.*")){
                                String[] value = line.split("=");
                                if (value.length > 1){
                                    fileLastCompleted = value[1];
                                }
                            }
                            if (isHabit){
                                if (line.matches("Habit Days =.*")) {
                                    String[] value = line.split("=");
                                    String[] days = value[1].split(", ");
                                    for (String day : days){
                                        habitDays.add(day);
                                    }
                                }
                            }
                            line = br.readLine();
                        }
                        if (isHabit){
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
                                        SubtaskComponent newSubtask = new SubtaskComponent(homeTaskbarContext, subTaskName, taskColor, subTaskIncrement, isHabit, taskText, this, hf);
                                        subTasks.add(newSubtask);
                                    }
                                }
                            }
                        } else {
                            SubtaskComponent newSubtask = new SubtaskComponent(homeTaskbarContext, subTaskName, taskColor, subTaskIncrement, isHabit, taskText, this, hf);
                            subTasks.add(newSubtask);
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void subtasksComplete(){
        if(currentProgressPercentage < 100){
            incrementTask();
            updateTaskFile(homeTaskbarContext.getFilesDir());
            if (!(currentProgressPercentage < 100)) {
                if (isHabit){
                    lastCompleted(homeTaskbarContext.getFilesDir());
                    bigConstraintLayout.removeAllViews();
                    resetTask();
                    currentProgressPercentage = 0;
                    updateTaskFile(homeTaskbarContext.getFilesDir());
                    updateLogFile(homeTaskbarContext.getFilesDir());
                    updatePointcardFile();
                } else {
                    updatePointcardFile();
                    deleteTask(homeTaskbarContext.getFilesDir());
                    bigConstraintLayout.removeAllViews();
                }
            }
        }
    }

    private void applyNotification(String name, int id, int hour, int minute){
        String stringId = String.valueOf(id);

        long secondVariable = 1000;
        long minuteVariable = 60 * secondVariable;
        long hourVariable = 60 * minuteVariable;
        long dayVariable = 24 * hourVariable;

        //alarm1
        long timeOfActivation = System.currentTimeMillis();
        long tenSeconds = 10 * 1000;

        //alarm2
        Calendar cal = Calendar.getInstance();
        long currentMinute = cal.get(Calendar.MINUTE);
        long currentHour = cal.get(Calendar.HOUR_OF_DAY);
        long hourDifference = 0;
        long minuteDifference;
        if (hour > currentHour){
            hourDifference = hour - currentHour;
        } else if (hour == currentHour) {
            hourDifference = 0;
        } else {
            getHabitDays(homeTaskbarContext.getFilesDir());
            int i = 1;
            for(String day : daysToAppear) {
                SimpleDateFormat lastCompleted = new SimpleDateFormat("EEEE");
                Date date = new Date(System.currentTimeMillis() + (i * dayVariable));
                String tomorrow = lastCompleted.format(date);
                if (tomorrow.equals(day)) {
                    hourDifference = (i * 24L) - (currentHour - hour);
                    break;
                } else {
                    i++;
                }
            }
        }
        if (minute > currentMinute){
            minuteDifference = minute - currentMinute;
        } else {
            minuteDifference = currentMinute - minute;
        }
        long launchTime = System.currentTimeMillis() + (hourDifference * hourVariable) - (minuteDifference * minuteVariable);

        //setup
        Intent inten = new Intent(homeTaskbarContext, Receiver.class);
        inten.putExtra("Name", name);
        inten.putExtra("ID", stringId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(homeTaskbarContext, id, inten,
                PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_IMMUTABLE);
        am = (AlarmManager) homeTaskbarContext.getSystemService(Context.ALARM_SERVICE);
        //Works ->
        //am.set(AlarmManager.RTC_WAKEUP,timeOfActivation + tenSeconds,pendingIntent);
        //Doesn't work (delivers immediately) ->
        am.set(AlarmManager.RTC_WAKEUP, launchTime, pendingIntent);
        //Semi-Works (First notification missing) ->
        //am.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeOfActivation + tenSeconds, tenSeconds, pendingIntent);
        //Doesn't work ->
        //am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY*7, pendingIntent);
        System.out.println("alma");
    }

    private int createID(){
        //Date now = new Date();
        //int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        //return id;
        int oneTimeID = (int) SystemClock.uptimeMillis();
        return oneTimeID;
    }

    private void getNotificationTime(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText +"Task.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Notification Toggled =.*")){
                                String[] value = line.split("=");
                                notificationToggle = Boolean.parseBoolean(value[1]);
                            } else if (line.matches("Notification Time =.*")){
                                if (notificationToggle){
                                    String[] value = line.split("=");
                                    String[] times = value[1].split(":");
                                    notificationHour = Integer.parseInt(times[0]);
                                    notificationMinute = Integer.parseInt(times[1]);
                                }
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

    private void getHabitDays(File dir){
        File[] files = dir.listFiles();
        //StringBuilder foundFiles = new StringBuilder();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText +"Task.txt")){
                    //foundFiles.append(fileName);
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (isHabit){
                                if (line.matches("Habit Days =.*")) {
                                    String[] value = line.split("=");
                                    String[] days = value[1].split(", ");
                                    for (String day : days){
                                        daysToAppear.add(day);
                                    }
                                }
                            } else {
                                daysToAppear.add("Monday");
                                daysToAppear.add("Tuesday");
                                daysToAppear.add("Wednesday");
                                daysToAppear.add("Thursday");
                                daysToAppear.add("Friday");
                                daysToAppear.add("Saturday");
                                daysToAppear.add("Sunday");
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

    private void updateLogFile(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText +"Log.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();

                        StringBuilder sb = new StringBuilder();
                        //read each line
                        while (line != null) {
                            sb.append(line + "\n");
                            line = br.readLine();
                        }
                        //append current date
                        SimpleDateFormat taskSimpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        Date taskDate = new Date();
                        String currentDate = taskSimpleDateFormat.format(taskDate);
                        sb.append(currentDate + "\n");

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

    private void deleteLogFile(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText +"Log.txt")){
                    boolean isItDeleted = file.delete();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updatePointcardFile(){
        //Get streak and percentage
        ArrayList<String> idealDates = new ArrayList<String>();
        float streakInt = 1;
        float percentage = 100;

        boolean habitual = getTaskType(homeTaskbarContext.getFilesDir(), taskText);
        String days = getTaskData(homeTaskbarContext.getFilesDir(), taskText);
        ArrayList<String> daysArray = daysToDaysArray(days); //Days To Array
        ArrayList<String> taskDates = getLogFile(homeTaskbarContext.getFilesDir(), taskText);
        if (!taskDates.isEmpty()){
            idealDates = getIdealDates(taskDates, daysArray); //Perfect Performance
            streakInt = calculateStreak(idealDates, taskDates); //Streak
            percentage = calculatePercentage(idealDates, taskDates); //Percentage
        }

        //Task Points
        int oldPoints = getcurrentPoints(homeTaskbarContext.getFilesDir(), taskText);
        int newPoints = oldPoints + (Math.round(10*streakInt* (percentage/100)));
        updatePointFile(homeTaskbarContext.getFilesDir(), taskText, newPoints);

        //AllTime Score
        int oldPointsAllTime = getcurrentPointsAllTime(homeTaskbarContext.getFilesDir());
        int newPointsAllTime = oldPointsAllTime + (Math.round(10 * streakInt * (percentage/100)));
        updateAllTimePointFile(homeTaskbarContext.getFilesDir(), newPointsAllTime);

        //Monthly Score
        SimpleDateFormat taskSimpleDateFormat = new SimpleDateFormat("MMMM");
        Date taskDate = new Date();
        String currentDateMonth = taskSimpleDateFormat.format(taskDate);
        //String currentDateMonth = "February";

        boolean resetMonth = checkIfMonthNeedsResetting(homeTaskbarContext.getFilesDir(), currentDateMonth);
        if (resetMonth){
            int oldPointsMonth = 0;
            int newPointsMonth = oldPointsMonth + (Math.round(10 * streakInt * (percentage/100)));
            updateMonthPointFileDated(homeTaskbarContext.getFilesDir(), newPointsMonth, currentDateMonth);
        } else {
            int oldPointsMonth = getcurrentPointsMonth(homeTaskbarContext.getFilesDir());
            int newPointsMonth = oldPointsMonth + (Math.round(10 * streakInt * (percentage/100)));
            updateMonthPointFile(homeTaskbarContext.getFilesDir(), newPointsMonth, currentDateMonth);
        }

        //Weekly Score
        SimpleDateFormat taskSimpleDateFormatWeekly = new SimpleDateFormat("dd-MM-yyyy");
        Date taskDateWeekly = new Date();
        String currentDateWeekly = taskSimpleDateFormatWeekly.format(taskDateWeekly);
        //String currentDateWeekly = "28-12-2022";

        SimpleDateFormat taskSimpleDateFormatWeeklyDay = new SimpleDateFormat("EEEE");
        Date taskDateWeeklyDay = new Date();
        String currentDateWeeklyDay = taskSimpleDateFormatWeeklyDay.format(taskDateWeeklyDay);
        //String currentDateWeeklyDay = "Sunday";


        String oldDateStr = getPreviousDateWeeklyScore(homeTaskbarContext.getFilesDir());

        if(currentDateWeeklyDay.equals("Sunday")){
            boolean resetWeek = checkIfWeekNeedsResetting(homeTaskbarContext.getFilesDir(), currentDateWeekly);
            if(resetWeek){
                int oldPointsWeek = 0;
                int newPointsWeek = oldPointsWeek + (Math.round(10 * streakInt * (percentage/100)));
                updateWeekPointFileDated(homeTaskbarContext.getFilesDir(), newPointsWeek, currentDateWeekly);
            } else {
                int oldPointsWeek = getcurrentPointsWeek(homeTaskbarContext.getFilesDir());
                int newPointsWeek = oldPointsWeek + (Math.round(10 * streakInt * (percentage/100)));
                updateWeekPointFile(homeTaskbarContext.getFilesDir(), newPointsWeek, currentDateWeekly);
            }
        } else {
            int oldPointsWeek = getcurrentPointsWeek(homeTaskbarContext.getFilesDir());
            int newPointsWeek = oldPointsWeek + (Math.round(10 * streakInt * (percentage/100)));
            updateWeekPointFile(homeTaskbarContext.getFilesDir(), newPointsWeek, oldDateStr);
        }

    }

    private boolean getTaskType(File dir, String taskName){
        File[] files = dir.listFiles();
        boolean isItHabitual = false;
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
                }
            }
        }
        return isItHabitual;
    }

    private String getTaskData(File dir, String taskName){
        File[] files = dir.listFiles();
        boolean isItHabitual = false;
        String days = null;
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
                            } else if (line.matches("Habit Days =.*")){
                                if (isItHabitual){
                                    String[] value = line.split("=");
                                    days = value[1];
                                } else {
                                    days = "Non-Habitual";
                                }
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
        return days;
    }

    private ArrayList<String> daysToDaysArray(String days){
        ArrayList<String> daysArray = new ArrayList<String>();
        String[] value = days.split(", ");
        daysArray.addAll(Arrays.asList(value));
        return daysArray;
    }

    private ArrayList<String> getLogFile(File dir, String taskName){
        ArrayList<String> taskDates = new ArrayList<String>();
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
        return taskDates;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<String> getIdealDates(ArrayList<String> taskDates, ArrayList<String> daysArray){
        ArrayList<String> idealDates = new ArrayList<String>();

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
        return idealDates;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int calculateStreak(ArrayList<String> idealDates, ArrayList<String> taskDates){
        int streakInt = 0;
        for (String idealLocalDate : idealDates){
            String[] splitter = idealLocalDate.split("-");
            String idealDate = splitter[2] + "-" + splitter[1] + "-" + splitter[0];
            if (taskDates.contains(idealDate)){
                streakInt++;
            } else {
                streakInt = 0;
            }
        }
        if (streakInt == 0){
            streakInt = 1;
        }
        return streakInt;
    }

    private int calculatePercentage(ArrayList<String> idealDates, ArrayList<String> taskDates){
        int percentage = 0;
        float divideBy = idealDates.size();
        float divide = taskDates.size();
        float result = (divide / divideBy) * 100;
        percentage = Math.round(result);
        return percentage;
    }

    private int getcurrentPoints(File dir, String taskName){
        int oldPoints = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskName + "Pointcard.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Points =.*")){
                                String[] value = line.split("=");
                                oldPoints = Integer.parseInt(value[1]);
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
        return oldPoints;
    }

    private void updatePointFile(File dir, String taskName, int newPoints){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText +"Pointcard.txt")){
                    try {
                        String pointString = Integer.toString(newPoints);
                        String output = "Points =" + pointString;
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

    private int getcurrentPointsAllTime(File dir){
        int oldPoints = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("AllTimeScore.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Points =.*")){
                                String[] value = line.split("=");
                                oldPoints = Integer.parseInt(value[1]);
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
        return oldPoints;
    }

    private void updateAllTimePointFile(File dir, int newPoints){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("AllTimeScore.txt")){
                    try {
                        String pointString = Integer.toString(newPoints);
                        String output = "Points =" + pointString;
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

    private boolean checkIfMonthNeedsResetting(File dir, String currentMonth){
        boolean resetMonth = false;
        String fileMonth;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("MonthlyScore.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Start Date =.*")){
                                String[] value = line.split("=");
                                fileMonth = value[1];
                                if(!currentMonth.equals(fileMonth)){
                                    resetMonth = true;
                                }
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
        return resetMonth;
    }

    private void updateMonthPointFileDated(File dir, int newPoints, String currentDate){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("MonthlyScore.txt")){
                    try {
                        StringBuilder fileBody = new StringBuilder();
                        fileBody.append("Start Date =" + currentDate + "\n");
                        fileBody.append("Points =" + newPoints + "\n");
                        String fileBodyString = fileBody.toString();

                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
                        fos.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    private int getcurrentPointsMonth(File dir){
        int oldPoints = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("MonthlyScore.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Points =.*")){
                                String[] value = line.split("=");
                                oldPoints = Integer.parseInt(value[1]);
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
        return oldPoints;
    }

    private void updateMonthPointFile(File dir, int newPoints, String currentDate){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("MonthlyScore.txt")){
                    try {
                        StringBuilder fileBody = new StringBuilder();
                        fileBody.append("Start Date =" + currentDate + "\n");
                        fileBody.append("Points =" + newPoints + "\n");
                        String fileBodyString = fileBody.toString();

                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
                        fos.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    private boolean checkIfWeekNeedsResetting(File dir, String currentMonth){
        boolean resetWeek = false;
        String fileMonth;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("WeeklyScore.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Start Date =.*")){
                                String[] value = line.split("=");
                                fileMonth = value[1];
                                if(!currentMonth.equals(fileMonth)){
                                    resetWeek = true;
                                }
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
        return resetWeek;
    }

    private String getPreviousDateWeeklyScore(File dir){
        boolean resetWeek = false;
        String fileMonth;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("WeeklyScore.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Start Date =.*")){
                                String[] value = line.split("=");
                                fileMonth = value[1];
                                return fileMonth;
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
        return null;
    }

    private void updateWeekPointFileDated(File dir, int newPoints, String currentDate){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("WeeklyScore.txt")){
                    try {
                        StringBuilder fileBody = new StringBuilder();
                        fileBody.append("Start Date =" + currentDate + "\n");
                        fileBody.append("Points =" + newPoints + "\n");
                        String fileBodyString = fileBody.toString();

                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
                        fos.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }

    private int getcurrentPointsWeek(File dir){
        int oldPoints = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("WeeklyScore.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Points =.*")){
                                String[] value = line.split("=");
                                oldPoints = Integer.parseInt(value[1]);
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
        return oldPoints;
    }

    private void updateWeekPointFile(File dir, int newPoints, String currentDate){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches("WeeklyScore.txt")){
                    try {
                        StringBuilder fileBody = new StringBuilder();
                        fileBody.append("Start Date =" + currentDate + "\n");
                        fileBody.append("Points =" + newPoints + "\n");
                        String fileBodyString = fileBody.toString();

                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(fileBodyString.getBytes(StandardCharsets.UTF_8));
                        fos.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
    }
}
