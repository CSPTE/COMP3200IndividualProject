package com.example.finalversion.ui.gallery;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalversion.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private LinearLayout colorful;
    private HashMap<String, ArrayList<String>> habitDays;
    private HashMap<String, Integer> weekDayCount = new HashMap<>();
    private Context cont;
    private String thisMonth;
    private int thisMonthInt;
    private String thisYear;
    private ArrayList<String> tasksSelected = new ArrayList<String>();
    private HashMap<String, String> habitColors = new HashMap<>();
    private boolean habitsAreSelected = true;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, HashMap<String, ArrayList<String>> alma, Context korte, String barack)
    {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        habitDays = alma;
        cont = korte;
        getDailyCount();
        String[] temp = barack.split(" ");
        thisMonth = temp[0];
        thisYear = temp[1];
        getMonthAsNumber(thisMonth);
        File taskFile = new File(cont.getFilesDir(), "TrackedHabits");
        if (taskFile.exists()) {
            findTasksSelected(cont.getFilesDir());
            for(String task : tasksSelected){
                findHabitColors(cont.getFilesDir(), task);
            }
        } else {
            habitsAreSelected = false;
        }
        String check = "Check";
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener, habitColors);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        holder.dayOfMonth.setText(daysOfMonth.get(position));

        //set color box border on existing days
        colorful = holder.itemView.findViewById(R.id.boxyBox);
        if (!holder.dayOfMonth.getText().toString().equals("")){
            SimpleDateFormat day = new SimpleDateFormat("dd");
            Date date = new Date();
            String currentDay = day.format(date);
            SimpleDateFormat month = new SimpleDateFormat("M");
            Date date2 = new Date();
            String currentMonth = month.format(date2);
            SimpleDateFormat year = new SimpleDateFormat("yyyy");
            Date date3 = new Date();
            String currentYear = year.format(date3);
            if((Integer.parseInt(thisYear) < Integer.parseInt(currentYear)) ||
                    ((Integer.parseInt(thisYear) == Integer.parseInt(currentYear)) && (thisMonthInt < Integer.parseInt(currentMonth))) ||
                    ((Integer.parseInt(thisYear) == Integer.parseInt(currentYear)) && (thisMonthInt == Integer.parseInt(currentMonth)) && (Integer.parseInt(holder.dayOfMonth.getText().toString()) <= Integer.parseInt(currentDay)))) {

                if(position % 7 == 0) {
                    //Its Sunday
                    int nr = weekDayCount.get("Sunday");
                    ColorBox cb = new ColorBox(cont, nr);
                    colorful.addView(cb);
                    if(nr > 0){
                        colorful.setBackgroundResource(R.drawable.calendar_square);
                    }
                    setColors(nr, "Sunday", cb, holder);
                } else if(position % 7 == 1) {
                    //Its Monday
                    int nr = weekDayCount.get("Monday");
                    ColorBox cb = new ColorBox(cont, nr);
                    colorful.addView(cb);
                    if(nr > 0){
                        colorful.setBackgroundResource(R.drawable.calendar_square);
                    }
                    setColors(nr, "Monday", cb, holder);
                } else if(position % 7 == 2) {
                    //Its Tuesday
                    int nr = weekDayCount.get("Tuesday");
                    ColorBox cb = new ColorBox(cont, nr);
                    colorful.addView(cb);
                    if(nr > 0){
                        colorful.setBackgroundResource(R.drawable.calendar_square);
                    }
                    setColors(nr, "Tuesday", cb, holder);
                } else if(position % 7 == 3) {
                    //Its Wednesday
                    int nr = weekDayCount.get("Wednesday");
                    ColorBox cb = new ColorBox(cont, nr);
                    colorful.addView(cb);
                    if(nr > 0){
                        colorful.setBackgroundResource(R.drawable.calendar_square);
                    }
                    setColors(nr, "Wednesday", cb, holder);
                } else if(position % 7 == 4) {
                    //Its Thursday
                    int nr = weekDayCount.get("Thursday");
                    ColorBox cb = new ColorBox(cont, nr);
                    colorful.addView(cb);
                    if(nr > 0){
                        colorful.setBackgroundResource(R.drawable.calendar_square);
                    }
                    setColors(nr, "Thursday", cb, holder);
                } else if(position % 7 == 5) {
                    //Its Friday
                    int nr = weekDayCount.get("Friday");
                    ColorBox cb = new ColorBox(cont, nr);
                    colorful.addView(cb);
                    if(nr > 0){
                        colorful.setBackgroundResource(R.drawable.calendar_square);
                    }
                    setColors(nr, "Friday", cb, holder);
                } else if(position % 7 == 6) {
                    //Its Saturday
                    int nr = weekDayCount.get("Saturday");
                    ColorBox cb = new ColorBox(cont, nr);
                    colorful.addView(cb);
                    if(nr > 0){
                        colorful.setBackgroundResource(R.drawable.calendar_square);
                    }
                    setColors(nr, "Saturday", cb, holder);
                }
            } else {
                boolean futureTaskExistOnThisDay;
                futureTaskExistOnThisDay = checkFutureTasks(cont.getFilesDir(), holder.dayOfMonth.getText().toString());
                if (futureTaskExistOnThisDay == true){
                    colorful.setBackgroundResource(R.drawable.calendar_square_future);
                }
            }
        }

        //background color for today
        SimpleDateFormat day = new SimpleDateFormat("dd");
        Date date = new Date();
        String currentDay = day.format(date);
        if (currentDay.equals(holder.dayOfMonth.getText().toString())){
            SimpleDateFormat month = new SimpleDateFormat("MMMM");
            Date date2 = new Date();
            String currentMonth = month.format(date2);
            if (currentMonth.equals(thisMonth)){
                SimpleDateFormat year = new SimpleDateFormat("yyyy");
                Date date3 = new Date();
                String currentYear = year.format(date3);
                if (currentYear.equals(thisYear)){
                    holder.dayOfMonth.setBackgroundResource(R.drawable.calendar_day_current); //;Color(Integer.parseInt("#007fff"));
                }
            }
        }
    }

    private boolean checkFutureTasks(File dir, String currentDay){
        File[] files = dir.listFiles();
        boolean doesItExist = false;
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(".*FutureTakk.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            if (line.matches("Date Of Task=.*")){
                                String[] value = line.split("=");
                                String dated = value[1];
                                String[] periods = dated.split("-");
                                if ((periods[0].equals(currentDay)) && (Integer.parseInt(periods[1]) == thisMonthInt) && (periods[2].equals(thisYear))){
                                    doesItExist = true;
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
        return doesItExist;
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public interface  OnItemListener
    {
        void onItemClick(int position, String dayText, LinearLayout calendarCircle, HashMap<String, String> habitColors);
    }

    private void getDailyCount(){
        for (String key : habitDays.keySet()){
            if (key.equals("Sunday")){
                Integer temp = habitDays.get(key).size();
                weekDayCount.put("Sunday", temp);
            } else if (key.equals("Monday")) {
                Integer temp = habitDays.get(key).size();
                weekDayCount.put("Monday", temp);
            } else if (key.equals("Tuesday")) {
                Integer temp = habitDays.get(key).size();
                weekDayCount.put("Tuesday", temp);
            } else if (key.equals("Wednesday")) {
                Integer temp = habitDays.get(key).size();
                weekDayCount.put("Wednesday", temp);
            } else if (key.equals("Thursday")) {
                Integer temp = habitDays.get(key).size();
                weekDayCount.put("Thursday", temp);
            } else if (key.equals("Friday")) {
                Integer temp = habitDays.get(key).size();
                weekDayCount.put("Friday", temp);
            } else if (key.equals("Saturday")) {
                Integer temp = habitDays.get(key).size();
                weekDayCount.put("Saturday", temp);
            }
        }
    }

    private void getMonthAsNumber(String monthLetters){
        if (monthLetters.equals("January")){
            thisMonthInt = 1;
        } else if (monthLetters.equals("February")){
            thisMonthInt = 2;
        } else if (monthLetters.equals("March")){
            thisMonthInt = 3;
        } else if (monthLetters.equals("April")){
            thisMonthInt = 4;
        } else if (monthLetters.equals("May")){
            thisMonthInt = 5;
        } else if (monthLetters.equals("June")){
            thisMonthInt = 6;
        } else if (monthLetters.equals("July")){
            thisMonthInt = 7;
        } else if (monthLetters.equals("August")){
            thisMonthInt = 8;
        } else if (monthLetters.equals("September")){
            thisMonthInt = 9;
        } else if (monthLetters.equals("October")){
            thisMonthInt = 10;
        } else if (monthLetters.equals("November")){
            thisMonthInt = 11;
        } else if (monthLetters.equals("December")){
            thisMonthInt = 12;
        }
    }

    private void findTasksSelected(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches( "TrackedHabits.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        //read each line
                        while (line != null) {
                            tasksSelected.add(line);
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

    private void findHabitColors(File dir, String taskText){
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
                            if (line.matches("Color Selected =.*")){
                                String[] value = line.split("=");
                                String taskColor = "#" + value[1];
                                habitColors.put(taskText, taskColor);
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

    private void setColors(int number, String day, ColorBox c, CalendarViewHolder hold){
        if (habitsAreSelected){
            ArrayList<String> passHabits = new ArrayList<String>();
            passHabits.addAll(Objects.requireNonNull(habitDays.get(day)));
            ArrayList<String> colorsToPass = new ArrayList<String>();
            for(String habit : passHabits){
                colorsToPass.add(linkToLogFile(cont.getFilesDir(), habit, hold, habitColors.get(habit)));
            }

            if(number == 1){
                c.initTaskOneSectors(colorsToPass.get(0));
            } else if (number == 2){
                c.initTaskTwoSectors(colorsToPass.get(0), colorsToPass.get(1));
            } else if (number == 3){
                c.initTaskThreeSectors(colorsToPass.get(0), colorsToPass.get(1), colorsToPass.get(2));
            } else if (number == 4){
                c.initTaskFourSectors(colorsToPass.get(0), colorsToPass.get(1), colorsToPass.get(2), colorsToPass.get(3));
            }
        }
    }

    private String linkToLogFile(File dir, String taskText, CalendarViewHolder holdUp, String color){
        File[] files = dir.listFiles();
        String finalColor = null;
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                String fileName = file.getName() + ".txt";
                if (fileName.matches(taskText +"Log.txt")){
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        String dayTested = holdUp.dayOfMonth.getText().toString() + "-" + thisMonthInt + "-" + thisYear;
                        String simpleDayOfMonth = "0" + holdUp.dayOfMonth.getText().toString();
                        String simpleDayTested = simpleDayOfMonth + "-" + thisMonthInt + "-" + thisYear;
                        System.out.println(dayTested);
                        boolean dayFound = false;
                        //read each line
                        while (line != null) {
                            if ((line.matches(dayTested)) || (line.matches(simpleDayTested))){
                                dayFound = true;
                            }
                            line = br.readLine();
                        }
                        if (dayFound == true) {
                            finalColor = color;
                        } else {
                            String[] splitter = color.split("#");
                            String background = "#00" + splitter[1];
                            finalColor = background;
                        }
                        br.close();
                    } catch (IOException e) {
                        Log.d("Exception",e.toString());
                    }
                }
            }
        }
        return finalColor;
    }
}